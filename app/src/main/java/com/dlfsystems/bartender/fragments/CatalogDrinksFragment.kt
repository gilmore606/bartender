package com.dlfsystems.bartender.fragments

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import com.dlfsystems.bartender.Action
import com.dlfsystems.bartender.R
import com.dlfsystems.bartender.nav.Rudder
import androidx.recyclerview.widget.RecyclerView
import com.dlfsystems.bartender.room.BarDB
import com.dlfsystems.bartender.room.Drink
import com.dlfsystems.bartender.room.DrinksViewModel
import com.dlfsystems.bartender.fragments.CatalogFragment.DrinkTabs

class CatalogDrinksFragment : CatalogListFragment() {

    data class DrinksState(
        val tab: DrinkTabs = DrinkTabs.ALL
    ) : BaseState()

    class DrinkAdapter(val context: Context): PagedListAdapter<Drink, DrinkAdapter.DrinkViewHolder>(DrinkDiffCallback()) {

        class DrinkViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
            val drinkName = view.findViewById(R.id.item_drink_name) as TextView
            val drinkImage = view.findViewById(R.id.item_drink_image) as ImageView
            val drinkFavorite = view.findViewById(R.id.item_drink_favorite_checkbox) as CheckBox

            fun bind(drink: Drink?) {
                drinkName.text = drink?.name ?: ""
                drinkFavorite.setOnCheckedChangeListener { _,_ -> }
                drinkFavorite.isChecked = drink?.favorite ?: false
                drinkFavorite.setOnCheckedChangeListener { _, isChecked ->
                    BarDB.setDrinkFavorite(view.context, drink?.id ?: 0, isChecked)
                }
            }
        }

        class DrinkDiffCallback : DiffUtil.ItemCallback<Drink>() {
            override fun areItemsTheSame(oldItem: Drink, newItem: Drink): Boolean =
                    oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: Drink, newItem: Drink): Boolean =
                    oldItem == newItem
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DrinkViewHolder {
            return DrinkViewHolder(LayoutInflater.from(context).inflate(R.layout.item_drink, parent, false))
        }

        override fun onBindViewHolder(holder: DrinkViewHolder, position: Int) {
            holder.bind(getItem(position))
        }
    }


    class DrinksView(val drinksFragment: Fragment) : BaseViewController() {

        lateinit var allDrinksViewModel: DrinksViewModel
        lateinit var favoriteDrinksViewModel: DrinksViewModel
        lateinit var drinksViewModel: DrinksViewModel
        lateinit var recyclerView: RecyclerView
        lateinit var recyclerAdapter: DrinkAdapter

        override fun subscribeActions() {
            mainView?.let {
                allDrinksViewModel = ViewModelProviders.of(drinksFragment).get(DrinksViewModel.All::class.java)
                favoriteDrinksViewModel = ViewModelProviders.of(drinksFragment).get(DrinksViewModel.Favorites::class.java)
                drinksViewModel = allDrinksViewModel

                recyclerView = it.findViewById(R.id.drinks_recycler) as RecyclerView
                recyclerView.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(it.context)
                recyclerAdapter = DrinkAdapter(it.context)
                recyclerView.adapter = recyclerAdapter
                subscribeLiveData(drinksViewModel)
            }
        }

        override fun render(previousState: BaseState, state: BaseState) {
            state as DrinksState
            previousState as DrinksState
            if (state.tab != previousState.tab) {
                drinksViewModel.getLiveData().removeObservers(drinksFragment)
                drinksViewModel = when (state.tab) {
                    (DrinkTabs.ALL) -> { allDrinksViewModel }
                    (DrinkTabs.FAVORITE) -> { favoriteDrinksViewModel }
                    else -> { allDrinksViewModel }
                }
                subscribeLiveData(drinksViewModel)
            }
        }

        private fun subscribeLiveData(viewModel: DrinksViewModel) {
            viewModel.getLiveData().observe(drinksFragment, Observer {
                it?.let { recyclerAdapter.submitList(it) }
            })
        }
    }


    override val layoutResource = R.layout.fragment_drinks
    override val viewController = DrinksView(this)
    override fun getDefaultState() = DrinksState()

    override fun hearAction(action: Action) {
        val state = previousState as DrinksState

        when (action) {
            is Action.drinkTab -> {
                changeState(state.copy(
                    tab = action.tab
                ))
            }
            else -> { }
        }
    }
}