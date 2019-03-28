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
import com.dlfsystems.bartender.room.Ingredient
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable

class IngredientsView @JvmOverloads constructor (
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
    defStyleRes: Int = 0
) : LinearLayout(context, attrs, defStyle, defStyleRes) {

    private var disposables = CompositeDisposable()

    val ingredients = ArrayList<Ingredient>(0)

    init {
        orientation = VERTICAL
        gravity = Gravity.CENTER_VERTICAL
    }

    fun populate(newbottles: ArrayList<Ingredient>) {
        newbottles.filter { !(it in ingredients) }
            .forEach {
                ingredients.add(it)
                val view = IngredientView(context)
                view.bindIngredient(it)
                addView(view)
                disposables += view.clickEvent.observeOn(AndroidSchedulers.mainThread())
                    .subscribe {
                        Log.d("bartender", "FNORD navving to " + it.toString())
                        Rudder.navTo(BottleFragment.BottleKey(it.id))
                    }
            }
    }
}