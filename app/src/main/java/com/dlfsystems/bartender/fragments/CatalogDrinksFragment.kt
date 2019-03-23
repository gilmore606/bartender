package com.dlfsystems.bartender.fragments

import androidx.fragment.app.Fragment
import com.dlfsystems.bartender.R

class CatalogDrinksFragment : CatalogListFragment() {

    data class DrinksState(
        val drinks: ArrayList<Int> = ArrayList(0)
    ) : BaseState()

    class DrinksView(val drinksFragment: Fragment) : BaseViewController() {

    }


    override val layoutResource = R.layout.fragment_drinks
    override val viewController = DrinksView(this)
    override fun getDefaultState() = DrinksState()

}