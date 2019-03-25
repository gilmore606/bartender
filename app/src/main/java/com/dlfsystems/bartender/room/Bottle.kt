package com.dlfsystems.bartender.room

import androidx.paging.DataSource
import androidx.room.*

@Entity(tableName = "bottles",
        indices = arrayOf(
            Index(value = ["active"])
        ))
data class Bottle(@PrimaryKey val id: Long,
                        val name: String,
                        val image: Int,
                        val active: Boolean = false,
                        val shopping: Boolean = false)


@Dao
interface BottleDao {

    @Query("SELECT * FROM bottles")
    fun getAll(): List<Bottle>

    @Query("SELECT * FROM bottles")
    fun getAllPaged(): DataSource.Factory<Int, Bottle>

    @Query("SELECT * FROM bottles WHERE active=1")
    fun getActive(): List<Bottle>

    @Query("SELECT * FROM bottles WHERE active=1")
    fun getActivePaged(): DataSource.Factory<Int, Bottle>

    @Query("SELECT * FROM bottles WHERE shopping=1")
    fun getShoppingPaged(): DataSource.Factory<Int, Bottle>

    @Insert
    fun add(bottle: Bottle)

    @Update
    fun update(bottle: Bottle)

    @Query("UPDATE bottles SET active=:active WHERE id=:bottleId")
    fun setActive(bottleId: Long, active: Int)

}