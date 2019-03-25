package com.dlfsystems.bartender

import androidx.fragment.app.Fragment
import com.dlfsystems.bartender.fragments.CatalogFragment

sealed class Action {

    class tabTo(val tab: CatalogFragment.Tabs) : Action()

    class navToDrink(val drinkId: Long) : Action()

    class navToBottle(val bottleId: Long) : Action()

    class bottleTab(val tab: CatalogFragment.BottleTabs) : Action()

    class drinkTab(val tab: CatalogFragment.DrinkTabs) : Action()
}