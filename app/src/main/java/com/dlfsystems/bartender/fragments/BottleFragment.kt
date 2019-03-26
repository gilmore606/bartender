package com.dlfsystems.bartender.fragments

import android.app.Application
import android.os.Bundle
import android.util.Log
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.dlfsystems.bartender.Action
import com.dlfsystems.bartender.BaseFragment
import com.dlfsystems.bartender.R
import com.dlfsystems.bartender.ioThread
import com.dlfsystems.bartender.nav.BaseKey
import com.dlfsystems.bartender.nav.FragAnimPair
import com.dlfsystems.bartender.room.BarDB
import com.dlfsystems.bartender.room.Bottle
import kotlinx.android.parcel.Parcelize
import com.ms.square.android.expandabletextview.ExpandableTextView

class BottleFragment : BaseFragment() {

    data class BottleState(
        val id: Long = 0,
        val bound: Boolean = false,
        val name: String = "",
        val desc: Int = 0,
        val image: Int = 0,
        val active: Boolean = false,
        val shopping: Boolean = false
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

        var bottleViewModel: BottleViewModel? = null
        var bottleName: TextView? = null
        var bottleImage: ImageView? = null
        var bottleActive: CheckBox? = null
        var bottleAbout: ExpandableTextView? = null

        override fun subscribeActions() {
            mainView?.also {
                bottleName = it.findViewById(R.id.bottle_name) as TextView
                bottleImage = it.findViewById(R.id.bottle_image) as ImageView
                bottleActive = it.findViewById(R.id.bottle_active) as CheckBox
                bottleAbout = it.findViewById(R.id.bottle_about) as ExpandableTextView

                bottleActive?.setOnClickListener { action.onNext(Action.bottleToggleActive()) }
            }
        }

        override fun render(previousState: BaseState, state: BaseState) {
            state as BottleState
            previousState as BottleState
            if (state.bound) {
                bottleName?.text = state.name
                if (!previousState.bound) {
                    bottleImage?.startAnimation(AnimationUtils.loadAnimation(mainView!!.context, R.anim.fade_in))
                    bottleImage?.setImageDrawable(ContextCompat.getDrawable(mainView!!.context, state.image))
                }
                val aboutString = try { bottleFragment.getString(state.desc) } catch (e: Exception) { " " }
                bottleAbout?.text = aboutString
                bottleActive?.isChecked = state.active
            } else {
                bottleViewModel = BottleViewModel(state.id, bottleFragment.context!!.applicationContext as Application)
                bottleViewModel?.bottle?.observe(bottleFragment, Observer {
                    bottleFragment.changeState(state.copy(
                        bound = true,
                        name = it.name,
                        desc = it.desc,
                        image = it.image,
                        active = it.active,
                        shopping = it.shopping
                    ))
                })
            }
        }
    }

    override val layoutResource = R.layout.fragment_bottle
    override val viewController = BottleView(this)
    override fun getDefaultState() = BottleState()

    override fun makeStateFromArguments(arguments: Bundle): BaseState =
            BottleState(
                id = arguments.getSerializable("bottleId") as Long,
                bound = false
            )

    override fun hearAction(action: Action) {
        val state = previousState as BottleState

        when (action) {
            is Action.bottleToggleActive -> {
                ioThread {
                    BarDB.setBottleActive(view!!.context, state.id, !state.active)
                }
            }
            else -> { }
        }
    }
}