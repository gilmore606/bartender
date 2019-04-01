package com.dlfsystems.bartender.fragments

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DiffUtil
import com.dlfsystems.bartender.Action
import com.dlfsystems.bartender.R
import com.dlfsystems.bartender.nav.Rudder
import androidx.recyclerview.widget.RecyclerView
import com.dlfsystems.bartender.room.Bottle
import com.dlfsystems.bartender.room.BottlesViewModel
import com.dlfsystems.bartender.fragments.CatalogFragment.BottleTabs
import com.dlfsystems.bartender.ioThread
import com.dlfsystems.bartender.room.BarDB
import com.dlfsystems.bartender.views
import com.dlfsystems.bartender.views.BottleItemView

import io.reactivex.subjects.PublishSubject

class CatalogBottlesFragment : CatalogListFragment() {

    data class BottlesState(
        val tab: BottleTabs = BottleTabs.ALL,
        val filter: Int = 0
    ) : BaseState()


    class BottleAdapter(val action: PublishSubject<Action>, val context: Context) : PagedListAdapter<Bottle, BottleAdapter.BottleViewHolder>(BottleDiffCallback()) {

        var tab: BottleTabs = BottleTabs.ALL

        class BottleViewHolder(val action: PublishSubject<Action>, val view: View) : RecyclerView.ViewHolder(view) {
            var bottleId: Long = 0

            fun bind(bottle: Bottle?) {
                bottle?.also { (view as BottleItemView).bindBottle(it, action) }
            }
        }

        class BottleDiffCallback : DiffUtil.ItemCallback<Bottle>() {
            override fun areItemsTheSame(oldItem: Bottle, newItem: Bottle): Boolean =
                    oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: Bottle, newItem: Bottle): Boolean =
                    oldItem == newItem
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BottleViewHolder {
            val view = BottleItemView(parent.context)
            view.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT)
            return BottleViewHolder(action, view)
        }

        override fun onBindViewHolder(holder: BottleViewHolder, position: Int) {
            (holder.view as BottleItemView).configureForTab(tab)
            holder.bind(getItem(position))
        }

        fun configureForTab(recyclerView: RecyclerView, newtab: BottleTabs) {
            tab = newtab
            (recyclerView.views.filter { it is BottleItemView } as List<BottleItemView>)
                .forEach { it.configureForTab(tab) }
        }
    }


    class BottlesView(val bottlesFragment: Fragment) : BaseViewController() {

        lateinit var allBottlesViewModel: BottlesViewModel
        lateinit var activeBottlesViewModel: BottlesViewModel
        lateinit var shopBottlesViewModel: BottlesViewModel
        lateinit var bottlesViewModel: BottlesViewModel
        lateinit var recyclerView: RecyclerView
        lateinit var recyclerAdapter: BottleAdapter

        override fun subscribeActions() {
            mainView?.let {
                allBottlesViewModel = ViewModelProviders.of(bottlesFragment).get(BottlesViewModel.All::class.java)
                activeBottlesViewModel = ViewModelProviders.of(bottlesFragment).get(BottlesViewModel.Active::class.java)
                shopBottlesViewModel = ViewModelProviders.of(bottlesFragment).get(BottlesViewModel.Shop::class.java)
                bottlesViewModel = allBottlesViewModel

                recyclerView = it.findViewById(R.id.bottles_recycler) as RecyclerView
                recyclerView.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(it.context)
                recyclerAdapter = BottleAdapter(action, it.context)
                recyclerView.adapter = recyclerAdapter
                recyclerView.itemAnimator = object : DefaultItemAnimator() {
                    override fun onAnimationStarted(holder: RecyclerView.ViewHolder) =
                        (holder.itemView as BottleItemView).configureForTab(recyclerAdapter.tab)
                    override fun onAnimationFinished(holder: RecyclerView.ViewHolder) =
                        (holder.itemView as BottleItemView).configureForTab(recyclerAdapter.tab)
                }
            }
        }

        override fun render(previousState: BaseState?, state: BaseState) {
            state as BottlesState
            previousState as BottlesState?

            if (state.filter != previousState?.filter) {
                ioThread {
                    BarDB.getInstance(bottlesFragment.context!!.applicationContext).filterDao()
                        .set("bottle", state.filter)
                }
            }

            if (state.tab != previousState?.tab) {
                bottlesViewModel.getLiveData().removeObservers(bottlesFragment)
                bottlesViewModel = when (state.tab) {
                    (BottleTabs.ALL) -> { allBottlesViewModel }
                    (BottleTabs.MINE) -> { activeBottlesViewModel }
                    (BottleTabs.SHOP) -> { shopBottlesViewModel }
                    else -> { allBottlesViewModel }
                }
                subscribeLiveData(bottlesViewModel, state)
            }
        }

        private fun subscribeLiveData(viewModel: BottlesViewModel, state: BottlesState) {
            viewModel.getLiveData().observe(bottlesFragment, Observer {
                it?.let {
                    recyclerAdapter.submitList(it)
                    recyclerAdapter.configureForTab(recyclerView, state.tab)
                }
            })
        }
    }


    override val layoutResource = R.layout.fragment_bottles
    override val viewController = BottlesView(this)
    override fun getDefaultState() = BottlesState()

    override val backButtonEnabled = false

    override fun hearAction(action: Action) {
        val state = previousState as BottlesState

        when (action) {
            is Action.navToBottle -> {
                Rudder.navTo(BottleFragment.BottleKey(action.bottleId))
            }
            is Action.bottleToggleActive -> {
                action.bottle?.also {
                    setBottleActive(action.bottle.id, action.bottle.name, action.active ?: true)
                }
            }
            is Action.bottleToggleShopping -> {
                action.bottle?.also {
                    setBottleShopping(action.bottle.id, action.bottle.name, action.shopping ?: true)
                }
            }
            is Action.bottleTab -> {
                changeState(state.copy(
                    tab = action.tab
                ))
            }
            is Action.bottleFilter -> {
                changeState(state.copy(
                    filter = action.filter
                ))
            }
            else -> { }
        }
    }
}