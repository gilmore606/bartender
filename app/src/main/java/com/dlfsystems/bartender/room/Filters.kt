package com.dlfsystems.bartender.room

import androidx.room.*

@Entity(tableName = "filter")
data class Filter(@PrimaryKey(autoGenerate = true) val id: Long,
                    val kind: String,
                    val filter: Int)

@Dao
interface FilterDao {
    @Insert
    fun add(filter: Filter)

    @Query("DELETE FROM filter WHERE kind=:kind")
    fun removeAll(kind: String)

    @Query("UPDATE filter SET filter=:filter WHERE kind=:kind")
    fun set(kind: String, filter: Int)
}
