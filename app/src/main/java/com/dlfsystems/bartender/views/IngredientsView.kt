package com.dlfsystems.bartender.views

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.Gravity
import android.widget.LinearLayout
import com.dlfsystems.bartender.Action
import com.dlfsystems.bartender.fragments.BottleFragment
import com.dlfsystems.bartender.nav.Rudder
import com.dlfsystems.bartender.plusAssign
import com.dlfsystems.bartender.room.Ingredient
import com.dlfsystems.bartender.views
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject

class IngredientsView @JvmOverloads constructor (
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
    defStyleRes: Int = 0
) : LinearLayout(context, attrs, defStyle, defStyleRes) {

    val ingredients = ArrayList<Ingredient>(0)

    init {
        orientation = VERTICAL
        gravity = Gravity.CENTER_VERTICAL
    }

    fun populate(newIngredients: ArrayList<Ingredient>, action: PublishSubject<Action>) {
        newIngredients.filter { !(it.bottleId in ingredients.map { it.bottleId}) }
            .forEach {
                val view = IngredientView(context)
                view.bindIngredient(it, action)
                addView(view)
            }
        newIngredients.filter { (it.bottleId in ingredients.map { it.bottleId}) }
            .forEach { newIngredient ->
                views.filter { it is IngredientView && it.ingredient.bottleId == newIngredient.bottleId }
                    .forEach {
                        (it as IngredientView).bindIngredient(newIngredient, action)
                    }
            }
        ingredients.clear()
        ingredients.addAll(newIngredients)
    }

    fun metricOptionChanged(value: Boolean, action: PublishSubject<Action>) {
        views.filter { it is IngredientView }
            .forEach {
                (it as IngredientView).metricOptionChanged(value, action)
            }
    }
}