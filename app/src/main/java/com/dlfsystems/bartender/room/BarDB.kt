package com.dlfsystems.bartender.room

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.dlfsystems.bartender.ioThread

@Database(entities = [(Drink::class), (Bottle::class)], version = 1)
abstract class BarDB : RoomDatabase() {
    abstract fun drinkDao(): DrinkDao
    abstract fun bottleDao(): BottleDao

    companion object {
        @Volatile
        private var instance: BarDB? = null

        fun getInstance(context: Context): BarDB {
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also { instance = it }
            }
        }

        private fun buildDatabase(context: Context): BarDB {
            Log.d("bartender", "FNORD building db")
            return Room.databaseBuilder(context, BarDB::class.java, "bar.db")
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        ioThread {
                            initialPopulate(context)
                        }
                    }
                })
                .build()
        }

        fun destroyInstance() {
            instance = null
        }

        // TODO: populate from files for real
        fun initialPopulate(context: Context) {
            Log.d("bartender", "FNORD initial population of database")
            val bottleDao = getInstance(context)!!.bottleDao()
            bottleDao.add(Bottle(1, "Vodka", "vodka"))
            bottleDao.add(Bottle(2, "Gin", "gin"))
            bottleDao.add(Bottle(3, "Rum", "rum"))
            bottleDao.add(Bottle(4, "Tequila", "tequila"))
            bottleDao.add(Bottle(5, "Whiskey", "whiskey"))
            bottleDao.add(Bottle(12, "Agave Syrup", "agave_syrup"))
            bottleDao.add(Bottle(13, "Ale", "ale"))
            bottleDao.add(Bottle(18, "Amaretto", "amaretto"))
            bottleDao.add(Bottle(20, "Angostura Bitters", "angostura_bitters"))
            bottleDao.add(Bottle(24, "Aperol", "aperol"))
            bottleDao.add(Bottle(26, "Apple Brandy", "apple_brandy"))
            bottleDao.add(Bottle(31, "Applejack", "applejack"))
            bottleDao.add(Bottle(35, "Aquavit", "aquavit"))
            bottleDao.add(Bottle(43, "Irish Cream", "irish_cream"))
            bottleDao.add(Bottle(52, "Beer", "beer"))
            bottleDao.add(Bottle(53, "Benedictine", "benedictine"))
            bottleDao.add(Bottle(67, "Blue Curacao", "blue_curacao"))
            bottleDao.add(Bottle(71, "Bourbon", "bourbon"))
            bottleDao.add(Bottle(74, "Brandy", "brandy"))
            bottleDao.add(Bottle(80, "Butterscotch Schnapps", "butterscotch_schnapps"))
            bottleDao.add(Bottle(81, "Cachaca", "cachaca"))
            bottleDao.add(Bottle(83, "Campari", "campari"))
            bottleDao.add(Bottle(89, "Soda Water", "soda_water"))
            bottleDao.add(Bottle(96, "Chambord", "chambord"))
            bottleDao.add(Bottle(97, "Sparkling Wine", "sparkling_wine"))
            bottleDao.add(Bottle(100, "Cherry Brandy", "cherry_brandy"))
            bottleDao.add(Bottle(103, "Cherry Liqueur", "cherry_liqueur"))
            bottleDao.add(Bottle(120, "Cider", "cider"))
            bottleDao.add(Bottle(121, "Cinnamon Schnapps", "cinnamon_schnapps"))
            bottleDao.add(Bottle(124, "Clamato", "clamato"))
            bottleDao.add(Bottle(128, "Cola", "cola"))
            bottleDao.add(Bottle(175, "Cynar", "cynar"))
        }
    }
}