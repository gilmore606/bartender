package com.dlfsystems.bartender.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.dlfsystems.bartender.ioThread

@Database(entities = [(Bottle::class), (Drink::class), (Spirit::class)], version = 1)
abstract class BarDB : RoomDatabase() {
    abstract fun bottleDao(): BottleDao
    abstract fun drinkDao(): DrinkDao
    abstract fun spiritDao(): SpiritDao

    companion object {
        private var INSTANCE: BarDB? = null

        fun getInstance(context: Context): BarDB? {
            if (INSTANCE == null) {
                synchronized(BarDB::class) {
                    INSTANCE = Room.databaseBuilder(context.applicationContext, BarDB::class.java, "bar.db")
                        .addCallback(object : Callback() {
                            override fun onCreate(db: SupportSQLiteDatabase) {
                                super.onCreate(db)
                                ioThread {
                                    (db as BarDB).initialPopulate(context)
                                }
                            }
                        })
                        .build()
                }
            }
            return INSTANCE
        }

        fun destroyInstance() {
            INSTANCE = null
        }
    }

    // TODO: populate from files for real
    fun initialPopulate(context: Context) {
        val spiritDao = getInstance(context)!!.spiritDao()
        spiritDao.add(Spirit(name="Gin"))
        spiritDao.add(Spirit(name="Vodka"))
        spiritDao.add(Spirit(name="Bourbon"))
        spiritDao.add(Spirit(name="Rye Whiskey"))
        spiritDao.add(Spirit(name="Dark Rum"))
        spiritDao.add(Spirit(name="Light Rum"))
        spiritDao.add(Spirit(name="Dry Vermouth"))
        spiritDao.add(Spirit(name="Sweet Vermouth"))
    }
}