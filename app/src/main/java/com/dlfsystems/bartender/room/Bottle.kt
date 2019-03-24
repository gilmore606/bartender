package com.dlfsystems.bartender.room

import androidx.paging.DataSource
import androidx.room.*

@Entity(tableName = "bottles",
        indices = arrayOf(
            Index(value = ["active"])
        ))
data class Bottle(@PrimaryKey val id: Long,
                        val name: String,
                        val image: String,
                        val active: Boolean = false,
                        val shopping: Boolean = false)


@Dao
interface BottleDao {

    @Query("SELECT * FROM bottles")
    fun getAll(): List<Bottle>

    @Query("SELECT * FROM bottles")
    fun getAllPaged(): DataSource.Factory<Int, Bottle>

    @Insert
    fun add(bottle: Bottle)


}