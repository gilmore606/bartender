package com.dlfsystems.bartender

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.dlfsystems.bartender.nav.BaseKey
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject
import java.io.Serializable

// Top-level fragment representing a screen subject to navigation

abstract class BaseFragment : androidx.fragment.app.Fragment() {


    abstract class BaseState : Serializable


    abstract class BaseViewController() {

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
        open fun render(previousState: BaseState, state: BaseState) { }

    }


    val requireArguments
        get() = this.arguments ?: throw IllegalStateException("Fragment arguments should exist!")

    fun <T : BaseKey> getKey(): T? = requireArguments.getParcelable<T>("KEY")

    var disposables = CompositeDisposable()

    abstract val layoutResource: Int
    abstract val viewController: BaseViewController
    var previousState = getDefaultState()

    abstract fun getDefaultState(): BaseState

    fun bindActions() {
        if (!disposables.isDisposed) disposables.dispose()
        disposables = CompositeDisposable()

        disposables += viewController.action.observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                hearAction(it)
            }
    }

    open fun hearAction(action: Action) { }

    fun changeState(state: BaseState) {
        viewController.render(previousState, state)
        previousState = state
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, bundle: Bundle?) : View? {
        val view = viewController.getMainView(layoutResource, inflater, container)
        bindActions()
        return view
    }

    override fun onHiddenChanged(hidden: Boolean) {
        if (hidden) onHide()
        else onUnhide()
    }

    protected open fun onHide() {
        disposables.dispose()
    }

    protected open fun onUnhide() {
        bindActions()
    }

    override fun onDestroy() {
        disposables.dispose()
        super.onDestroy()
    }
}