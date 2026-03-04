package com.example.gaspricesnearme

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface StationDao {

    @Query("SELECT * FROM stations")
    suspend fun getAll(): List<StationEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(stations: List<StationEntity>)

    @Query("DELETE FROM stations")
    suspend fun clear()

    @Query("SELECT * FROM stations WHERE coordinates = :coords LIMIT 1")
    suspend fun getStation(coords: String): StationEntity?
}