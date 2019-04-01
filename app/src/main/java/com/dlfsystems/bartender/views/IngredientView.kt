package com.dlfsystems.bartender.views

import android.content.Context
import android.net.Uri
import android.preference.PreferenceManager
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
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
    val bottleAmount: TextView
    val bottleImage: ImageView
    val bottleTopView: LinearLayout

    init {
        LayoutInflater.from(context)
            .inflate(R.layout.ingredientlistview_item, this, true)
        onFinishInflate()

        bottleName = findViewById(R.id.ingredientlistview_item_name)
        bottleAmount = findViewById(R.id.ingredientlistview_amount)
        bottleImage = findViewById(R.id.ingredientlistview_bottle_image)
        bottleTopView = findViewById(R.id.ingredientlistview_item_toplayer)
    }

    fun bindIngredient(newingredient: Ingredient) {
        ingredient = newingredient
        bottleName.text = ingredient.bottleName
        var amount = ingredient.amount
        if (" oz" in amount) {
            if (PreferenceManager.getDefaultSharedPreferences(this.context).getBoolean("metric", false)) {
                val ounces = Regex("""(\d+\.?\d*) oz""").find(amount)!!.groupValues[1].toFloat()
                val centilitres = ounces * 3.0f
                amount = "%.1f".format(centilitres) + " cL"
            }
        }
        bottleAmount.text = amount
        Glide.with(context).load(Uri.parse("file:///android_asset/bottle_thumb/" + ingredient.bottleImage + ".png"))
            .asBitmap().into(bottleImage)
        setBackgroundResource(
                if (ingredient.bottleActive) R.drawable.bg_listitem_active else R.drawable.bg_listitem_inactive
        )
        bottleTopView.setOnClickListener {
            clickEvent.onNext(Bottle(id=ingredient.bottleId, name=ingredient.bottleName))
        }
    }

    fun metricOptionChanged(value: Boolean) {
        bindIngredient(ingredient)
    }
}