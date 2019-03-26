package com.dlfsystems.bartender.fragments

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.dlfsystems.bartender.Action
import com.dlfsystems.bartender.BaseFragment
import com.dlfsystems.bartender.R
import com.dlfsystems.bartender.ioThread
import com.dlfsystems.bartender.nav.BaseKey
import com.dlfsystems.bartender.room.BarDB
import kotlinx.android.parcel.Parcelize

class BottleFragment : BaseFragment() {

    data class BottleState(
        val id: Long = 0,
        val loaded: Boolean = false,
        val name: String = "",
        val image: Int = 0
    ) : BaseState()

    @Parcelize
    data class BottleKey(val bottleId: Long) : BaseKey() {
        override fun createFragment() = BottleFragment().apply {
            arguments = (arguments ?: Bundle()).also {
                it.putSerializable("bottleId", bottleId)
            }
        }
    }

    class BottleView(val bottleFragment: Fragment) : BaseViewController() {

        var bottleName: TextView? = null
        var bottleImage: ImageView? = null

        override fun subscribeActions() {
            mainView?.let {
                bottleName = it.findViewById(R.id.bottle_name) as TextView
                bottleImage = it.findViewById(R.id.bottle_image) as ImageView
            }
        }

        override fun render(previousState: BaseState, state: BaseState) {
            state as BottleState
            if (state.loaded) {
                bottleName?.text = state.name
                bottleImage?.setImageDrawable(ContextCompat.getDrawable(mainView!!.context, state.image))
            } else {
                ioThread {
                    action.onNext(
                        Action.bottleDetailLoaded(
                            BarDB.getInstance(mainView!!.context).bottleDao().byId(state.id)
                        )
                    )
                }
            }
        }
    }

    override val layoutResource = R.layout.fragment_bottle
    override val viewController = BottleView(this)
    override fun getDefaultState() = BottleState()

    override fun makeStateFromArguments(arguments: Bundle): BaseState =
            BottleState(
                id = arguments.getSerializable("bottleId") as Long
            )

    override fun hearAction(action: Action) {
        val state = previousState as BottleState

        when (action) {
            is Action.bottleDetailLoaded -> {
                changeState(state.copy(
                    loaded = true,
                    name = action.bottle.name,
                    image = action.bottle.image
                ))
            }
            else -> { }
        }
    }
}