package com.dlfsystems.bartender.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import com.dlfsystems.bartender.R
import com.dlfsystems.bartender.room.Drink
import io.reactivex.subjects.PublishSubject

class DrinkitemView @JvmOverloads constructor (
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
    defStyleRes: Int = 0
): LinearLayout(context, attrs, defStyle) {

    var drink = Drink(id=0, name="")

    val clickEvent = PublishSubject.create<Drink>()

    val drinkName: TextView
    val drinkTopView: LinearLayout

    init {
        LayoutInflater.from(context)
            .inflate(R.layout.drinklistview_item, this, true)
        onFinishInflate()

        drinkName = findViewById(R.id.drinklistview_item_name)
        drinkTopView = findViewById(R.id.drinklistview_item_toplayer)
    }

    fun bindDrink(newdrink: Drink) {
        drink = newdrink
        drinkName.text = drink.name
        drinkTopView.setOnClickListener {
            clickEvent.onNext(drink)
        }
    }
}