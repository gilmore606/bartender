package com.dlfsystems.bartender.room

import android.content.Context
import android.preference.PreferenceManager
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
import com.dlfsystems.bartender.prefs

@Database(entities = [(Drink::class), (DrinkIngredient::class), (Bottle::class), (Family::class),
    (BottleFamily::class), (Drinktag::class), (DrinkDrinktag::class), (Filter::class),
    (Glass::class)], version = 1)
abstract class BarDB : RoomDatabase() {
    abstract fun drinkDao(): DrinkDao
    abstract fun bottleDao(): BottleDao
    abstract fun drinkIngredientDao(): DrinkIngredientDao
    abstract fun familyDao(): FamilyDao
    abstract fun bottleFamilyDao(): BottleFamilyDao
    abstract fun drinktagDao(): DrinktagDao
    abstract fun drinkDrinktagDao(): DrinkDrinktagDao
    abstract fun filterDao(): FilterDao
    abstract fun glassDao(): GlassDao

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

            val filterDao = getInstance(context).filterDao()

            filterDao.removeAll("bottle")
            filterDao.removeAll("drink")
            filterDao.add(Filter(0, "bottle", 0))
            filterDao.add(Filter(0, "drink", 0))

            val familyDao = getInstance(context).familyDao()

            familyDao.add(Family(0, "All", "All ingredients."))
            familyDao.add(Family(1, "Spirit", "Spirits, or base spirits, are distilled hard liquors used as the base of a cocktail."))
            familyDao.add(Family(2, "Liqueur", "Liqueurs are sweetened and flavored lower-alcohol products, often mixed into base spirits in cocktails."))
            familyDao.add(Family(3, "Mixer", "Mixers are non-alcoholic beverages such as fruit juices and sodas."))
            familyDao.add(Family(4, "Amaro", "Amari (the plural of Amaro) are bittersweet herbal liqueurs."))
            familyDao.add(Family(5, "Wine", "Wines and wine-based liqueurs, made from fermented grapes."))
            familyDao.add(Family(6, "Grocery", "Food items from the grocery store."))
            familyDao.add(Family(7, "Flavoring", "Add-ins used in small amounts to change a drink's flavor."))


            val bottleDao = getInstance(context).bottleDao()
            val bottleFamilyDao = getInstance(context).bottleFamilyDao()
            val drinkDao = getInstance(context).drinkDao()
            val drinkIngredientDao = getInstance(context).drinkIngredientDao()
            val drinkDrinktagDao = getInstance(context).drinkDrinktagDao()
            val drinktagDao = getInstance(context).drinktagDao()
            val glassDao = getInstance(context).glassDao()

            val bottleInputStream = context.resources.openRawResource(R.raw.bottles)
            val bottleReader = bottleInputStream.bufferedReader()
            var eof = false
            while (!eof) {
                val line = bottleReader.readLine()
                Rudder.addLoadProgress(0.4f)
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
                    val familyObjects = ArrayList<BottleFamily>(0)
                    families.forEach { familyObjects.add(BottleFamily(id = 0, bottleId = id, familyId = it.toLong())) }
                    bottleFamilyDao.addAll(familyObjects)
                }
            }
            Log.d("bartender", "FNORD done loading bottles")

            drinktagDao.add(Drinktag(0, "All", "All drinks."))
            drinktagDao.add(Drinktag(1, "IBA", "Classic and contemporary cocktails recognized by the International Bartenders Association."))
            drinktagDao.add(Drinktag(2, "Short", "Drinks served in short glasses such as rocks or coupe glasses, usually fairly potent."))
            drinktagDao.add(Drinktag(3, "Highball", "Topped with a mixer, highball type drinks are served in tall glasses, usually less potent and more refreshing."))
            drinktagDao.add(Drinktag(4, "Tiki", "Tropics-inspired drinks full of fruit and sweet."))
            drinktagDao.add(Drinktag(5, "Shooter", "Drinks served in small glasses meant to be downed in a single gulp."))
            drinktagDao.add(Drinktag(6, "Classic", "Drinks with a long and storied history."))
            drinktagDao.add(Drinktag(7, "Blended", "Drinks made in a blender."))
            drinktagDao.add(Drinktag(8, "Sour", "Tart and tangy drinks made with citrus juice."))
            drinktagDao.add(Drinktag(9, "Sweet", "Drinks on the sweeter side."))
            drinktagDao.add(Drinktag(10, "Strong", "Drinks that pack an alcoholic punch."))
            drinktagDao.add(Drinktag(11, "Easy", "Simple drinks of only two or three ingredients.  Easy to make even if you've had a few."))
            drinktagDao.add(Drinktag(12, "Complex", "Drinks with complex flavor combinations that may not be for everyone."))

            glassDao.add(Glass(1, "Coupe", "coupe"))
            glassDao.add(Glass(2, "Champagne Flute", "flute"))
            glassDao.add(Glass(3, "Highball / Collins", "highball"))
            glassDao.add(Glass(4, "Hurricane", "hurricane"))
            glassDao.add(Glass(5, "Martini", "martini"))
            glassDao.add(Glass(6, "Mug", "mug"))
            glassDao.add(Glass(7, "Nick and Nora", "nicknora"))
            glassDao.add(Glass(8, "Rocks", "rocks"))
            glassDao.add(Glass(9, "Shot", "shot"))

            Log.d("bartender", "FNORD loading drinks")

            val drinkInputStream = context.resources.openRawResource(R.raw.drinks)
            val drinkReader = drinkInputStream.bufferedReader()
            eof = false
            var ingredientId: Long = 1
            while (!eof) {
                val line = drinkReader.readLine()
                Log.d("bartender", "FNORD " + line)
                Rudder.addLoadProgress(0.4f)
                if (line == null) eof = true
                else {
                    val chunks = line.split('|')
                    val drinkId = chunks[0].toLong()
                    val tagIds = chunks[1].split(',')
                    val name = chunks[2]
                    val image = chunks[3]
                    val makestr = chunks[4]
                    val garnishstr = chunks[5]
                    val glass = chunks[6].toLong()
                    val ice = chunks[7]
                    val infoId = context.resources.getIdentifier(image, "string", context.packageName)
                    val makeId = context.resources.getIdentifier("make_" + makestr, "string", context.packageName)
                    val garnishId = context.resources.getIdentifier("garnish_" + garnishstr, "string", context.packageName)

                    drinkDao.add(Drink(id = drinkId, name = name, image = image, info = infoId, make = makeId, garnish = garnishId, glass = glass, ice = ice))
                    val tagobjects = ArrayList<DrinkDrinktag>(0)
                    tagIds.forEach { if (it != "") tagobjects.add(DrinkDrinktag(id = 0, drinkId = drinkId, drinktagId = it.toLong())) }
                    drinkDrinktagDao.addAll(tagobjects)

                    var i = 8
                    var eol = false
                    val ingredientObjects = ArrayList<DrinkIngredient>(0)
                    while (!eol) {
                        if (i >= chunks.size) eol = true
                        else {
                            val ingredient = chunks[i].toLong()
                            val amount = chunks[i+1]
                            ingredientObjects.add(DrinkIngredient(id = ingredientId, drinkId = drinkId, bottleId = ingredient, amount = amount))
                            ingredientId++
                            i += 2
                        }
                    }
                    drinkIngredientDao.addAll(ingredientObjects)
                }
            }
            Log.d("bartender", "FNORD setting populated pref")
            prefs(context).edit().putBoolean("populated", true).apply()
            Log.d("bartender", "FNORD moving to catalog")
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