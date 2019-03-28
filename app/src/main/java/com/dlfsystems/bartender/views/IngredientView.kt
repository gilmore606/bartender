package com.dlfsystems.bartender.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import com.dlfsystems.bartender.R
import com.dlfsystems.bartender.room.Bottle
import com.dlfsystems.bartender.room.Ingredient
import io.reactivex.subjects.PublishSubject

class IngredientView @JvmOverloads constructor (
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
    defStyleRes: Int = 0
): LinearLayout(context, attrs, defStyle) {

    var ingredient = Ingredient()

    val clickEvent = PublishSubject.create<Bottle>()

    val bottleName: TextView
    val bottleTopView: LinearLayout

    init {
        LayoutInflater.from(context)
            .inflate(R.layout.ingredientlistview_item, this, true)
        onFinishInflate()

        bottleName = findViewById(R.id.ingredientlistview_item_name)
        bottleTopView = findViewById(R.id.ingredientlistview_item_toplayer)
    }

    fun bindIngredient(newingredient: Ingredient) {
        ingredient = newingredient
        bottleName.text = ingredient.amount + " " + ingredient.bottleName
        setBackgroundResource(
                if (ingredient.bottleActive) R.drawable.bg_listitem_active else R.drawable.bg_listitem_inactive
        )
        bottleTopView.setOnClickListener {
            clickEvent.onNext(Bottle(id=ingredient.bottleId, name=ingredient.bottleName))
        }
    }
}