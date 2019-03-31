package com.dlfsystems.bartender.room

import androidx.annotation.Nullable
import androidx.lifecycle.LiveData
import androidx.paging.DataSource
import androidx.room.*

@Entity(tableName = "drinks")
data class Drink(@PrimaryKey val id: Long,
                       val name: String,
                        val favorite: Boolean = false,
                        val image: String = "",
                        val info: Int = 0,
                        val make: Int = 0,
                        val garnish: Int = 0,
                        val missingBottles: Int = 0)

@Entity(tableName = "drink_ingredients",
        foreignKeys = arrayOf(ForeignKey(
            entity = Drink::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("drinkId")
        ), ForeignKey(
            entity = Bottle::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("bottleId")
        )),
        indices = arrayOf(
            Index(value = ["drinkId"]),
            Index(value = ["bottleId"])
        ))
data class DrinkIngredient(@PrimaryKey(autoGenerate = true) val id: Long = 1,
                            val drinkId: Long,
                            val bottleId: Long,
                            val amount: String)

data class Ingredient(val bottleId: Long = 0,
                      val bottleName: String = "",
                      val bottleImage: String = "",
                      val bottleActive: Boolean = false,
                      val amount: String = ""
                      )

@Entity(tableName = "drinktags")
data class Drinktag(@PrimaryKey val id: Long,
                    val name: String,
                    val description: String)

@Entity(tableName = "drink_drinktag",
        foreignKeys = arrayOf(ForeignKey(
            entity = Drink::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("drinkId")
        ), ForeignKey(
            entity = Drinktag::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("drinktagId")
        )),
    indices = arrayOf(
        Index(value = ["drinkId"]),
        Index(value = ["drinktagId"])
    ))
data class DrinkDrinktag(@PrimaryKey(autoGenerate = true) val id: Long,
                         val drinkId: Long,
                         val drinktagId: Long)

@Dao
interface DrinktagDao {
    @Insert
    fun add(drinktag: Drinktag)

    @Query("SELECT t.id, t.name, t.description FROM drink_drinktag dt INNER JOIN drinktags t ON t.id = dt.drinktagId WHERE dt.drinkId = :drinkId")
    fun liveDrinktagsForDrink(drinkId: Long): LiveData<List<Drinktag>>
}

@Dao
interface DrinkDrinktagDao {
    @Insert
    fun add(drinkdrinktag: DrinkDrinktag)
}

@Dao
interface DrinkDao {

    companion object {
        const val drinkWithMissingCount =
                "d.id id, d.name name, d.favorite favorite, d.image, d.info, d.make, d.garnish, count(di.bottleId) - sum(b.active) missingBottles FROM drink_ingredients di INNER JOIN bottles b ON di.bottleId = b.id INNER JOIN drinks d on di.drinkId = d.id"
    }
    @Query("SELECT * FROM drinks")
    fun getAll(): List<Drink>

    @Insert
    fun add(drink: Drink)

    @Query("UPDATE drinks SET favorite=:favorite WHERE id=:drinkId")
    fun setFavorite(drinkId: Long, favorite: Int)

    @Query("SELECT $drinkWithMissingCount WHERE di.drinkId in (SELECT drinkId from drink_ingredients WHERE bottleId=:bottleId) GROUP BY d.name ORDER BY 8,2")
    fun liveDrinksForBottle(bottleId: Long): LiveData<List<Drink>>

    @Query("SELECT $drinkWithMissingCount GROUP BY d.name ORDER BY 8,2")
    fun getAllPaged(): DataSource.Factory<Int, Drink>

    @Query("SELECT $drinkWithMissingCount WHERE d.favorite = 1 GROUP BY d.name ORDER BY 8,2")
    fun getFavoritesPaged(): DataSource.Factory<Int, Drink>

    @Query("SELECT $drinkWithMissingCount GROUP BY d.name ORDER BY d.name")
    fun getAlphabeticalPaged(): DataSource.Factory<Int, Drink>

    @Query("SELECT * FROM drinks WHERE id=:drinkId")
    fun liveById(drinkId: Long): LiveData<Drink>

    @Query("SELECT b.id bottleId, b.name bottleName, b.image bottleImage, b.active bottleActive, di.amount amount FROM drink_ingredients di INNER JOIN bottles b ON b.id = di.bottleId WHERE di.drinkId=:drinkId")
    fun liveIngredientsForDrink(drinkId: Long): LiveData<List<Ingredient>>

    @Query("SELECT COUNT(DISTINCT(di.drinkId)) FROM drink_ingredients di WHERE bottleId=:bottleId")
    fun liveDrinkCountForBottle(bottleId: Long): LiveData<Int>
}

@Dao
interface DrinkIngredientDao {

    @Insert
    fun add(drinkIngredient: DrinkIngredient)
}