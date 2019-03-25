package com.dlfsystems.bartender.room

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.dlfsystems.bartender.R
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
            bottleDao.add(Bottle(1, "Vodka", R.drawable.vodka))
            bottleDao.add(Bottle(2, "Gin", R.drawable.gin))
            bottleDao.add(Bottle(3, "Rum", R.drawable.rum))
            bottleDao.add(Bottle(4, "Tequila", R.drawable.tequila))
            bottleDao.add(Bottle(5, "Whiskey", R.drawable.whiskey))
            bottleDao.add(Bottle(12, "Agave Syrup", R.drawable.agave_syrup))
            bottleDao.add(Bottle(13, "Ale", R.drawable.ale))
            bottleDao.add(Bottle(18, "Amaretto", R.drawable.amaretto))
            bottleDao.add(Bottle(20, "Angostura Bitters", R.drawable.angostura_bitters))
            bottleDao.add(Bottle(24, "Aperol", R.drawable.aperol))
            bottleDao.add(Bottle(26, "Apple Brandy", R.drawable.apple_brandy))
            bottleDao.add(Bottle(31, "Applejack", R.drawable.applejack))
            bottleDao.add(Bottle(35, "Aquavit", R.drawable.aquavit))
            bottleDao.add(Bottle(43, "Irish Cream", R.drawable.irish_cream))
            bottleDao.add(Bottle(52, "Beer", R.drawable.beer))
            bottleDao.add(Bottle(53, "Benedictine", R.drawable.benedictine))
            bottleDao.add(Bottle(67, "Blue Curacao", R.drawable.blue_curacao))
            bottleDao.add(Bottle(71, "Bourbon", R.drawable.bourbon))
            bottleDao.add(Bottle(74, "Brandy", R.drawable.brandy))
            bottleDao.add(Bottle(80, "Butterscotch Schnapps", R.drawable.butterscotch_schnapps))
            bottleDao.add(Bottle(81, "Cachaca", R.drawable.cachaca))
            bottleDao.add(Bottle(83, "Campari", R.drawable.campari))
            bottleDao.add(Bottle(89, "Soda Water", R.drawable.soda_water))
            bottleDao.add(Bottle(96, "Chambord", R.drawable.chambord))
            bottleDao.add(Bottle(97, "Sparkling Wine", R.drawable.sparkling_wine))
            bottleDao.add(Bottle(100, "Cherry Brandy", R.drawable.cherry_brandy))
            bottleDao.add(Bottle(103, "Cherry Liqueur", R.drawable.cherry_liqueur))
            bottleDao.add(Bottle(120, "Cider", R.drawable.cider))
            bottleDao.add(Bottle(121, "Cinnamon Schnapps", R.drawable.cinnamon_schnapps))
            bottleDao.add(Bottle(124, "Clamato", R.drawable.clamato))
            bottleDao.add(Bottle(128, "Cola", R.drawable.cola))
            bottleDao.add(Bottle(175, "Cynar", R.drawable.cynar))
        }

        fun setBottleActive(context: Context, bottleId: Long, active: Boolean) {
            ioThread {
                getInstance(context).bottleDao().setActive(bottleId, if (active) 1 else 0)
            }
        }
    }
}