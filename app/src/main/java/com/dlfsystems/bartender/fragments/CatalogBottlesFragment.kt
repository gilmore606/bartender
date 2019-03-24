package com.dlfsystems.bartender.fragments

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import com.dlfsystems.bartender.Action
import com.dlfsystems.bartender.R
import com.dlfsystems.bartender.nav.Rudder
import androidx.recyclerview.widget.RecyclerView
import com.dlfsystems.bartender.room.Bottle
import com.dlfsystems.bartender.room.BottlesViewModel

class CatalogBottlesFragment : CatalogListFragment() {

    data class BottlesState(
        val bottles: ArrayList<Int> = ArrayList(0)
    ) : BaseState()


    class BottleAdapter(val context: Context) : PagedListAdapter<Bottle, BottleAdapter.BottleViewHolder>(BottleDiffCallback()) {

        class BottleViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val bottleName = view.findViewById(R.id.item_bottle_name) as TextView
            fun bind(bottle: Bottle?) {
                bottleName.text = bottle?.name ?: ""
            }
        }

        class BottleDiffCallback : DiffUtil.ItemCallback<Bottle>() {
            override fun areItemsTheSame(oldItem: Bottle, newItem: Bottle): Boolean =
                    oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: Bottle, newItem: Bottle): Boolean =
                    oldItem == newItem
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BottleViewHolder {
            return BottleViewHolder(LayoutInflater.from(context).inflate(R.layout.item_bottle, parent, false))
        }

        override fun onBindViewHolder(holder: BottleViewHolder, position: Int) {
            holder.bind(getItem(position))
        }
    }


    class BottlesView(val bottlesFragment: Fragment) : BaseViewController() {

        lateinit var bottlesViewModel: BottlesViewModel
        lateinit var recyclerView: RecyclerView
        lateinit var recyclerAdapter: BottleAdapter

        override fun subscribeActions() {
            mainView?.let {
                bottlesViewModel = ViewModelProviders.of(bottlesFragment).get(BottlesViewModel::class.java)
                recyclerView = it.findViewById(R.id.bottles_recycler) as RecyclerView
                recyclerView.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(it.context)
                recyclerAdapter = BottleAdapter(it.context)
                recyclerView.adapter = recyclerAdapter
                subscribeLiveData(recyclerAdapter)
            }
        }

        private fun subscribeLiveData(adapter: BottleAdapter) {
            bottlesViewModel.getBottlesLiveData().observe(bottlesFragment, Observer {
                it?.let { adapter.submitList(it) }
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
                Rudder.navTo(BottleFragment.BottleKey())
            }
            else -> { }
        }
    }
}