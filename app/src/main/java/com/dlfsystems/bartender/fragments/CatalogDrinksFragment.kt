package com.dlfsystems.bartender.fragments

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import com.dlfsystems.bartender.Action
import com.dlfsystems.bartender.R
import com.dlfsystems.bartender.nav.Rudder
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.dlfsystems.bartender.room.BarDB
import com.dlfsystems.bartender.room.Drink
import com.dlfsystems.bartender.room.DrinksViewModel
import com.dlfsystems.bartender.fragments.CatalogFragment.DrinkTabs
import com.dlfsystems.bartender.ioThread
import io.reactivex.subjects.PublishSubject

class CatalogDrinksFragment : CatalogListFragment() {

    data class DrinksState(
        val tab: DrinkTabs = DrinkTabs.ALL,
        val filter: Int = 0
    ) : BaseState()

    class DrinkAdapter(val action: PublishSubject<Action>, val context: Context): PagedListAdapter<Drink, DrinkAdapter.DrinkViewHolder>(DrinkDiffCallback()) {

        class DrinkViewHolder(val action: PublishSubject<Action>, val view: View) : RecyclerView.ViewHolder(view) {
            val drinkName = view.findViewById(R.id.item_drink_name) as TextView
            val drinkImage = view.findViewById(R.id.item_drink_image) as ImageView
            val drinkMissing = view.findViewById(R.id.item_drink_missing) as TextView
            val drinkFavorite = view.findViewById(R.id.item_drink_favorite_checkbox) as CheckBox
            var drinkId: Long = 0

            init {
                view.setOnClickListener {
                    action.onNext(Action.navToDrink(drinkId))
                }
            }
            fun bind(drink: Drink?) {
                drinkId = drink?.id ?: 0
                drinkName.text = drink?.name ?: ""
                drink?.also {
                    if (it.image != "")
                        Glide.with(view.context).load(Uri.parse("file:///android_asset/drink_thumb/" + it.image + ".jpg"))
                            .asBitmap().into(drinkImage)
                }
                drinkFavorite.setOnCheckedChangeListener { _,_ -> }
                drinkFavorite.isChecked = drink?.favorite ?: false
                drinkFavorite.setOnCheckedChangeListener { _, isChecked ->
                    action.onNext(Action.drinkToggleFavorite(drink, isChecked))
                }
                if (drink?.missingBottles ?: 0 > 0) {
                    drinkMissing.text = "need " + drink!!.missingBottles.toString()
                    view.setBackgroundResource(R.drawable.bg_listitem_inactive)
                } else {
                    drinkMissing.text = ""
                    view.setBackgroundResource(R.drawable.bg_listitem_active)
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
            return DrinkViewHolder(action, LayoutInflater.from(context).inflate(R.layout.item_drink, parent, false))
        }

        override fun onBindViewHolder(holder: DrinkViewHolder, position: Int) {
            holder.bind(getItem(position))
        }
    }


    class DrinksView(val drinksFragment: Fragment) : BaseViewController() {

        lateinit var allDrinksViewModel: DrinksViewModel
        lateinit var favoriteDrinksViewModel: DrinksViewModel
        lateinit var alphaDrinksViewModel: DrinksViewModel
        lateinit var drinksViewModel: DrinksViewModel
        lateinit var recyclerView: RecyclerView
        lateinit var recyclerAdapter: DrinkAdapter

        override fun subscribeActions() {
            mainView?.let {
                allDrinksViewModel = ViewModelProviders.of(drinksFragment).get(DrinksViewModel.All::class.java)
                favoriteDrinksViewModel = ViewModelProviders.of(drinksFragment).get(DrinksViewModel.Favorites::class.java)
                alphaDrinksViewModel = ViewModelProviders.of(drinksFragment).get(DrinksViewModel.Alpha::class.java)
                drinksViewModel = allDrinksViewModel

                recyclerView = it.findViewById(R.id.drinks_recycler) as RecyclerView
                recyclerView.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(it.context)
                recyclerAdapter = DrinkAdapter(action, it.context)
                recyclerView.adapter = recyclerAdapter
                subscribeLiveData(drinksViewModel)
            }
        }

        override fun render(previousState: BaseState?, state: BaseState) {
            state as DrinksState
            previousState as DrinksState?
            if (state.filter != previousState?.filter) {
                ioThread {
                    BarDB.getInstance(drinksFragment.context!!.applicationContext).filterDao()
                        .set("drink", state.filter)
                }
            }
            if (state.tab != previousState?.tab) {
                drinksViewModel.getLiveData().removeObservers(drinksFragment)
                drinksViewModel = when (state.tab) {
                    (DrinkTabs.ALL) -> { allDrinksViewModel }
                    (DrinkTabs.FAVORITE) -> { favoriteDrinksViewModel }
                    (DrinkTabs.ALPHA) -> { alphaDrinksViewModel }
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

    override val backButtonEnabled = false

    override fun hearAction(action: Action) {
        val state = previousState as DrinksState

        when (action) {
            is Action.navToDrink -> {
                Rudder.navTo(DrinkFragment.DrinkKey(action.drinkId))
            }
            is Action.drinkToggleFavorite -> {
                action.drink?.also {
                    setDrinkFavorite(action.drink.id, action.drink.name, action.favorite ?: true)
                }
            }
            is Action.drinkTab -> {
                changeState(state.copy(
                    tab = action.tab
                ))
            }
            is Action.drinkFilter -> {
                changeState(state.copy(
                    filter = action.filter
                ))
            }
            else -> { }
        }
    }
}