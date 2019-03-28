package com.dlfsystems.bartender.room

import androidx.annotation.Nullable
import androidx.lifecycle.LiveData
import androidx.paging.DataSource
import androidx.room.*

@Entity(tableName = "drinks")
data class Drink(@PrimaryKey val id: Long,
                       val name: String,
                        val favorite: Boolean = false,
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
@Dao
interface DrinkDao {

    @Query("SELECT * FROM drinks")
    fun getAll(): List<Drink>

    @Insert
    fun add(drink: Drink)

    @Query("UPDATE drinks SET favorite=:favorite WHERE id=:drinkId")
    fun setFavorite(drinkId: Long, favorite: Int)

    @Query("SELECT d.id, d.name, d.favorite, count(di.bottleId) - sum(b.active) missingBottles FROM drink_ingredients di INNER JOIN bottles b ON di.bottleId = b.id INNER JOIN drinks d on di.drinkId = d.id WHERE di.drinkId in (SELECT drinkId from drink_ingredients WHERE bottleId=:bottleId) GROUP BY d.name ORDER BY 4,2")
    fun liveDrinksForBottle(bottleId: Long): LiveData<List<Drink>>

    @Query("SELECT d.id, d.name, d.favorite, count(di.bottleId) - sum(b.active) missingBottles FROM drink_ingredients di INNER JOIN bottles b ON di.bottleId = b.id INNER JOIN drinks d on di.drinkId = d.id GROUP BY d.name ORDER BY 4,2")
    fun getAllPaged(): DataSource.Factory<Int, Drink>

    @Query("SELECT d.id, d.name, d.favorite, count(di.bottleId) - sum(b.active) missingBottles FROM drink_ingredients di INNER JOIN bottles b ON di.bottleId = b.id INNER JOIN drinks d on di.drinkId = d.id WHERE d.favorite = 1 GROUP BY d.name ORDER BY 4,2")
    fun getFavoritesPaged(): DataSource.Factory<Int, Drink>

    @Query("SELECT * FROM drinks WHERE id=:drinkId")
    fun liveById(drinkId: Long): LiveData<Drink>
}

@Dao
interface DrinkIngredientDao {

    @Insert
    fun add(drinkIngredient: DrinkIngredient)
}