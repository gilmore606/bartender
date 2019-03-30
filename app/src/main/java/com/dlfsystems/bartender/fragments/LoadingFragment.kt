package com.dlfsystems.bartender.fragments

import androidx.fragment.app.Fragment
import com.dlfsystems.bartender.Action
import com.dlfsystems.bartender.BaseFragment
import com.dlfsystems.bartender.R
import com.dlfsystems.bartender.nav.BaseKey
import kotlinx.android.parcel.Parcelize

class LoadingFragment : BaseFragment() {

    data class LoadingState(
        val dummy: Boolean = false
    ) : BaseState()

    @Parcelize
    data class LoadingKey(val tag: String) : BaseKey() {
        constructor(): this("LoadingKey")
        override fun createFragment() = LoadingFragment()
    }

    class LoadingView(val loadingFragment: Fragment) : BaseViewController() {

        override fun subscribeActions() {

        }

        override fun render(previousState: BaseState?, state: BaseState) {

        }
    }

    override val layoutResource = R.layout.fragment_loading
    override val viewController = LoadingView(this)
    override fun getDefaultState() = LoadingState()
    override val backButtonEnabled = false

    override fun hearAction(action: Action) {

    }

}