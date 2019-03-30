package com.dlfsystems.bartender.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
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
    val drinkImage: ImageView
    val drinkMissing: TextView
    val drinkTopView: LinearLayout

    init {
        LayoutInflater.from(context)
            .inflate(R.layout.drinklistview_item, this, true)
        onFinishInflate()

        drinkName = findViewById(R.id.drinklistview_item_name)
        drinkImage = findViewById(R.id.drinklistview_item_image)
        drinkMissing = findViewById(R.id.drinklistview_item_missing)
        drinkTopView = findViewById(R.id.drinklistview_item_toplayer)
    }

    fun bindDrink(newdrink: Drink) {
        drink = newdrink
        drinkName.text = drink.name
        if (drink.image > 0) drinkImage.setImageDrawable(ContextCompat.getDrawable(context, drink.image))
        if (drink.missingBottles > 0)
            drinkMissing.text = "need " + drink.missingBottles.toString()
        else
            drinkMissing.text = ""
        setBackgroundResource(
            if (drink.missingBottles < 1) R.drawable.bg_listitem_active else R.drawable.bg_listitem_inactive
        )
        drinkTopView.setOnClickListener {
            clickEvent.onNext(drink)
        }
    }
}