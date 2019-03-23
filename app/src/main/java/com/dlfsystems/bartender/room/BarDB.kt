package com.dlfsystems.bartender.room

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [(Bottle::class), (Drink::class), (Kind::class)], version = 1)
abstract class BarDB : RoomDatabase() {
    abstract fun bottleDao(): BottleDao
    abstract fun drinkDao(): DrinkDao
    abstract fun kindDao(): KindDao
}