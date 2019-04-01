package com.dlfsystems.bartender.views

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.widget.LinearLayout
import com.dlfsystems.bartender.plusAssign
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable

class TagbarView @JvmOverloads constructor (
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
    defStyleRes: Int = 0
    ) : LinearLayout(context, attrs, defStyle, defStyleRes) {

    val tags = ArrayList<Tag>(0)

    init {
        orientation = HORIZONTAL
        gravity = Gravity.CENTER_HORIZONTAL
    }

    fun populate(newtags: ArrayList<Tag>) {
        removeAllViewsInLayout()
        tags.clear()
        tags.addAll(newtags)
        tags.filter { it.tag != "All" }
            .forEach {
            val view = TagView(it, context)
            val params = LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
            params.setMargins(20,0,20,0)
            view.layoutParams = params
            addView(view)
        }
    }
}