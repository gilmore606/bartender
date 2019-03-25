package com.dlfsystems.bartender.room

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.dlfsystems.bartender.R
import com.dlfsystems.bartender.ioThread
import java.io.File

@Database(entities = [(Drink::class), (DrinkIngredient::class), (Bottle::class)], version = 1)
abstract class BarDB : RoomDatabase() {
    abstract fun drinkDao(): DrinkDao
    abstract fun bottleDao(): BottleDao
    abstract fun drinkIngredientDao(): DrinkIngredientDao

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

            val bottleDao = getInstance(context).bottleDao()

            val bottleInputStream = context.resources.openRawResource(R.raw.bottles)
            val bottleReader = bottleInputStream.bufferedReader()
            var eof = false
            while (!eof) {
                val line = bottleReader.readLine()
                if (line == null) eof = true
                else {
                    val chunks = line.split('|')
                    val id = chunks[0].toLong()
                    val name = chunks[1]
                    val imageName = chunks[2]
                    val imageId = context.resources.getIdentifier(imageName, "drawable", context.packageName)
                    val btype = chunks[3].toLong()
                    bottleDao.add(Bottle(id = id, name = name, image = imageId))
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
                    drinkDao.add(Drink(id = drinkId, name = name))

                    var i = 2
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
        }

        fun setBottleActive(context: Context, bottleId: Long, active: Boolean) {
            ioThread {
                getInstance(context).bottleDao().setActive(bottleId, if (active) 1 else 0)
            }
        }
    }
}