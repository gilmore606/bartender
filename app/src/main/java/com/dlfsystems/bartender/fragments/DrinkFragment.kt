package com.dlfsystems.bartender.fragments

import android.app.Application
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.ScrollView
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
import com.dlfsystems.bartender.room.Ingredient
import com.dlfsystems.bartender.views.IngredientsView
import com.ms.square.android.expandabletextview.ExpandableTextView
import kotlinx.android.parcel.Parcelize

class DrinkFragment : BaseFragment() {

    data class DrinkState(
        val id: Long = 0,
        val boundDrink: Boolean = false,
        val name: String = "",
        val favorite: Boolean = false,
        val image: String = "",
        val info: Int = 0,
        val make: Int = 0,
        val garnish: Int = 0,
        val boundIngredients: Boolean = false,
        val ingredients: ArrayList<Ingredient> = ArrayList(0)
    ) : BaseState()

    @Parcelize
    data class DrinkKey(val drinkId: Long) : BaseKey() {
        override fun createFragment() = DrinkFragment().apply {
            arguments = (arguments ?: Bundle()).also {
                it.putSerializable("drinkId", drinkId)
            }
        }
        override fun getAnimation() =
                FragAnimPair(R.anim.grow_fade_in_from_bottom, R.anim.blank)
        override fun getBackAnimation() =
                FragAnimPair(R.anim.blank, R.anim.shrink_fade_out_from_bottom)
    }


    class DrinkView(val drinkFragment: BaseFragment) : BaseViewController() {

        class DrinkViewModel(drinkId: Long, application: Application) : AndroidViewModel(application) {
            val drink: LiveData<Drink> = BarDB.getInstance(getApplication()).drinkDao().liveById(drinkId)
        }
        class DrinkIngredientsViewModel(drinkId: Long, application: Application) : AndroidViewModel(application) {
            val bottles: LiveData<List<Ingredient>> = BarDB.getInstance(getApplication()).drinkDao().liveIngredientsForDrink(drinkId)
        }

        var drinkViewModel: DrinkViewModel? = null
        var drinkIngredientsViewModel: DrinkIngredientsViewModel? = null
        var drinkName: TextView? = null
        var drinkFavorite: CheckBox? = null
        var drinkIngredients: IngredientsView? = null
        var drinkAbout: ExpandableTextView? = null
        var drinkAboutHeader: TextView? = null
        var drinkImage: ImageView? = null
        var drinkMake: TextView? = null
        var drinkGarnish: TextView? = null
        var scrollView: ScrollView? = null

        override fun subscribeActions() {
            mainView?.also {
                drinkName = it.findViewById(R.id.drink_name) as TextView
                drinkFavorite = it.findViewById(R.id.drink_favorite) as CheckBox
                drinkIngredients = it.findViewById(R.id.drink_bottlelist) as IngredientsView
                drinkAbout = it.findViewById(R.id.drink_about) as ExpandableTextView
                drinkAboutHeader = it.findViewById(R.id.drink_aboutheader) as TextView
                drinkImage = it.findViewById(R.id.drink_image) as ImageView
                drinkMake = it.findViewById(R.id.drink_directions) as TextView
                drinkGarnish = it.findViewById(R.id.drink_garnish) as TextView
                scrollView = it.findViewById(R.id.drink_scrollview) as ScrollView

                drinkFavorite?.setOnClickListener { action.onNext(Action.drinkToggleFavorite()) }

                scrollView?.viewTreeObserver?.addOnScrollChangedListener {
                    drinkImage!!.y = (scrollView!!.scrollY / 2).toFloat()
                }
            }
        }

        override fun render(previousState: BaseState?, state: BaseState) {
            state as DrinkState
            previousState as DrinkState?
            if (state.boundDrink) {
                drinkName?.text = state.name
                drinkFavorite?.isChecked = state.favorite
                drinkAbout?.text = (try { drinkFragment.getString(state.info) } catch (e: Exception) { "" }).replace("\n", "\n\n")
                if (drinkAbout?.text == "") {
                    drinkAboutHeader?.visibility = View.GONE
                }
                if (!(previousState?.boundDrink ?: false) && state.image != "") {
                    drinkImage?.also {
                        drinkImage?.startAnimation(AnimationUtils.loadAnimation(mainView!!.context, R.anim.fade_in))
                        Glide.with(mainView!!.context).load(Uri.parse("file:///android_asset/drink/" + state.image + ".jpg"))
                            .asBitmap().into(drinkImage)
                    }
                }
                drinkMake?.text = (try { drinkFragment.getString(state.make) } catch (e: Exception) { " ??? " }).replace("\n", "\n\n")
                drinkGarnish?.text = try { drinkFragment.getString(state.garnish) } catch (e: Exception) { "none" }
            } else {
                drinkViewModel = DrinkViewModel(state.id, drinkFragment.context!!.applicationContext as Application)
                drinkViewModel?.drink?.observe(drinkFragment, Observer {
                    action.onNext(Action.drinkLoad(it))
                })
            }
            if (state.boundIngredients) {
                drinkIngredients?.populate(state.ingredients)
            } else {
                drinkIngredientsViewModel = DrinkIngredientsViewModel(state.id, drinkFragment.context!!.applicationContext as Application)
                drinkIngredientsViewModel?.bottles?.observe(drinkFragment, Observer {
                    action.onNext(Action.drinkLoadIngredients(it))
                })
            }
        }
    }

    override val layoutResource = R.layout.fragment_drink
    override val viewController = DrinkView(this)
    override fun getDefaultState() = DrinkState()

    override fun makeStateFromArguments(arguments: Bundle): BaseState =
            DrinkState(
                id = arguments.getSerializable("drinkId") as Long,
                boundDrink = false,
                boundIngredients = false
            )

    override fun hearAction(action: Action) {
        when (action) {
            is Action.drinkToggleFavorite -> {
                val state = previousState as DrinkState
                setDrinkFavorite(state.id, state.name, !state.favorite)
            }
            is Action.drinkLoad -> {
                changeState(
                    (previousState as DrinkState).copy(
                        boundDrink = true,
                        name = action.load.name,
                        favorite = action.load.favorite,
                        image = action.load.image,
                        info = action.load.info,
                        make = action.load.make,
                        garnish = action.load.garnish
                    )
                )
            }
            is Action.drinkLoadIngredients -> {
                changeState(
                    (previousState as DrinkState).copy(
                        boundIngredients = true,
                        ingredients = ArrayList(action.load)
                    )
                )
            }
            else -> { }
        }
    }
}