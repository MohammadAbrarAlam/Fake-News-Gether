package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedPlaceDao {
    @Query("SELECT * FROM saved_places ORDER BY timestamp DESC")
    fun getAllSavedPlaces(): Flow<List<SavedPlaceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlace(place: SavedPlaceEntity): Long

    @Query("DELETE FROM saved_places WHERE id = :id")
    suspend fun deletePlace(id: Long)

    @Query("SELECT * FROM saved_places WHERE latitude BETWEEN :minLat AND :maxLat AND longitude BETWEEN :minLng AND :maxLng")
    suspend fun getNearbyPlaces(minLat: Double, maxLat: Double, minLng: Double, maxLng: Double): List<SavedPlaceEntity>
}
