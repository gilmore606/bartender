package com.dlfsystems.bartender.fragments

import android.util.Log
import android.view.View
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.dlfsystems.bartender.Action
import com.dlfsystems.bartender.BaseFragment
import com.dlfsystems.bartender.R
import com.dlfsystems.bartender.nav.BaseKey
import com.dlfsystems.bartender.views.MySpinnerAdapter
import io.reactivex.subjects.PublishSubject
import kotlinx.android.parcel.Parcelize

// container fragment for the Bottles and Drinks list subscreens

class CatalogFragment : BaseFragment() {

    enum class Tabs { BOTTLES, DRINKS }
    enum class BottleTabs { MINE, ALL, SHOP }
    enum class DrinkTabs { ALL, FAVORITE, ALPHA }

    data class CatalogState(
        val tab: Tabs = Tabs.BOTTLES,
        val bottleTab: BottleTabs = BottleTabs.MINE,
        val drinkTab: DrinkTabs = DrinkTabs.ALL,
        val bottleFilter: Int = 0,
        val drinkFilter: Int = 0
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

        var buttonBottles: ToggleButton? = null
        var buttonDrinks: ToggleButton? = null
        var spinnerBottles: Spinner? = null
        var spinnerDrinks: Spinner? = null
        var spinnerBottlesFilter: Spinner? = null
        var spinnerDrinksFilter: Spinner? = null

        override fun subscribeActions() {
            mainView?.also {
                buttonBottles = it.findViewById(R.id.catalog_button_bottles) as ToggleButton
                buttonDrinks = it.findViewById(R.id.catalog_button_drinks) as ToggleButton
                spinnerBottles = it.findViewById(R.id.catalog_bottles_spinner) as Spinner
                spinnerDrinks = it.findViewById(R.id.catalog_drinks_spinner) as Spinner
                spinnerBottlesFilter = it.findViewById(R.id.catalog_bottles_filter_spinner) as Spinner
                spinnerDrinksFilter = it.findViewById(R.id.catalog_drinks_filter_spinner) as Spinner

                buttonBottles?.isChecked = true
                buttonBottles?.isClickable = false
                buttonBottles?.setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) action.onNext(Action.tabTo(Tabs.BOTTLES))
                }
                buttonDrinks?.setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) action.onNext(Action.tabTo(Tabs.DRINKS))
                }

                spinnerBottles?.also {
                    val adapter = MySpinnerAdapter(mainView!!.context,
                        R.layout.spinner_item,
                        catalogFragment.resources.getStringArray(R.array.bottle_spin_array))
                    adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
                    it.adapter = adapter
                    it.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(parent: AdapterView<*>, view: View, pos: Int, id: Long) {
                            (parent.adapter as MySpinnerAdapter).setSelection(pos)
                            val tabaction = Action.bottleTab(
                                when (pos) {
                                    0 -> { BottleTabs.MINE }
                                    1 -> { BottleTabs.ALL }
                                    2 -> { BottleTabs.SHOP }
                                    else -> { BottleTabs.ALL }
                                })
                            action.onNext(tabaction)
                            bottleAction.onNext(tabaction)
                        }
                        override fun onNothingSelected(parent: AdapterView<*>) { }
                    }
                }

                spinnerBottlesFilter?.also {
                    val adapter = MySpinnerAdapter(mainView!!.context,
                        R.layout.spinner_item,
                        catalogFragment.resources.getStringArray(R.array.bottle_filter_array))
                    adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
                    it.adapter = adapter
                    it.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(parent: AdapterView<*>, view: View, pos: Int, id: Long) {
                            (parent.adapter as MySpinnerAdapter).setSelection(pos)
                            val filterAction = Action.bottleFilter(pos)
                            action.onNext(filterAction)
                            bottleAction.onNext(filterAction)
                        }
                        override fun onNothingSelected(parent: AdapterView<*>) { }
                    }
                }

                spinnerDrinks?.also {
                    val adapter = MySpinnerAdapter(mainView!!.context,
                        R.layout.spinner_item,
                        catalogFragment.resources.getStringArray(R.array.drink_spin_array))
                    adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
                    it.adapter = adapter
                    it.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(parent: AdapterView<*>, view: View, pos: Int, id: Long) {
                            (parent.adapter as MySpinnerAdapter).setSelection(pos)
                            val tabaction = Action.drinkTab(
                                when (pos) {
                                    0 -> { DrinkTabs.FAVORITE }
                                    1 -> { DrinkTabs.ALL }
                                    2 -> { DrinkTabs.ALPHA }
                                    else -> { DrinkTabs.ALL }
                                })
                            action.onNext(tabaction)
                            drinkAction.onNext(tabaction)
                        }
                        override fun onNothingSelected(parent: AdapterView<*>) { }
                    }
                }

                spinnerDrinksFilter?.also {
                    val adapter = MySpinnerAdapter(mainView!!.context,
                        R.layout.spinner_item,
                        catalogFragment.resources.getStringArray(R.array.drink_filter_array))
                    adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
                    it.adapter = adapter
                    it.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(parent: AdapterView<*>, view: View, pos: Int, id: Long) {
                            (parent.adapter as MySpinnerAdapter).setSelection(pos)
                            val filterAction = Action.drinkFilter(pos)
                            action.onNext(filterAction)
                            drinkAction.onNext(filterAction)
                        }
                        override fun onNothingSelected(parent: AdapterView<*>) { }
                    }
                }
            }
        }

        override fun render(previousState: BaseState?, state: BaseState) {
            state as CatalogState
            previousState as CatalogState?

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
                Tabs.BOTTLES -> {
                    buttonDrinks?.isChecked = false
                    buttonDrinks?.isClickable = true
                    buttonDrinks?.setBackgroundResource(R.drawable.bg_iconbar)
                    buttonBottles?.isClickable = false
                    buttonBottles?.setBackgroundResource(R.drawable.bg_iconbar_lit)
                }
                Tabs.DRINKS -> {
                    buttonBottles?.isChecked = false
                    buttonBottles?.isClickable = true
                    buttonBottles?.setBackgroundResource(R.drawable.bg_iconbar)
                    buttonDrinks?.isClickable = false
                    buttonDrinks?.setBackgroundResource(R.drawable.bg_iconbar_lit)
                }
            }

            spinnerBottles?.setSelection(when (state.bottleTab) {
                BottleTabs.MINE -> { 0 }
                BottleTabs.ALL -> { 1 }
                BottleTabs.SHOP -> { 2 }
            })
            spinnerBottlesFilter?.setSelection(state.bottleFilter)

            spinnerDrinks?.setSelection(when (state.drinkTab) {
                DrinkTabs.FAVORITE -> { 0 }
                DrinkTabs.ALL -> { 1 }
                DrinkTabs.ALPHA -> { 2 }
            })
            spinnerDrinksFilter?.setSelection(state.drinkFilter)

            spinnerBottles?.visibility = if (state.tab == Tabs.BOTTLES) View.VISIBLE else View.GONE
            spinnerBottlesFilter?.visibility = if (state.tab == Tabs.BOTTLES) View.VISIBLE else View.GONE
            spinnerDrinks?.visibility = if (state.tab == Tabs.DRINKS) View.VISIBLE else View.GONE
            spinnerDrinksFilter?.visibility = if (state.tab == Tabs.DRINKS) View.VISIBLE else View.GONE

            if (state.tab != (previousState?.tab ?: state.tab)) {
                val animIn = if (state.tab == Tabs.DRINKS) R.anim.slide_in_left else R.anim.slide_in_right
                val animOut = if (state.tab == Tabs.DRINKS) R.anim.slide_out_left else R.anim.slide_out_right
                catalogFragment.activity?.supportFragmentManager?.beginTransaction()?.disallowAddToBackStack()?.apply {
                    setCustomAnimations(animIn, animOut)
                    previousState?.also { hide(fragmentForTab(previousState.tab)) }
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

    override val backButtonEnabled = false

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
            is Action.bottleFilter -> {
                changeState(state.copy(
                    bottleFilter = action.filter
                ))
            }
            is Action.drinkFilter -> {
                changeState(state.copy(
                    drinkFilter = action.filter
                ))
            }
            else -> { }
        }
    }
}