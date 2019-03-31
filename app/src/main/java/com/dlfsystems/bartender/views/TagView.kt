package com.dlfsystems.bartender.views

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.widget.FrameLayout
import android.widget.TextView
import com.dlfsystems.bartender.R
import io.reactivex.disposables.CompositeDisposable

class TagView @JvmOverloads constructor (
    tag: Tag,
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
    defStyleRes: Int = 0
    ) : TextView(context, attrs, defStyle, defStyleRes) {

    init {
        setBackgroundResource(R.drawable.bg_tagview)
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 10.0f)
        isAllCaps = true
        setPadding(20,10, 20, 10)

        text = tag.tag
    }

}