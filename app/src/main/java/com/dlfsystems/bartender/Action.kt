package com.dlfsystems.bartender

import androidx.fragment.app.Fragment
import com.dlfsystems.bartender.fragments.CatalogFragment
import com.dlfsystems.bartender.room.Bottle
import com.dlfsystems.bartender.room.Drink

sealed class Action {

    class tabTo(val tab: CatalogFragment.Tabs) : Action()

    class navToDrink(val drinkId: Long) : Action()

    class navToBottle(val bottleId: Long) : Action()

    class bottleTab(val tab: CatalogFragment.BottleTabs) : Action()

    class drinkTab(val tab: CatalogFragment.DrinkTabs) : Action()

    class bottleToggleActive() : Action()
    class bottleToggleShopping(): Action()

    class bottleLoad(val load: Bottle): Action()
    class bottleLoadDrinks(val load: List<Drink>): Action()

    class drinkToggleFavorite(): Action()
    class drinkLoad(val load: Drink): Action()
    class drinkLoadBottles(val load: List<Bottle>): Action()
}