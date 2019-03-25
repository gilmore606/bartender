package com.dlfsystems.bartender.room

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.paging.DataSource
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList

sealed class DrinksViewModel(application: Application) : AndroidViewModel(application) {

    var drinksLiveData: LiveData<PagedList<Drink>>
    init {
        val factory: DataSource.Factory<Int, Drink> =
                drinkFactory()

        val pagedListBuilder = LivePagedListBuilder<Int, Drink>(factory, 50)
        drinksLiveData = pagedListBuilder.build()
    }

    fun getLiveData() = drinksLiveData

    abstract fun drinkFactory(): DataSource.Factory<Int, Drink>

    class All constructor(application: Application) : DrinksViewModel(application) {
        override fun drinkFactory() =
                BarDB.getInstance(getApplication()).drinkDao().getAllPaged()
    }
    class Favorites constructor(application: Application) : DrinksViewModel(application) {
        override fun drinkFactory() =
                BarDB.getInstance(getApplication()).drinkDao().getFavoritesPaged()
    }
}