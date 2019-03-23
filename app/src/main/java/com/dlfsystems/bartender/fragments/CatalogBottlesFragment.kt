package com.dlfsystems.bartender.fragments

import android.util.Log
import android.widget.Button
import androidx.fragment.app.Fragment
import com.dlfsystems.bartender.Action
import com.dlfsystems.bartender.R
import com.dlfsystems.bartender.nav.Rudder

class CatalogBottlesFragment : CatalogListFragment() {

    data class BottlesState(
        val bottles: ArrayList<Int> = ArrayList(0)
    ) : BaseState()

    class BottlesView(val bottlesFragment: Fragment) : BaseViewController() {

        override fun subscribeActions() {
            mainView?.let {
                (it.findViewById(R.id.bottles_bottle_button) as Button).setOnClickListener {
                    action.onNext(Action.navToBottle(1))
                }
            }
        }
    }


    override val layoutResource = R.layout.fragment_bottles
    override val viewController = BottlesView(this)
    override fun getDefaultState() = BottlesState()

    override fun hearAction(action: Action) {
        val state = previousState as BottlesState

        when (action) {
            is Action.navToBottle -> {
                Rudder.navTo(BottleFragment.BottleKey())
            }
            else -> { }
        }
    }
}