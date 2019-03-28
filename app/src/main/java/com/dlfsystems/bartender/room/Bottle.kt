package com.dlfsystems.bartender.room

import androidx.lifecycle.LiveData
import androidx.paging.DataSource
import androidx.room.*

@Entity(tableName = "bottles",
        indices = arrayOf(
            Index(value = ["active"]),
            Index(value = ["name"])
        ))
data class Bottle(@PrimaryKey val id: Long,
                        val name: String,
                        val image: Int = 0,
                        val desc: Int = 0,
                        val type: Long = 1,
                        val active: Boolean = false,
                        val shopping: Boolean = false)


@Dao
interface BottleDao {

    @Query("SELECT * FROM bottles")
    fun getAll(): List<Bottle>

    @Query("SELECT * FROM bottles ORDER BY name")
    fun getAllPaged(): DataSource.Factory<Int, Bottle>

    @Query("SELECT * FROM bottles WHERE active=1")
    fun getActive(): List<Bottle>

    @Query("SELECT * FROM bottles WHERE active=1 ORDER BY NAME")
    fun getActivePaged(): DataSource.Factory<Int, Bottle>

    @Query("SELECT * FROM bottles WHERE shopping=1 ORDER BY NAME")
    fun getShoppingPaged(): DataSource.Factory<Int, Bottle>

    @Insert
    fun add(bottle: Bottle)

    @Update
    fun update(bottle: Bottle)

    @Query("UPDATE bottles SET active=:active WHERE id=:bottleId")
    fun setActive(bottleId: Long, active: Int)

    @Query("UPDATE bottles SET shopping=:shopping WHERE id=:bottleId")
    fun setShopping(bottleId: Long, shopping: Int)

    @Query("SELECT * FROM bottles WHERE id=:bottleId")
    fun byId(bottleId: Long): Bottle

    @Query("SELECT * FROM bottles WHERE id=:bottleId")
    fun liveById(bottleId: Long): LiveData<Bottle>

    @Query("SELECT b.* FROM bottles b INNER JOIN drink_ingredients di ON b.id = di.bottleId WHERE di.drinkId=:drinkId")
    fun liveBottlesForDrink(drinkId: Long): LiveData<List<Bottle>>
}