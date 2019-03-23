package com.dlfsystems.bartender.fragments

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import com.dlfsystems.bartender.Action
import com.dlfsystems.bartender.BaseFragment
import com.dlfsystems.bartender.MainActivity
import com.dlfsystems.bartender.R
import com.dlfsystems.bartender.nav.BaseKey
import kotlinx.android.parcel.Parcelize

// container fragment for the Bottles and Drinks list subscreens

class CatalogFragment : BaseFragment() {

    enum class Tabs { BOTTLES, DRINKS }

    data class CatalogState(
        val tab: Tabs = Tabs.BOTTLES
    ) : BaseState()


    @Parcelize
    data class CatalogKey(val tag: String) : BaseKey() {
        constructor(): this("CatalogKey")
        override fun createFragment() = CatalogFragment()
    }


    class CatalogView(val catalogFragment: Fragment) : BaseViewController() {

        val bottlesFragment = CatalogBottlesFragment()
        val drinksFragment = CatalogDrinksFragment()
        var attachedSubFragments = false

        override fun subscribeActions() {
            mainView?.let {
                (it.findViewById(R.id.catalog_button_bottles) as Button).setOnClickListener {
                    action.onNext(Action.tabTo(Tabs.BOTTLES))
                }
                (it.findViewById(R.id.catalog_button_drinks) as Button).setOnClickListener {
                    action.onNext(Action.tabTo(Tabs.DRINKS))
                }
            }
        }

        override fun render(previousState: BaseState, state: BaseState) {
            state as CatalogState
            previousState as CatalogState

            if (!attachedSubFragments) {
                catalogFragment.activity?.supportFragmentManager?.beginTransaction()?.disallowAddToBackStack()?.apply {
                    add(R.id.catalog_pane, bottlesFragment, "bottles")
                    add(R.id.catalog_pane, drinksFragment, "drinks")
                    hide(drinksFragment)
                    show(bottlesFragment)
                }?.commitNow()
                attachedSubFragments = true
            }

            if (state.tab != previousState.tab) {
                catalogFragment.activity?.supportFragmentManager?.beginTransaction()?.disallowAddToBackStack()?.apply {
                    hide(fragmentForTab(previousState.tab))
                    show(fragmentForTab(state.tab))
                }?.commitNow()
            }
        }

        private fun fragmentForTab(tab: Tabs) : Fragment {
            return when (tab) {
                Tabs.BOTTLES -> { bottlesFragment }
                Tabs.DRINKS -> { drinksFragment }
            }
        }
    }


    override val layoutResource = R.layout.fragment_catalog
    override val viewController = CatalogView(this)
    override fun getDefaultState() = CatalogState()

    override fun hearAction(action: Action) {
        val state = previousState as CatalogState

        when (action) {
            is Action.tabTo -> {
                changeState(state.copy(
                    tab = action.tab
                ))
            }
            else -> { }
        }
    }
}