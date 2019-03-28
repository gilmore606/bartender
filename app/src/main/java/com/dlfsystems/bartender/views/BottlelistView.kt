package com.dlfsystems.bartender.views

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.Gravity
import android.widget.LinearLayout
import com.dlfsystems.bartender.fragments.BottleFragment
import com.dlfsystems.bartender.nav.Rudder
import com.dlfsystems.bartender.plusAssign
import com.dlfsystems.bartender.room.Bottle
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable

class BottlelistView @JvmOverloads constructor (
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
    defStyleRes: Int = 0
) : LinearLayout(context, attrs, defStyle, defStyleRes) {

    private var disposables = CompositeDisposable()

    val bottles = ArrayList<Bottle>(0)

    init {
        orientation = VERTICAL
        gravity = Gravity.CENTER_VERTICAL
    }

    fun populate(newbottles: ArrayList<Bottle>) {
        newbottles.filter { !(it in bottles) }
            .forEach {
                bottles.add(it)
                val view = BottleitemView(context)
                view.bindBottle(it)
                addView(view)
                disposables += view.clickEvent.observeOn(AndroidSchedulers.mainThread())
                    .subscribe {
                        Log.d("bartender", "FNORD navving to " + it.toString())
                        Rudder.navTo(BottleFragment.BottleKey(it.id))
                    }
            }
    }
}