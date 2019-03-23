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
            entity = Spirit::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("spiritId")
        )))
data class DrinkIngredient(@PrimaryKey(autoGenerate = true) val id: Long,
                            val drinkId: Long,
                            val spiritId: Long,
                            val amount: Float)
@Dao
interface DrinkDao {

    @Query("SELECT * FROM drinks")
    fun getAll(): List<Drink>

    @Insert
    fun addDrink(drink: Drink)
}
