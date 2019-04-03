package com.dlfsystems.bartender.room

import androidx.lifecycle.LiveData
import androidx.room.*

@Entity(tableName = "glasses")
data class Glass(@PrimaryKey val id: Long,
                 val name: String,
                 val resourcename: String)

@Dao
interface GlassDao {
    @Insert
    fun add(glass: Glass)

    @Query("SELECT * FROM glasses WHERE id=:glassId")
    fun byId(glassId: Long): Glass

    @Query("SELECT * FROM glasses WHERE id=:glassId")
    fun liveById(glassId: Long): LiveData<Glass>
}