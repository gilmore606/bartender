package com.dlfsystems.bartender.fragments

import android.app.Application
import android.os.Bundle
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import com.dlfsystems.bartender.Action
import com.dlfsystems.bartender.BaseFragment
import com.dlfsystems.bartender.R
import com.dlfsystems.bartender.ioThread
import com.dlfsystems.bartender.nav.BaseKey
import com.dlfsystems.bartender.room.BarDB
import com.dlfsystems.bartender.room.Bottle
import kotlinx.android.parcel.Parcelize

class BottleFragment : BaseFragment() {

    data class BottleState(
        val id: Long = 0,
        val loaded: Boolean = false,
        val name: String = "",
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
    }


    class BottleView(val bottleFragment: BaseFragment) : BaseViewController() {

        class BottleViewModel(bottleId: Long, application: Application) : AndroidViewModel(application) {
            val bottle: LiveData<Bottle> = BarDB.getInstance(getApplication()).bottleDao().liveById(bottleId)
        }

        var bottleViewModel: BottleViewModel? = null
        var bottleName: TextView? = null
        var bottleImage: ImageView? = null
        var bottleActive: CheckBox? = null

        override fun subscribeActions() {
            mainView?.also {
                bottleName = it.findViewById(R.id.bottle_name) as TextView
                bottleImage = it.findViewById(R.id.bottle_image) as ImageView
                bottleActive = it.findViewById(R.id.bottle_active) as CheckBox

                bottleActive?.setOnClickListener { action.onNext(Action.bottleToggleActive()) }
            }
        }

        override fun render(previousState: BaseState, state: BaseState) {
            state as BottleState
            if (state.loaded) {
                bottleName?.text = state.name
                bottleImage?.setImageDrawable(ContextCompat.getDrawable(mainView!!.context, state.image))
                bottleActive?.isChecked = state.active
            } else {
                bottleViewModel = BottleViewModel(state.id, bottleFragment.context!!.applicationContext as Application)
                bottleViewModel?.bottle?.observe(bottleFragment, Observer {
                    bottleFragment.changeState(state.copy(
                        loaded = true,
                        name = it.name,
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
                loaded = false
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