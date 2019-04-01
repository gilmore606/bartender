package com.dlfsystems.bartender.fragments

import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import com.dlfsystems.bartender.Action
import com.dlfsystems.bartender.BaseFragment
import com.dlfsystems.bartender.R
import com.dlfsystems.bartender.nav.BaseKey
import com.dlfsystems.bartender.nav.Rudder
import com.dlfsystems.bartender.plusAssign
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.parcel.Parcelize

class LoadingFragment : BaseFragment() {

    data class LoadingState(
        val dummy: Boolean = false
    ) : BaseState()

    @Parcelize
    data class LoadingKey(val tag: String) : BaseKey() {
        constructor(): this("LoadingKey")
        override fun createFragment() = LoadingFragment()
    }

    class LoadingView(val loadingFragment: Fragment) : BaseViewController() {

        val disposables = CompositeDisposable()
        var progressBar: ProgressBar? = null

        override fun subscribeActions() {
            mainView?.also {
                progressBar = it.findViewById(R.id.loading_progress)

                disposables += Rudder.loadingProgress.observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    progressBar?.progress = it
                }
            }
        }

        override fun render(previousState: BaseState?, state: BaseState) {

        }
    }

    override val layoutResource = R.layout.fragment_loading
    override val viewController = LoadingView(this)
    override fun getDefaultState() = LoadingState()
    override val backButtonEnabled = false

    override fun hearAction(action: Action) {

    }

}