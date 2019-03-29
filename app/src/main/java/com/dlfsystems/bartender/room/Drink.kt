package com.dlfsystems.bartender.room

import androidx.annotation.Nullable
import androidx.lifecycle.LiveData
import androidx.paging.DataSource
import androidx.room.*

@Entity(tableName = "drinks")
data class Drink(@PrimaryKey val id: Long,
                       val name: String,
                        val favorite: Boolean = false,
                        val image: Int = 0,
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
                      val bottleImage: Int = 0,
                      val bottleActive: Boolean = false,
                      val amount: String = ""
                      )

@Dao
interface DrinkDao {

    @Query("SELECT * FROM drinks")
    fun getAll(): List<Drink>

    @Insert
    fun add(drink: Drink)

    @Query("UPDATE drinks SET favorite=:favorite WHERE id=:drinkId")
    fun setFavorite(drinkId: Long, favorite: Int)

    @Query("SELECT d.id id, d.name name, d.favorite favorite, d.image, d.info, d.make, d.garnish, count(di.bottleId) - sum(b.active) missingBottles FROM drink_ingredients di INNER JOIN bottles b ON di.bottleId = b.id INNER JOIN drinks d on di.drinkId = d.id WHERE di.drinkId in (SELECT drinkId from drink_ingredients WHERE bottleId=:bottleId) GROUP BY d.name ORDER BY 4,2")
    fun liveDrinksForBottle(bottleId: Long): LiveData<List<Drink>>

    @Query("SELECT d.id id, d.name name, d.favorite favorite, d.image, d.info, d.make, d.garnish, count(di.bottleId) - sum(b.active) missingBottles FROM drink_ingredients di INNER JOIN bottles b ON di.bottleId = b.id INNER JOIN drinks d on di.drinkId = d.id GROUP BY d.name ORDER BY 4,2")
    fun getAllPaged(): DataSource.Factory<Int, Drink>

    @Query("SELECT d.id id, d.name name, d.favorite favorite, d.image, d.info, d.make, d.garnish, count(di.bottleId) - sum(b.active) missingBottles FROM drink_ingredients di INNER JOIN bottles b ON di.bottleId = b.id INNER JOIN drinks d on di.drinkId = d.id WHERE d.favorite = 1 GROUP BY d.name ORDER BY 4,2")
    fun getFavoritesPaged(): DataSource.Factory<Int, Drink>

    @Query("SELECT d.id id, d.name name, d.favorite favorite, d.image, d.info, d.make, d.garnish, count(di.bottleId) - sum(b.active) missingBottles FROM drink_ingredients di INNER JOIN bottles b ON di.bottleId = b.id INNER JOIN drinks d on di.drinkId = d.id GROUP BY d.name ORDER BY d.name")
    fun getAlphabeticalPaged(): DataSource.Factory<Int, Drink>

    @Query("SELECT * FROM drinks WHERE id=:drinkId")
    fun liveById(drinkId: Long): LiveData<Drink>

    @Query("SELECT b.id bottleId, b.name bottleName, b.image bottleImage, b.active bottleActive, di.amount amount FROM drink_ingredients di INNER JOIN bottles b ON b.id = di.bottleId WHERE di.drinkId=:drinkId")
    fun liveIngredientsForDrink(drinkId: Long): LiveData<List<Ingredient>>
}

@Dao
interface DrinkIngredientDao {

    @Insert
    fun add(drinkIngredient: DrinkIngredient)
}