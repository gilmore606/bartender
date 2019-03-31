package com.dlfsystems.bartender.room

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.dlfsystems.bartender.R
import com.dlfsystems.bartender.fragments.CatalogFragment
import com.dlfsystems.bartender.fragments.LoadingFragment
import com.dlfsystems.bartender.ioThread
import com.dlfsystems.bartender.nav.Rudder

@Database(entities = [(Drink::class), (DrinkIngredient::class), (Bottle::class), (Family::class), (BottleFamily::class)], version = 1)
abstract class BarDB : RoomDatabase() {
    abstract fun drinkDao(): DrinkDao
    abstract fun bottleDao(): BottleDao
    abstract fun drinkIngredientDao(): DrinkIngredientDao
    abstract fun familyDao(): FamilyDao
    abstract fun bottleFamilyDao(): BottleFamilyDao

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

        fun initialPopulate(context: Context) {
            Log.d("bartender", "FNORD initial population of database")

            Rudder.navTo(LoadingFragment.LoadingKey())

            val familyDao = getInstance(context).familyDao()

            familyDao.add(Family(1, "Spirit", "Spirits, or base spirits, are distilled hard liquors used as the base of a cocktail."))
            familyDao.add(Family(2, "Liqueur", "Liqueurs are sweetened and flavored lower-alcohol products, often mixed into base spirits in cocktails."))
            familyDao.add(Family(3, "Mixer", "Mixers are non-alcoholic beverages such as fruit juices and sodas."))
            familyDao.add(Family(4, "Amaro", "Amari (the plural of Amaro) are bittersweet herbal liqueurs."))
            familyDao.add(Family(5, "Wine", "Wines and wine-based liqueurs, made from fermented grapes."))
            familyDao.add(Family(6, "Grocery", "Food items from the grocery store."))
            familyDao.add(Family(7, "Flavoring", "Add-ins used in small amounts to change a drink's flavor."))


            val bottleDao = getInstance(context).bottleDao()
            val bottleFamilyDao = getInstance(context).bottleFamilyDao()

            val bottleInputStream = context.resources.openRawResource(R.raw.bottles)
            val bottleReader = bottleInputStream.bufferedReader()
            var eof = false
            while (!eof) {
                val line = bottleReader.readLine()
                if (line == null) eof = true
                else {
                    val chunks = line.split('|')
                    val id = chunks[0].toLong()
                    val families = chunks[1].split(',')
                    val name = chunks[2]
                    val imageName = chunks[3]
                    val stringId = context.resources.getIdentifier(imageName, "string", context.packageName)
                    val btype = chunks[4].toLong()
                    bottleDao.add(Bottle(id = id, name = name, image = imageName, descstr = stringId, type = btype))
                    families.forEach { bottleFamilyDao.add(BottleFamily(id = 0, bottleId = id, familyId = it.toLong())) }
                }
            }

            val drinkDao = getInstance(context).drinkDao()
            val drinkIngredientDao = getInstance(context).drinkIngredientDao()

            val drinkInputStream = context.resources.openRawResource(R.raw.drinks)
            val drinkReader = drinkInputStream.bufferedReader()
            eof = false
            var ingredientId: Long = 1
            while (!eof) {
                val line = drinkReader.readLine()
                if (line == null) eof = true
                else {
                    val chunks = line.split('|')
                    val drinkId = chunks[0].toLong()
                    val name = chunks[1]
                    val image = chunks[2]
                    val makestr = chunks[3]
                    val garnishstr = chunks[4]
                    val infoId = context.resources.getIdentifier(image, "string", context.packageName)
                    val makeId = context.resources.getIdentifier("make_" + makestr, "string", context.packageName)
                    val garnishId = context.resources.getIdentifier("garnish_" + garnishstr, "string", context.packageName)
                    drinkDao.add(Drink(id = drinkId, name = name, image = image, info = infoId, make = makeId, garnish = garnishId))

                    var i = 5
                    var eol = false
                    while (!eol) {
                        if (i >= chunks.size) eol = true
                        else {
                            val ingredient = chunks[i].toLong()
                            val amount = chunks[i+1]
                            drinkIngredientDao.add(DrinkIngredient(id = ingredientId, drinkId = drinkId, bottleId = ingredient, amount = amount))
                            ingredientId++
                            i += 2
                        }
                    }
                }
            }
            Rudder.navTo(CatalogFragment.CatalogKey())
        }

        fun setBottleActive(context: Context, bottleId: Long, active: Boolean) {
            ioThread {
                getInstance(context).bottleDao().setActive(bottleId, if (active) 1 else 0)
            }
        }

        fun setBottleShopping(context: Context, bottleId: Long, shopping: Boolean) {
            ioThread {
                getInstance(context).bottleDao().setShopping(bottleId, if (shopping) 1 else 0)
            }
        }

        fun setDrinkFavorite(context: Context, drinkId: Long, favorite: Boolean) {
            ioThread {
                getInstance(context).drinkDao().setFavorite(drinkId, if (favorite) 1 else 0)
            }
        }
    }
}