package com.dlfsystems.bartender.fragments

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
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

        var buttonBottles: Button? = null
        var buttonDrinks: Button? = null
        var buttonBottlesMine: Button? = null
        var buttonBottlesAdd: Button? = null
        var buttonDrinksAll: Button? = null
        var buttonDrinksFavorites: Button? = null

        override fun subscribeActions() {
            mainView?.let {
                buttonBottles = it.findViewById(R.id.catalog_button_bottles) as Button
                buttonDrinks = it.findViewById(R.id.catalog_button_drinks) as Button
                buttonBottlesMine = it.findViewById(R.id.catalog_button_bottles_mine) as Button
                buttonBottlesAdd = it.findViewById(R.id.catalog_button_bottles_add) as Button
                buttonDrinksAll = it.findViewById(R.id.catalog_button_drinks_all) as Button
                buttonDrinksFavorites = it.findViewById(R.id.catalog_button_drinks_favorites) as Button

                buttonBottles?.setOnClickListener {
                    action.onNext(Action.tabTo(Tabs.BOTTLES))
                }
                buttonDrinks?.setOnClickListener {
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

            when (state.tab) {
                (Tabs.BOTTLES) -> {
                    mainView?.let {
                        buttonBottles?.background = ContextCompat.getDrawable(mainView!!.context, R.drawable.button_background_lit)
                        buttonDrinks?.background = ContextCompat.getDrawable(mainView!!.context, R.drawable.button_background_unlit)
                        buttonBottlesAdd?.visibility = View.VISIBLE
                        buttonBottlesMine?.visibility = View.VISIBLE
                        buttonDrinksAll?.visibility = View.GONE
                        buttonDrinksFavorites?.visibility = View.GONE
                    }
                }
                (Tabs.DRINKS) -> {
                    mainView?.let {
                        buttonBottles?.background = ContextCompat.getDrawable(mainView!!.context, R.drawable.button_background_unlit)
                        buttonDrinks?.background = ContextCompat.getDrawable(mainView!!.context, R.drawable.button_background_lit)
                        buttonBottlesAdd?.visibility = View.GONE
                        buttonBottlesMine?.visibility = View.GONE
                        buttonDrinksAll?.visibility = View.VISIBLE
                        buttonDrinksFavorites?.visibility = View.VISIBLE
                    }
                }
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