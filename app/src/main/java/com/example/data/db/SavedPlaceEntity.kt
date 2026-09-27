package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_places")
data class SavedPlaceEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val category: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val notes: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
