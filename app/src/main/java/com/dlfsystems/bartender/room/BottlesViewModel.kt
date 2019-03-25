package com.dlfsystems.bartender.room

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.paging.DataSource
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList

sealed class BottlesViewModel(application: Application) : AndroidViewModel(application) {

    var bottlesLiveData: LiveData<PagedList<Bottle>>
    init {
        val factory: DataSource.Factory<Int, Bottle> =
            bottleFactory()

        val pagedListBuilder = LivePagedListBuilder<Int, Bottle>(factory, 50)
        bottlesLiveData = pagedListBuilder.build()
    }

    fun getLiveData() = bottlesLiveData

    abstract fun bottleFactory(): DataSource.Factory<Int, Bottle>

    class All constructor(application: Application) : BottlesViewModel(application) {
        override fun bottleFactory() =
                BarDB.getInstance(getApplication()).bottleDao().getAllPaged()
    }
    class Active constructor(application: Application) : BottlesViewModel(application) {
        override fun bottleFactory() =
                BarDB.getInstance(getApplication()).bottleDao().getActivePaged()
    }
    class Shop constructor(application: Application) : BottlesViewModel(application) {
        override fun bottleFactory() =
                BarDB.getInstance(getApplication()).bottleDao().getShoppingPaged()
    }
}