package com.dlfsystems.bartender

import androidx.fragment.app.Fragment
import com.dlfsystems.bartender.fragments.CatalogFragment
import com.dlfsystems.bartender.room.*

sealed class Action {

    class tabTo(val tab: CatalogFragment.Tabs) : Action()

    class navToDrink(val drinkId: Long) : Action()
    class navToBottle(val bottleId: Long) : Action()

    class bottleTab(val tab: CatalogFragment.BottleTabs) : Action()
    class drinkTab(val tab: CatalogFragment.DrinkTabs) : Action()

    class bottleFilter(val filter: Int): Action()
    class drinkFilter(val filter: Int): Action()

    class bottlesActiveChanged(val count: Int): Action()

    class catalogHiddenChanged(val hidden: Boolean): Action()

    class bottleToggleActive(val bottle: Bottle? = null, val active: Boolean? = null) : Action()
    class bottleToggleShopping(val bottle: Bottle? = null, val shopping: Boolean? = null): Action()

    class bottleLoad(val load: Bottle): Action()
    class bottleLoadDrinks(val load: List<Drink>): Action()
    class bottleLoadTags(val load: List<Family>): Action()

    class drinkToggleFavorite(val drink: Drink? = null, val favorite: Boolean? = null): Action()

    class drinkLoad(val load: Drink): Action()
    class drinkLoadIngredients(val load: List<Ingredient>): Action()
    class drinkLoadTags(val load: List<Drinktag>): Action()
    class drinkLoadGlass(val load: Glass): Action()
}