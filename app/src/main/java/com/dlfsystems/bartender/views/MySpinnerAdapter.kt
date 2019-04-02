package com.dlfsystems.bartender.views

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.BaseAdapter
import com.dlfsystems.bartender.R

class MySpinnerAdapter(context: Context, resource: Int, objects: Array<String>): ArrayAdapter<String>(context, resource, objects) {

    private var selected: Int = -1

    fun setSelection(position: Int) {
        selected = position
        notifyDataSetChanged()
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val itemView = super.getDropDownView(position, convertView, parent)
        if (position == selected)
            itemView.setBackgroundResource(R.drawable.bg_listitem_active)
        else
            itemView.setBackgroundResource(R.drawable.bg_spinner)
        return itemView
    }
}