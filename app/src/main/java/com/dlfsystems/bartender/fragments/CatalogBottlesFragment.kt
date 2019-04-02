package com.dlfsystems.bartender.fragments

import android.animation.Animator
import android.content.Context
import android.util.Log
import android.view.*
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DiffUtil
import com.dlfsystems.bartender.nav.Rudder
import androidx.recyclerview.widget.RecyclerView
import com.dlfsystems.bartender.*
import com.dlfsystems.bartender.room.Bottle
import com.dlfsystems.bartender.room.BottlesViewModel
import com.dlfsystems.bartender.fragments.CatalogFragment.BottleTabs
import com.dlfsystems.bartender.room.BarDB
import com.dlfsystems.bartender.views.BottleItemView
import io.github.douglasjunior.androidSimpleTooltip.SimpleTooltip

import io.reactivex.subjects.PublishSubject

class CatalogBottlesFragment : CatalogListFragment() {

    data class BottlesState(
        val tab: BottleTabs = BottleTabs.MINE,
        val filter: Int = 1,
        val emptyBar: Boolean = true
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
        lateinit var emptyLayout: LinearLayout
        lateinit var emptyTipAnchor: TextView
        lateinit var emptyText1: TextView
        lateinit var emptyText2: TextView
        lateinit var activeBottleCount: LiveData<Int>

        var isBarEmpty = false
        var lastState: BottlesState? = null

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

                emptyLayout = it.findViewById(R.id.bottles_emptylayout)
                emptyTipAnchor = it.findViewById(R.id.bottles_empty_tipanchor)
                emptyText1 = it.findViewById(R.id.bottles_emptytext1)
                emptyText2 = it.findViewById(R.id.bottles_emptytext2)

                activeBottleCount = BarDB.getInstance(mainView!!.context.applicationContext).bottleDao().liveActiveBottleCount()
                activeBottleCount.observe(bottlesFragment, Observer {
                    action.onNext(Action.bottlesActiveChanged(it))
                })
            }
        }

        override fun render(previousState: BaseState?, state: BaseState) {
            state as BottlesState
            previousState as BottlesState?
            lastState = state
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

            isBarEmpty = state.emptyBar
        }

        private fun subscribeLiveData(viewModel: BottlesViewModel, state: BottlesState) {
            viewModel.getLiveData().observe(bottlesFragment, Observer {
                it?.also {
                    if (it.size == 0) {
                        recyclerView.afterMeasured { }
                        showEmptyContent(true, state)
                    } else {
                        showEmptyContent(false, state)
                        recyclerView.afterMeasured {
                            if (isBarEmpty) showHelperTips()
                            recyclerAdapter.configureForTab(recyclerView, state.tab)
                        }
                    }
                    recyclerAdapter.submitList(it)

                }
            })
        }

        private fun showEmptyContent(isEmpty: Boolean, state: BottlesState) {
            if (!bottlesFragment.isHidden) {
                if (isEmpty) {
                    var filterString = "is empty"
                    if (lastState?.filter ?: 0 > 0) {
                        filterString = "has no " + bottlesFragment.resources.getStringArray(R.array.bottle_filter_array)[lastState!!.filter]
                    }
                    recyclerView.visibility = View.GONE
                    emptyLayout.visibility = View.VISIBLE
                    if (state.tab == BottleTabs.MINE) {
                        SimpleTooltip.Builder(mainView!!.context)
                            .anchorView(emptyTipAnchor)
                            .text("Pick 'Add to Bar'!")
                            .gravity(Gravity.BOTTOM)
                            .animated(true)
                            .build()
                            .show()
                        emptyText1.text = "Your bar $filterString.  Fill it up!"
                        emptyText2.text = "Pick 'Add to Bar' from the dropdown at the top."
                    } else {
                        emptyText1.text = "Your shopping list $filterString."
                        emptyText2.text = "Add items from the 'Add to Bar' view."
                    }
                    emptyText1.apply {
                        alpha = 0f
                        animate().alpha(1f).setDuration(1000).setListener(null)
                    }
                    emptyText2.apply {
                        alpha = 0f
                        animate().alpha(1f).setDuration(2000).setListener(null)
                    }
                } else {
                    emptyLayout.visibility = View.GONE
                    recyclerView.visibility = View.VISIBLE
                }
            }
        }

        private fun showHelperTips() {
            if (!bottlesFragment.isHidden) {
                recyclerView.layoutManager?.findViewByPosition(2)?.also {
                    (it.findViewById(R.id.item_bottle_owned_checkbox) as CheckBox).also {
                        SimpleTooltip.Builder(mainView!!.context)
                            .anchorView(it)
                            .text("Pick bottles you own!")
                            .gravity(Gravity.BOTTOM)
                            .animated(true)
                            .build()
                            .show()
                    }
                }
            }
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
            is Action.bottlesActiveChanged -> {
                changeState(state.copy(
                    emptyBar = action.count < 1
                ))
            }
            else -> { }
        }
    }
}