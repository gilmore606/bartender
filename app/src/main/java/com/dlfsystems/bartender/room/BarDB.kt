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
        spiritDao.add(Spirit(1, "Vodka", "vodka"))
        spiritDao.add(Spirit(2, "Gin", "gin"))
        spiritDao.add(Spirit(3, "Rum", "rum"))
        spiritDao.add(Spirit(4, "Tequila", "tequila"))
        spiritDao.add(Spirit(5, "Whiskey", "whiskey"))
        spiritDao.add(Spirit(12, "Agave Syrup", "agave_syrup"))
        spiritDao.add(Spirit(13, "Ale", "ale"))
        spiritDao.add(Spirit(18, "Amaretto", "amaretto"))
        spiritDao.add(Spirit(20, "Angostura Bitters", "angostura_bitters"))
        spiritDao.add(Spirit(24, "Aperol", "aperol"))
        spiritDao.add(Spirit(26, "Apple Brandy", "apple_brandy"))
        spiritDao.add(Spirit(31, "Applejack", "applejack"))
        spiritDao.add(Spirit(35, "Aquavit", "aquavit"))
        spiritDao.add(Spirit(43, "Irish Cream", "irish_cream"))
        spiritDao.add(Spirit(52, "Beer", "beer"))
        spiritDao.add(Spirit(53, "Benedictine", "benedictine"))
        spiritDao.add(Spirit(67, "Blue Curacao", "blue_curacao"))
        spiritDao.add(Spirit(71, "Bourbon", "bourbon"))
        spiritDao.add(Spirit(74, "Brandy", "brandy"))
        spiritDao.add(Spirit(80, "Butterscotch Schnapps", "butterscotch_schnapps"))
        spiritDao.add(Spirit(81, "Cachaca", "cachaca"))
        spiritDao.add(Spirit(83, "Campari", "campari"))
        spiritDao.add(Spirit(89, "Soda Water", "soda_water"))
        spiritDao.add(Spirit(96, "Chambord", "chambord"))
        spiritDao.add(Spirit(97, "Sparkling Wine", "sparkling_wine"))
        spiritDao.add(Spirit(100, "Cherry Brandy", "cherry_brandy"))
        spiritDao.add(Spirit(103, "Cherry Liqueur", "cherry_liqueur"))
        spiritDao.add(Spirit(120, "Cider", "cider"))
        spiritDao.add(Spirit(121, "Cinnamon Schnapps", "cinnamon_schnapps"))
        spiritDao.add(Spirit(124, "Clamato", "clamato"))
        spiritDao.add(Spirit(128, "Cola", "coca_cola"))
        spiritDao.add(Spirit(175, "Cynar", "cynar"))
    }
}