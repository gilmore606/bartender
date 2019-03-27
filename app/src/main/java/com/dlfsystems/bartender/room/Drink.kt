package com.dlfsystems.bartender.room

import androidx.lifecycle.LiveData
import androidx.paging.DataSource
import androidx.room.*

@Entity(tableName = "drinks")
data class Drink(@PrimaryKey val id: Long,
                       val name: String,
                        val favorite: Boolean = false)

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

    @Query("SELECT * FROM drinks")
    fun getAllPaged(): DataSource.Factory<Int, Drink>

    @Query("SELECT * FROM drinks WHERE favorite=1")
    fun getFavoritesPaged(): DataSource.Factory<Int, Drink>

    @Insert
    fun add(drink: Drink)

    @Query("UPDATE drinks SET favorite=:favorite WHERE id=:drinkId")
    fun setFavorite(drinkId: Long, favorite: Int)

    @Query("SELECT * FROM drinks LIMIT 5")
    fun liveDrinksForBottle(): LiveData<List<Drink>>
}

@Dao
interface DrinkIngredientDao {

    @Insert
    fun add(drinkIngredient: DrinkIngredient)
}