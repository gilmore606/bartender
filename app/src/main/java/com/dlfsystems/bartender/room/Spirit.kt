package com.dlfsystems.bartender.room

import androidx.room.*

@Entity(tableName = "spirits")
data class Spirit(@ColumnInfo(name="id") @PrimaryKey(autoGenerate = true) var id: Long = 0,
                @ColumnInfo(name="name") var name: String)

@Dao
interface SpiritDao {

    @Query("SELECT * FROM spirits")
    fun getAll(): List<Spirit>

    @Insert
    fun add(spirit: Spirit)
}