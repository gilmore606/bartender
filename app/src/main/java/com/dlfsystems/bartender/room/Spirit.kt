package com.dlfsystems.bartender.room

import androidx.room.*

@Entity(tableName = "spirits")
data class Spirit(@ColumnInfo(name="id") @PrimaryKey var id: Long = 0,
                @ColumnInfo(name="name") var name: String,
                  @ColumnInfo(name="image") var image: String)

@Dao
interface SpiritDao {

    @Query("SELECT * FROM spirits")
    fun getAll(): List<Spirit>

    @Insert
    fun add(spirit: Spirit)
}