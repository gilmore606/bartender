package com.dlfsystems.bartender.fragments

import android.app.Application
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.dlfsystems.bartender.Action
import com.dlfsystems.bartender.BaseFragment
import com.dlfsystems.bartender.R
import com.dlfsystems.bartender.nav.BaseKey
import com.dlfsystems.bartender.nav.FragAnimPair
import com.dlfsystems.bartender.room.BarDB
import com.dlfsystems.bartender.room.Bottle
import com.dlfsystems.bartender.room.Drink
import com.dlfsystems.bartender.room.Family
import com.dlfsystems.bartender.views.DrinklistView
import com.dlfsystems.bartender.views.Tag
import com.dlfsystems.bartender.views.TagbarView
import kotlinx.android.parcel.Parcelize
import com.ms.square.android.expandabletextview.ExpandableTextView

class BottleFragment : BaseFragment() {

    data class BottleState(
        val id: Long = 0,
        val boundBottle: Boolean = false,
        val name: String = "",
        val desc: Int = 0,
        val image: String = "",
        val active: Boolean = false,
        val shopping: Boolean = false,
        val boundDrinks: Boolean = false,
        val drinks: ArrayList<Drink> = ArrayList(0),
        val boundTags: Boolean = false,
        val tags: ArrayList<Tag> = ArrayList(0)
    ) : BaseState()

    @Parcelize
    data class BottleKey(val bottleId: Long) : BaseKey() {
        override fun createFragment() = BottleFragment().apply {
            arguments = (arguments ?: Bundle()).also {
                it.putSerializable("bottleId", bottleId)
            }
        }
        override fun getAnimation() =
            FragAnimPair(R.anim.grow_fade_in_from_bottom, R.anim.blank)
        override fun getBackAnimation() =
            FragAnimPair(R.anim.blank, R.anim.shrink_fade_out_from_bottom)
    }


    class BottleView(val bottleFragment: BaseFragment) : BaseViewController() {

        class BottleViewModel(bottleId: Long, application: Application) : AndroidViewModel(application) {
            val bottle: LiveData<Bottle> = BarDB.getInstance(getApplication()).bottleDao().liveById(bottleId)
        }
        class BottleDrinksViewModel(bottleId: Long, application: Application) : AndroidViewModel(application) {
            val drinks: LiveData<List<Drink>> = BarDB.getInstance(getApplication()).drinkDao().liveDrinksForBottle(bottleId)
        }
        class BottleTagsViewModel(bottleId: Long, application: Application): AndroidViewModel(application) {
            val tags: LiveData<List<Family>> = BarDB.getInstance(getApplication()).familyDao().liveFamiliesForBottle(bottleId)
        }

        var bottleViewModel: BottleViewModel? = null
        var bottleDrinksViewModel: BottleDrinksViewModel? = null
        var bottleTagsViewModel: BottleTagsViewModel? = null
        var bottleName: TextView? = null
        var bottleImage: ImageView? = null
        var bottleActive: CheckBox? = null
        var bottleShopping: CheckBox? = null
        var bottleAbout: ExpandableTextView? = null
        var bottleTagbar: TagbarView? = null
        var bottleDrinklist: DrinklistView? = null

        override fun subscribeActions() {
            mainView?.also {
                bottleName = it.findViewById(R.id.bottle_name) as TextView
                bottleImage = it.findViewById(R.id.bottle_image) as ImageView
                bottleActive = it.findViewById(R.id.bottle_active) as CheckBox
                bottleShopping = it.findViewById(R.id.bottle_shopping) as CheckBox
                bottleAbout = it.findViewById(R.id.bottle_about) as ExpandableTextView
                bottleTagbar = it.findViewById(R.id.bottle_tagbar) as TagbarView
                bottleDrinklist = it.findViewById(R.id.bottle_drinklist) as DrinklistView

                bottleActive?.setOnClickListener { action.onNext(Action.bottleToggleActive()) }
                bottleShopping?.setOnClickListener { action.onNext(Action.bottleToggleShopping()) }
            }
        }

        override fun render(previousState: BaseState?, state: BaseState) {
            state as BottleState
            previousState as BottleState?
            if (state.boundBottle) {
                bottleName?.text = state.name
                if (!(previousState?.boundBottle ?: false)) {
                    bottleImage?.also {
                        it.startAnimation(AnimationUtils.loadAnimation(mainView!!.context, R.anim.fade_in))
                        Glide.with(mainView!!.context)
                            .load(Uri.parse("file:///android_asset/bottle/" + state.image + ".png"))
                            .asBitmap().into(it)
                        it.visibility = View.VISIBLE
                    }
                }
                val aboutString = try { bottleFragment.getString(state.desc) } catch (e: Exception) { " " }.replace("\n", "\n\n")
                bottleAbout?.text = aboutString
                bottleActive?.isChecked = state.active
                bottleShopping?.isChecked = state.shopping
            } else {
                bottleViewModel = BottleViewModel(state.id, bottleFragment.context!!.applicationContext as Application)
                bottleViewModel?.bottle?.observe(bottleFragment, Observer {
                    action.onNext(Action.bottleLoad(it))
                })
            }
            if (state.boundDrinks) {
                bottleDrinklist?.populate(state.drinks)
            } else {
                bottleDrinksViewModel = BottleDrinksViewModel(state.id, bottleFragment.context!!.applicationContext as Application)
                bottleDrinksViewModel?.drinks?.observe(bottleFragment, Observer {
                    action.onNext(Action.bottleLoadDrinks(it))
                })
            }
            if (state.boundTags) {
                bottleTagbar?.populate(state.tags)
            } else {
                bottleTagsViewModel = BottleTagsViewModel(state.id, bottleFragment.context!!.applicationContext as Application)
                bottleTagsViewModel?.tags?.observe(bottleFragment, Observer {
                    action.onNext(Action.bottleLoadTags(it))
                })
            }
        }
    }

    private fun tagsFromFamilies(families: ArrayList<Family>): ArrayList<Tag> {
        val tags = ArrayList<Tag>(0)
        families.forEach {
            tags.add(Tag(id = it.id, isBottleTag = true, tag = it.name, description = it.description))
        }
        return tags
    }

    override val layoutResource = R.layout.fragment_bottle
    override val viewController = BottleView(this)
    override fun getDefaultState() = BottleState()

    override fun makeStateFromArguments(arguments: Bundle): BaseState =
            BottleState(
                id = arguments.getSerializable("bottleId") as Long,
                boundBottle = false,
                boundDrinks = false
            )

    override fun hearAction(action: Action) {
        when (action) {
            is Action.bottleToggleActive -> {
                val state = previousState as BottleState
                setBottleActive(state.id, state.name, !state.active)
            }
            is Action.bottleToggleShopping -> {
                val state = previousState as BottleState
                setBottleShopping(state.id, state.name, !state.shopping)
            }
            is Action.bottleLoad -> {
                changeState(
                    (previousState as BottleState).copy(
                        boundBottle = true,
                        name = action.load.name,
                        desc = action.load.descstr,
                        image = action.load.image,
                        active = action.load.active,
                        shopping = action.load.shopping
                    )
                )
            }
            is Action.bottleLoadDrinks -> {
                changeState(
                    (previousState as BottleState).copy(
                        boundDrinks = true,
                        drinks = ArrayList(action.load)
                    )
                )
            }
            is Action.bottleLoadTags -> {
                changeState(
                    (previousState as BottleState).copy(
                        boundTags = true,
                        tags = tagsFromFamilies(ArrayList(action.load))
                    )
                )
            }
            else -> { }
        }
    }
}