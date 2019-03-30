package com.dlfsystems.bartender.views

import android.app.Application
import android.content.Context
import android.net.Uri
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.bumptech.glide.Glide
import com.dlfsystems.bartender.Action
import com.dlfsystems.bartender.R
import com.dlfsystems.bartender.fragments.CatalogFragment.BottleTabs
import com.dlfsystems.bartender.room.BarDB
import com.dlfsystems.bartender.room.Bottle
import io.reactivex.subjects.PublishSubject

class BottleItemView @JvmOverloads constructor (
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
    defStyleRes: Int = 0
    ): LinearLayout(context, attrs, defStyle) {

    var bottle = Bottle(id=0, name="")
    var tab = BottleTabs.ALL

    val bottleName: TextView
    val bottleDrinkCount: TextView
    val bottleImage: ImageView
    val bottleActive: CheckBox
    val bottleShopping: CheckBox

    init {
        LayoutInflater.from(context)
            .inflate(R.layout.item_bottle, this, true)
        onFinishInflate()

        bottleName = findViewById(R.id.item_bottle_name)
        bottleDrinkCount = findViewById(R.id.item_bottle_drinkcount)
        bottleImage = findViewById(R.id.item_bottle_image)
        bottleActive = findViewById(R.id.item_bottle_owned_checkbox)
        bottleShopping = findViewById(R.id.item_bottle_shopping)
    }

    fun bindBottle(newbottle: Bottle, action: PublishSubject<Action>) {
        bottle = newbottle
        configureForTab(tab)
        bottleName.text = bottle.name
        bottleDrinkCount.text = "in " + bottle.drinkCount + " drinks"
        Glide.with(context).load(Uri.parse("file:///android_asset/bottle_thumb/" + bottle.image + ".png"))
            .asBitmap().into(bottleImage)
        bottleActive.setOnCheckedChangeListener { _,_ -> }
        bottleActive.isChecked = bottle.active
        bottleActive.setOnCheckedChangeListener { _, isChecked ->
            action.onNext(Action.bottleToggleActive(bottle, isChecked))
        }
        bottleShopping.setOnCheckedChangeListener { _,_ -> }
        bottleShopping.isChecked = bottle.shopping
        bottleShopping.setOnCheckedChangeListener { _, isChecked ->
            action.onNext(Action.bottleToggleShopping(bottle, isChecked))
        }
        setOnClickListener {
            action.onNext(Action.navToBottle(bottle.id))
        }
    }

    fun configureForTab(newtab: BottleTabs) {
        tab = newtab
        when (tab) {
            BottleTabs.MINE -> {
                bottleActive.visibility = View.GONE
            }
            BottleTabs.ALL -> {
                bottleActive.visibility = View.VISIBLE
            }
            BottleTabs.SHOP -> {
                bottleActive.visibility = View.VISIBLE
            }
        }
    }
}