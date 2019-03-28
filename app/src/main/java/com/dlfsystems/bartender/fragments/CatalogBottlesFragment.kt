package com.dlfsystems.bartender.fragments

import android.content.Context
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
import com.dlfsystems.bartender.room.BarDB
import com.dlfsystems.bartender.room.Bottle
import com.dlfsystems.bartender.room.BottlesViewModel
import com.dlfsystems.bartender.fragments.CatalogFragment.BottleTabs

import io.reactivex.subjects.PublishSubject

class CatalogBottlesFragment : CatalogListFragment() {

    data class BottlesState(
        val tab: BottleTabs = BottleTabs.ALL
    ) : BaseState()


    class BottleAdapter(val action: PublishSubject<Action>, val context: Context) : PagedListAdapter<Bottle, BottleAdapter.BottleViewHolder>(BottleDiffCallback()) {

        class BottleViewHolder(val action: PublishSubject<Action>, val view: View) : RecyclerView.ViewHolder(view) {
            val bottleName = view.findViewById(R.id.item_bottle_name) as TextView
            val bottleImage = view.findViewById(R.id.item_bottle_image) as ImageView
            val bottleOwned = view.findViewById(R.id.item_bottle_owned_checkbox) as CheckBox
            var bottleId: Long = 0

            init {
                view.setOnClickListener {
                    action.onNext(Action.navToBottle(bottleId))
                }
            }
            fun bind(bottle: Bottle?) {
                bottleId = bottle?.id ?: 0
                bottleName.text = bottle?.name ?: ""
                bottleImage.setImageDrawable(ContextCompat.getDrawable(view.context, bottle?.image ?: 0))
                bottleOwned.setOnCheckedChangeListener { _,_ -> }
                bottleOwned.isChecked = bottle?.active ?: false
                bottleOwned.setOnCheckedChangeListener { _, isChecked ->
                    action.onNext(Action.bottleToggleActive(bottle, isChecked))
                }
            }
        }

        class BottleDiffCallback : DiffUtil.ItemCallback<Bottle>() {
            override fun areItemsTheSame(oldItem: Bottle, newItem: Bottle): Boolean =
                    oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: Bottle, newItem: Bottle): Boolean =
                    oldItem == newItem
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BottleViewHolder {
            return BottleViewHolder(action, LayoutInflater.from(context).inflate(R.layout.item_bottle, parent, false))
        }

        override fun onBindViewHolder(holder: BottleViewHolder, position: Int) {
            holder.bind(getItem(position))
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
                subscribeLiveData(bottlesViewModel)
            }
        }

        override fun render(previousState: BaseState, state: BaseState) {
            state as BottlesState
            previousState as BottlesState
            if (state.tab != previousState.tab) {
                bottlesViewModel.getLiveData().removeObservers(bottlesFragment)
                bottlesViewModel = when (state.tab) {
                    (BottleTabs.ALL) -> { allBottlesViewModel }
                    (BottleTabs.MINE) -> { activeBottlesViewModel }
                    (BottleTabs.SHOP) -> { shopBottlesViewModel }
                    else -> { allBottlesViewModel }
                }
                subscribeLiveData(bottlesViewModel)
            }
        }

        private fun subscribeLiveData(viewModel: BottlesViewModel) {
            viewModel.getLiveData().observe(bottlesFragment, Observer {
                it?.let { recyclerAdapter.submitList(it) }
            })
        }
    }


    override val layoutResource = R.layout.fragment_bottles
    override val viewController = BottlesView(this)
    override fun getDefaultState() = BottlesState()

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
            is Action.bottleTab -> {
                changeState(state.copy(
                    tab = action.tab
                ))
            }
            else -> { }
        }
    }
}