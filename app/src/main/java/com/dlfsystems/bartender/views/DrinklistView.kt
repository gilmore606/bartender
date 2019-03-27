package com.dlfsystems.bartender.views

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.widget.LinearLayout
import com.dlfsystems.bartender.nav.Rudder
import com.dlfsystems.bartender.plusAssign
import com.dlfsystems.bartender.room.Drink
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable

class DrinklistView @JvmOverloads constructor (
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
    defStyleRes: Int = 0
    ) : LinearLayout(context, attrs, defStyle, defStyleRes) {

    private var disposables = CompositeDisposable()

    val drinks = ArrayList<Drink>(0)

    init {
        orientation = VERTICAL
        gravity = Gravity.CENTER_VERTICAL
    }

    fun populate(newdrinks: ArrayList<Drink>) {
        newdrinks.filter { !(it in drinks) }
            .forEach {
                drinks.add(it)
                val view = DrinkitemView(context)
                view.bindDrink(it)
                addView(view)
                disposables += view.clickEvent.observeOn(AndroidSchedulers.mainThread())
                    .subscribe {
                        // Rudder.navTo(DrinkKey(it.id))
                    }
            }
    }

}