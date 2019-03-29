package com.dlfsystems.bartender

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import com.dlfsystems.bartender.nav.BaseKey
import com.dlfsystems.bartender.room.BarDB
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject
import java.io.Serializable

// Top-level fragment representing a screen subject to navigation

abstract class BaseFragment : androidx.fragment.app.Fragment() {


    abstract class BaseState : Serializable


    abstract class BaseViewController {

        var mainView: View? = null

        val action = PublishSubject.create<Action>()

        // Inflate the main view, or return existing main view
        open fun getMainView(layoutResource: Int, inflater: LayoutInflater, container: ViewGroup?) : View? {
            return when (mainView) {
                null -> {
                    mainView = inflater.inflate(layoutResource, container, false)
                    subscribeActions()
                    mainView
                }
                else -> { mainView }
            }
        }

        // Bind UI elements to emit an action
        open fun subscribeActions() { }

        // Render a state to the UI elements
        open fun render(previousState: BaseState?, state: BaseState) { }

    }


    val requireArguments
        get() = this.arguments ?: throw IllegalStateException("Fragment arguments should exist!")

    fun <T : BaseKey> getKey(): T? = requireArguments.getParcelable<T>("KEY")

    var disposables = CompositeDisposable()

    abstract val layoutResource: Int
    abstract val viewController: BaseViewController
    var previousState = getDefaultState()

    abstract fun getDefaultState(): BaseState
    open fun makeStateFromArguments(arguments: Bundle) = getDefaultState()

    open fun makeInitialState(bundle: Bundle?, arguments: Bundle?): BaseState {
        if (arguments != null) {
            return makeStateFromArguments(arguments)
        }
        return getDefaultState()
    }

    fun renderInitialState() { viewController.render(null, previousState) }

    open fun bindActions() {
        if (!disposables.isDisposed) disposables.dispose()
        disposables = CompositeDisposable()

        disposables += viewController.action.observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                hearAction(it)
            }
    }

    open fun hearAction(action: Action) { }

    @Synchronized fun changeState(state: BaseState) {
        activity?.runOnUiThread {
            viewController.render(previousState, state)
        }
        previousState = state
    }

    fun setBottleActive(bottleId: Long, bottleName: String, active: Boolean) {
        context?.also {
            BarDB.setBottleActive(context!!, bottleId, active)
            Toast.makeText(context!!,
                if (active) ("Added " + bottleName + " to bar.")
                else ("Removed " + bottleName + " from bar."),
                Toast.LENGTH_SHORT).show()
        }
    }

    fun setDrinkFavorite(drinkId: Long, drinkName: String, favorite: Boolean) {
        context?.also {
            BarDB.setDrinkFavorite(context!!, drinkId, favorite)
            Toast.makeText(context!!,
                if (favorite) ("Added " + drinkName + " to favorites.")
                else ("Removed " + drinkName + " from favorites."),
                Toast.LENGTH_SHORT).show()
        }
    }

    fun setBottleShopping(bottleId: Long, bottleName: String, shopping: Boolean) {
        context?.also {
            BarDB.setBottleShopping(context!!, bottleId, shopping)
            Toast.makeText(context!!,
                if (shopping) ("Added " + bottleName + " to shopping list.")
                else ("Removed " + bottleName + " from shopping list."),
                Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, bundle: Bundle?) : View? {
        previousState = makeInitialState(bundle, arguments)
        val view = viewController.getMainView(layoutResource, inflater, container)
        bindActions()
        renderInitialState()
        toggleBackButton(backButtonEnabled)
        return view
    }

    override fun onHiddenChanged(hidden: Boolean) {
        if (hidden) onHide()
        else onUnhide()
    }

    protected open fun onHide() {
        disposables.dispose()
    }

    open val backButtonEnabled = true
    protected open fun onUnhide() {
        bindActions()
        toggleBackButton(backButtonEnabled)
    }

    override fun onDestroy() {
        disposables.dispose()
        super.onDestroy()
    }

    fun toggleBackButton(value: Boolean) {
        (activity as MainActivity).toggleBackButton(value)
    }
}