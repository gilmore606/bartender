package com.dlfsystems.bartender

import android.view.View
import android.view.ViewGroup
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

operator fun CompositeDisposable.plusAssign(subscription: Disposable) {
    add(subscription)
}

val View.isVisible: Boolean
    get() = (visibility == View.VISIBLE)

var View.visibleElseGone: Boolean
    get() = (visibility == View.VISIBLE)
    set(value) { visibility = if (value) View.VISIBLE else View.GONE }

var View.visibleElseInvisible: Boolean
    get() = (visibility == View.VISIBLE)
    set(value) { visibility = if (value) View.VISIBLE else View.INVISIBLE }

fun View.visible() {
    visibility = View.VISIBLE
}

fun View.invisible() {
    visibility = View.INVISIBLE
}

fun View.gone() {
    visibility = View.GONE
}

val ViewGroup.views: List<View>
    get() = (0..getChildCount() - 1).map { getChildAt(it) }