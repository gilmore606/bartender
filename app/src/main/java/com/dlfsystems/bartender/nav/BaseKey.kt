package com.dlfsystems.bartender.nav

import android.os.Bundle
import android.os.Parcelable
import com.dlfsystems.bartender.BaseFragment

abstract class BaseKey : Parcelable {
    val fragmentTag: String
        get() = toString()

    fun newFragment(): BaseFragment = createFragment().apply {
        arguments = (arguments ?: Bundle()).also { bundle ->
            bundle.putParcelable("KEY", this@BaseKey)
        }
    }

    protected abstract fun createFragment(): BaseFragment

    open fun getAnimation(): FragAnimPair =
        FragAnimPair(0, 0)

    open fun getBackAnimation(): FragAnimPair =
        FragAnimPair(0, 0)
}