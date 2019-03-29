package com.dlfsystems.bartender

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.AppCompatDelegate
import com.dlfsystems.bartender.nav.BaseKey
import com.dlfsystems.bartender.nav.FragmentStateChanger
import com.dlfsystems.bartender.nav.Rudder
import com.dlfsystems.bartender.fragments.CatalogFragment
import com.dlfsystems.bartender.room.BarDB
import com.zhuinden.simplestack.BackstackDelegate
import com.zhuinden.simplestack.History
import com.zhuinden.simplestack.StateChange
import com.zhuinden.simplestack.StateChanger
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_main.*

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
        setSupportActionBar(mainToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        fragmentStateChanger = FragmentStateChanger(supportFragmentManager, R.id.base_frame)
        backstackDelegate.setStateChanger(this)

        disposables += Rudder.navDest.observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                navigateTo(it)
            }

    }

    private fun navigateTo(destKey: BaseKey) {
        backstackDelegate.backstack.goTo(destKey)
    }

    override fun onBackPressed() {
        if (!backstackDelegate.onBackPressed()) {
            super.onBackPressed()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            (android.R.id.home) -> {
                onBackPressed()
            }
            else -> { return false }
        }
        return true
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

    fun toggleBackButton(value: Boolean) {
        supportActionBar?.setDisplayHomeAsUpEnabled(value)
    }
}
