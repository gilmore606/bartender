package com.dlfsystems.bartender.room

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.paging.DataSource
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList

class BottlesViewModel constructor(application: Application)
    : AndroidViewModel(application) {

    private var bottlesLiveData: LiveData<PagedList<Bottle>>

    init {
        val factory: DataSource.Factory<Int, Bottle> =
                BarDB.getInstance(getApplication()).bottleDao().getAllPaged()

        val pagedListBuilder = LivePagedListBuilder<Int, Bottle>(factory, 50)
        bottlesLiveData = pagedListBuilder.build()
    }

    fun getBottlesLiveData() = bottlesLiveData
}