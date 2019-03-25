package com.dlfsystems.bartender.fragments

import android.view.View
import android.widget.Button
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.dlfsystems.bartender.Action
import com.dlfsystems.bartender.BaseFragment
import com.dlfsystems.bartender.R
import com.dlfsystems.bartender.nav.BaseKey
import io.reactivex.subjects.PublishSubject
import kotlinx.android.parcel.Parcelize

// container fragment for the Bottles and Drinks list subscreens

class CatalogFragment : BaseFragment() {

    enum class Tabs { BOTTLES, DRINKS }
    enum class BottleTabs { MINE, ALL, SHOP }
    enum class DrinkTabs { ALL, FAVORITE }

    data class CatalogState(
        val tab: Tabs = Tabs.BOTTLES,
        val bottleTab: BottleTabs = BottleTabs.ALL,
        val drinkTab: DrinkTabs = DrinkTabs.ALL
    ) : BaseState()


    @Parcelize
    data class CatalogKey(val tag: String) : BaseKey() {
        constructor(): this("CatalogKey")
        override fun createFragment() = CatalogFragment()
    }


    class CatalogView(val catalogFragment: Fragment) : BaseViewController() {

        var bottleAction = PublishSubject.create<Action>()
        var drinkAction = PublishSubject.create<Action>()

        val bottlesFragment = CatalogBottlesFragment().also { it.parentAction = bottleAction }
        val drinksFragment = CatalogDrinksFragment().also { it.parentAction = drinkAction }

        var attachedSubFragments = false

        var buttonBottles: Button? = null
        var buttonDrinks: Button? = null
        var buttonBottlesMine: Button? = null
        var buttonBottlesAll: Button? = null
        var buttonDrinksAll: Button? = null
        var buttonDrinksFavorites: Button? = null



        override fun subscribeActions() {
            mainView?.let {
                buttonBottles = it.findViewById(R.id.catalog_button_bottles) as Button
                buttonDrinks = it.findViewById(R.id.catalog_button_drinks) as Button
                buttonBottlesMine = it.findViewById(R.id.catalog_button_bottles_mine) as Button
                buttonBottlesAll = it.findViewById(R.id.catalog_button_bottles_add) as Button
                buttonDrinksAll = it.findViewById(R.id.catalog_button_drinks_all) as Button
                buttonDrinksFavorites = it.findViewById(R.id.catalog_button_drinks_favorites) as Button

                buttonBottles?.setOnClickListener {
                    action.onNext(Action.tabTo(Tabs.BOTTLES))
                }
                buttonDrinks?.setOnClickListener {
                    action.onNext(Action.tabTo(Tabs.DRINKS))
                }
                buttonBottlesAll?.setOnClickListener {
                    action.onNext(Action.bottleTab(BottleTabs.ALL))
                    bottleAction.onNext(Action.bottleTab(BottleTabs.ALL))
                }
                buttonBottlesMine?.setOnClickListener {
                    action.onNext(Action.bottleTab(BottleTabs.MINE))
                    bottleAction.onNext(Action.bottleTab(BottleTabs.MINE))
                }
                buttonDrinksAll?.setOnClickListener {
                    action.onNext(Action.drinkTab(DrinkTabs.ALL))
                    drinkAction.onNext(Action.drinkTab(DrinkTabs.ALL))
                }
                buttonDrinksFavorites?.setOnClickListener {
                    action.onNext(Action.drinkTab(DrinkTabs.FAVORITE))
                    drinkAction.onNext(Action.drinkTab(DrinkTabs.FAVORITE))
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

            tintButton(buttonBottles, state.tab == Tabs.BOTTLES)
            tintButton(buttonDrinks, state.tab == Tabs.DRINKS)
            tintButton(buttonBottlesAll, state.bottleTab == BottleTabs.ALL)
            tintButton(buttonBottlesMine, state.bottleTab == BottleTabs.MINE)
            tintButton(buttonDrinksAll, state.drinkTab == DrinkTabs.ALL)
            tintButton(buttonDrinksFavorites, state.drinkTab == DrinkTabs.FAVORITE)

            buttonBottlesAll?.visibility = if (state.tab == Tabs.BOTTLES) View.VISIBLE else View.GONE
            buttonBottlesMine?.visibility = buttonBottlesAll?.visibility ?: View.GONE
            buttonDrinksAll?.visibility = if (state.tab == Tabs.DRINKS) View.VISIBLE else View.GONE
            buttonDrinksFavorites?.visibility = buttonDrinksAll?.visibility ?: View.GONE

            if (state.tab != previousState.tab) {
                catalogFragment.activity?.supportFragmentManager?.beginTransaction()?.disallowAddToBackStack()?.apply {
                    hide(fragmentForTab(previousState.tab))
                    show(fragmentForTab(state.tab))
                }?.commitNow()
            }
        }

        private fun tintButton(button: Button?, lit: Boolean) {
            button?.background = ContextCompat.getDrawable(mainView!!.context,
                if (lit) R.drawable.button_background_lit
                else R.drawable.button_background_unlit)
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
            is Action.bottleTab -> {
                changeState(state.copy(
                    bottleTab = action.tab
                ))
            }
            is Action.drinkTab -> {
                changeState(state.copy(
                    drinkTab = action.tab
                ))
            }
            else -> { }
        }
    }
}