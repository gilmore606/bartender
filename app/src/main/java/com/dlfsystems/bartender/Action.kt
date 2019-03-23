package com.dlfsystems.bartender

import androidx.fragment.app.Fragment
import com.dlfsystems.bartender.fragments.CatalogFragment

sealed class Action {

    class tabTo(val tab: CatalogFragment.Tabs) : Action()

    class navToDrink(val drinkId: Int) : Action()

    class navToBottle(val bottleId: Int) : Action()

}