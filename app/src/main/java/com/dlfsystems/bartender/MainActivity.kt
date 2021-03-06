package com.dlfsystems.bartender

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
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
import com.crashlytics.android.Crashlytics;
import com.dlfsystems.bartender.fragments.LoadingFragment
import io.fabric.sdk.android.Fabric;

class MainActivity : AppCompatActivity(), StateChanger {

    lateinit var backstackDelegate: BackstackDelegate
    lateinit var fragmentStateChanger: FragmentStateChanger
    var disposables = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {

        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)

        val startScreen =
            if (prefs(applicationContext).getBoolean("populated", false)) CatalogFragment.CatalogKey()
            else LoadingFragment.LoadingKey()

        backstackDelegate = BackstackDelegate()
        backstackDelegate.onCreate(savedInstanceState,
            lastCustomNonConfigurationInstance,
            History.single(startScreen))
        backstackDelegate.registerForLifecycleCallbacks(this)

        super.onCreate(savedInstanceState)
        Fabric.with(this, Crashlytics())
        setContentView(R.layout.activity_main)
        setSupportActionBar(mainToolbar)
        supportActionBar?.setTitle("OpenBar")
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        fragmentStateChanger = FragmentStateChanger(supportFragmentManager, R.id.base_frame)
        backstackDelegate.setStateChanger(this)

        disposables += Rudder.navDest.observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                navigateTo(it)
            }

        ioThread {
            BarDB.getInstance(applicationContext).bottleDao().byId(1)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.actionbar_menu, menu)
        if (prefs(this).getBoolean("metric", false))
            menu?.getItem(1)?.setChecked(true)
        else
            menu?.getItem(0)?.setChecked(true)
        return true
    }

    private fun navigateTo(destKey: BaseKey) {
        if (destKey is CatalogFragment.CatalogKey) {
            backstackDelegate.backstack.setHistory(History.single(destKey), StateChange.REPLACE)
        }
        backstackDelegate.backstack.goTo(destKey)
    }

    override fun onBackPressed() {
        if (!backstackDelegate.onBackPressed()) {
            super.onBackPressed()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
            }
            R.id.action_menu_imperial -> {
                item.isChecked = true
                setMeasureMetric(false)
            }
            R.id.action_menu_metric -> {
                item.isChecked = true
                setMeasureMetric(true)
            }
            R.id.action_menu_about -> {
                showAboutDialog()
            }
            else -> { return false }
        }
        return true
    }

    fun showAboutDialog() {
        AlertDialog.Builder(this, R.style.DialogStyle)
            .setTitle("OpenBar for Android")
            .setMessage("Version 1.12\n2019 DLF Systems")
            .create().show()
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

    fun setMeasureMetric(value: Boolean) {
        prefs(this).edit().putBoolean("metric", value).apply()
        (supportFragmentManager.findFragmentById(R.id.base_frame) as BaseFragment).metricOptionChanged(value)
    }
}
