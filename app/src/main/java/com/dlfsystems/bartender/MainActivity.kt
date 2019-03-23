package com.dlfsystems.bartender

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import com.dlfsystems.bartender.nav.BaseKey
import com.dlfsystems.bartender.nav.FragmentStateChanger
import com.dlfsystems.bartender.nav.Rudder
import com.dlfsystems.bartender.fragments.CatalogFragment
import com.zhuinden.simplestack.BackstackDelegate
import com.zhuinden.simplestack.History
import com.zhuinden.simplestack.StateChange
import com.zhuinden.simplestack.StateChanger
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable

class MainActivity : AppCompatActivity(), StateChanger {

    lateinit var backstackDelegate: BackstackDelegate
    lateinit var fragmentStateChanger: FragmentStateChanger
    var disposables = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {

        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)

        backstackDelegate = BackstackDelegate()
        backstackDelegate.onCreate(savedInstanceState,
            lastCustomNonConfigurationInstance,
            History.single(CatalogFragment.CatalogKey()))
        backstackDelegate.registerForLifecycleCallbacks(this)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fragmentStateChanger = FragmentStateChanger(supportFragmentManager, R.id.base_frame)
        backstackDelegate.setStateChanger(this)

        disposables += Rudder.navDest.distinctUntilChanged().observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                navigateTo(it)
            }

    }

    private fun navigateTo(destKey: BaseKey) {
        backstackDelegate.backstack.goTo(destKey)
    }

    private fun navigateBack() {
        backstackDelegate.backstack.goBack()
    }

    override fun onBackPressed() {
        if (!backstackDelegate.onBackPressed()) {
            super.onBackPressed()
        }
    }

    override fun handleStateChange(stateChange: StateChange, completionCallback: StateChanger.Callback) {
        if (stateChange.isTopNewStateEqualToPrevious) {
            completionCallback.stateChangeComplete()
            return
        }
        fragmentStateChanger.handleStateChange(stateChange)
        completionCallback.stateChangeComplete()
    }

    override fun onDestroy() {
        disposables.dispose()
        super.onDestroy()
    }
}
