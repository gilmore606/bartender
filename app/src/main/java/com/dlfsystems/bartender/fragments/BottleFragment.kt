package com.dlfsystems.bartender.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.dlfsystems.bartender.BaseFragment
import com.dlfsystems.bartender.R
import com.dlfsystems.bartender.nav.BaseKey
import kotlinx.android.parcel.Parcelize

class BottleFragment : BaseFragment() {

    data class BottleState(
        val bottleId: Long = 0
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

        override fun render(previousState: BaseState, state: BaseState) {

        }
    }

    override val layoutResource = R.layout.fragment_bottle
    override val viewController = BottleView(this)
    override fun getDefaultState() = BottleState()

    override fun makeStateFromArguments(arguments: Bundle): BaseState =
            BottleState(
                bottleId = arguments.getSerializable("bottleId") as Long
            )
}