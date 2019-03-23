package com.dlfsystems.bartender.room

import androidx.room.*

@Entity(tableName = "bottles",
        foreignKeys = arrayOf(ForeignKey(
            entity = Spirit::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("spiritId")
        )))
data class Bottle(@PrimaryKey(autoGenerate = true) val id: Long,
                        val name: String,
                        val spiritId: Long,
                        val active: Boolean)


@Dao
interface BottleDao {

    @Query("SELECT * FROM bottles")
    fun getAll(): List<Bottle>

    @Insert
    fun addBottle(bottle: Bottle)
}