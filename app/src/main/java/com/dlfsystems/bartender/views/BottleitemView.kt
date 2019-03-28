package com.dlfsystems.bartender.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import com.dlfsystems.bartender.R
import com.dlfsystems.bartender.room.Bottle
import io.reactivex.subjects.PublishSubject

class BottleitemView @JvmOverloads constructor (
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
    defStyleRes: Int = 0
): LinearLayout(context, attrs, defStyle) {

    var bottle = Bottle(id=0, name="")

    val clickEvent = PublishSubject.create<Bottle>()

    val bottleName: TextView
    val bottleTopView: LinearLayout

    init {
        LayoutInflater.from(context)
            .inflate(R.layout.bottlelistview_item, this, true)
        onFinishInflate()

        bottleName = findViewById(R.id.bottlelistview_item_name)
        bottleTopView = findViewById(R.id.bottlelistview_item_toplayer)
    }

    fun bindBottle(newbottle: Bottle) {
        bottle = newbottle
        bottleName.text = bottle.name
        bottleTopView.setOnClickListener {
            clickEvent.onNext(bottle)
        }
    }
}