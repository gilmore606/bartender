package com.dlfsystems.bartender.room

import androidx.room.*

@Entity(tableName = "drinks")
data class Drink(@PrimaryKey(autoGenerate = true) val id: Long,
                       val name: String)

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
data class DrinkIngredient(@PrimaryKey(autoGenerate = true) val id: Long,
                            val drinkId: Long,
                            val bottleId: Long,
                            val amount: String)
@Dao
interface DrinkDao {

    @Query("SELECT * FROM drinks")
    fun getAll(): List<Drink>

    @Insert
    fun add(drink: Drink)
}
