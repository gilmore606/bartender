package com.dlfsystems.bartender.fragments

import androidx.fragment.app.Fragment
import com.dlfsystems.bartender.BaseFragment
import com.dlfsystems.bartender.R
import com.dlfsystems.bartender.nav.BaseKey
import kotlinx.android.parcel.Parcelize

class BottleFragment : BaseFragment() {

    data class BottleState(
        val bottleId: Int = 0
    ) : BaseState()

    @Parcelize
    data class BottleKey(val tag: String) : BaseKey() {
        constructor(): this("BottleKey")
        override fun createFragment() = BottleFragment()
    }

    class BottleView(val bottleFragment: Fragment) : BaseViewController() {

        override fun render(previousState: BaseState, state: BaseState) {

        }
    }

    override val layoutResource = R.layout.fragment_bottle
    override val viewController = BottleView(this)
    override fun getDefaultState() = BottleState()
}