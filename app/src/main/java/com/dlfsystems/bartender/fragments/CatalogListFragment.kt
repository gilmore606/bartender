package com.dlfsystems.bartender.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.dlfsystems.bartender.Action
import com.dlfsystems.bartender.BaseFragment
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers

abstract class CatalogListFragment : BaseFragment() {

    var parentAction: Observable<Action>? = null

    override fun bindActions() {
        super.bindActions()

        parentAction?.let {
            it.observeOn(AndroidSchedulers.mainThread()).subscribe {
                hearAction(it)
            }
        }
    }
}