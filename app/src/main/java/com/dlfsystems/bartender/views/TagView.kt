package com.dlfsystems.bartender.views

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.widget.TextView
import com.dlfsystems.bartender.R

class TagView @JvmOverloads constructor (
    val theTag: Tag,
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
    defStyleRes: Int = 0
    ) : TextView(context, attrs, defStyle, defStyleRes) {

    init {
        setBackgroundResource(R.drawable.bg_tagview)
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 10.0f)
        isAllCaps = true
        setPadding(30,16, 30, 16)

        text = theTag.tag
    }

}