package com.example.data.repository

import com.example.data.api.GeminiApiClient
import com.example.data.api.GeminiGenerationResult
import com.example.data.api.GeminiTurn
import com.example.data.db.AppDatabase
import com.example.data.db.ChatMessageEntity
import com.example.data.db.SavedPlaceEntity
import com.example.data.model.GeoPin
import com.example.data.model.GroundingSource
import kotlinx.coroutines.flow.Flow
import org.json.JSONArray
import org.json.JSONObject

class MapChatRepository(private val database: AppDatabase) {
    val allMessages: Flow<List<ChatMessageEntity>> = database.chatDao().getAllMessages()
    val savedPlaces: Flow<List<SavedPlaceEntity>> = database.savedPlaceDao().getAllSavedPlaces()

    suspend fun saveUserMessage(
        content: String,
        modelId: String
    ): Long {
        val entity = ChatMessageEntity(
            role = "user",
            content = content,
            modelId = modelId
        )
        return database.chatDao().insertMessage(entity)
    }

    suspend fun sendQueryToGemini(
        modelId: String,
        allHistory: List<ChatMessageEntity>,
        userPrompt: String,
        locationContext: String?
    ): GeminiGenerationResult {
        // Map history to GeminiTurn
        val turns = allHistory.map {
            GeminiTurn(role = it.role, text = it.content)
        }

        val result = GeminiApiClient.generateResponse(
            modelId = modelId,
            history = turns,
            currentPrompt = userPrompt,
            currentLocationContext = locationContext
        )

        // Save model response to Room DB
        val entity = ChatMessageEntity(
            role = "model",
            content = result.responseText,
            modelId = modelId,
            groundingSourcesJson = serializeGroundingSources(result.groundingSources),
            searchQueriesJson = serializeSearchQueries(result.searchQueries),
            geoPinsJson = serializeGeoPins(result.extractedPins),
            isError = result.isError
        )
        database.chatDao().insertMessage(entity)

        return result
    }

    suspend fun clearHistory() {
        database.chatDao().clearAllMessages()
    }

    suspend fun deleteMessage(id: Long) {
        database.chatDao().deleteMessage(id)
    }

    suspend fun savePlace(
        name: String,
        category: String,
        address: String,
        latitude: Double,
        longitude: Double,
        notes: String = ""
    ): Long {
        val entity = SavedPlaceEntity(
            name = name,
            category = category,
            address = address,
            latitude = latitude,
            longitude = longitude,
            notes = notes
        )
        return database.savedPlaceDao().insertPlace(entity)
    }

    suspend fun deleteSavedPlace(id: Long) {
        database.savedPlaceDao().deletePlace(id)
    }

    private fun serializeGroundingSources(sources: List<GroundingSource>): String {
        val array = JSONArray()
        for (source in sources) {
            val obj = JSONObject()
            obj.put("title", source.title)
            obj.put("url", source.url)
            array.put(obj)
        }
        return array.toString()
    }

    fun deserializeGroundingSources(json: String): List<GroundingSource> {
        if (json.isBlank()) return emptyList()
        val list = mutableListOf<GroundingSource>()
        try {
            val array = JSONArray(json)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(GroundingSource(obj.optString("title"), obj.optString("url")))
            }
        } catch (e: Exception) {
            // Ignore parse errors
        }
        return list
    }

    private fun serializeSearchQueries(queries: List<String>): String {
        val array = JSONArray()
        for (q in queries) array.put(q)
        return array.toString()
    }

    fun deserializeSearchQueries(json: String): List<String> {
        if (json.isBlank()) return emptyList()
        val list = mutableListOf<String>()
        try {
            val array = JSONArray(json)
            for (i in 0 until array.length()) {
                list.add(array.getString(i))
            }
        } catch (e: Exception) {
            // Ignore parse errors
        }
        return list
    }

    private fun serializeGeoPins(pins: List<GeoPin>): String {
        val array = JSONArray()
        for (pin in pins) {
            val obj = JSONObject()
            obj.put("id", pin.id)
            obj.put("title", pin.title)
            obj.put("description", pin.description)
            obj.put("lat", pin.lat)
            obj.put("lng", pin.lng)
            obj.put("category", pin.category)
            array.put(obj)
        }
        return array.toString()
    }

    fun deserializeGeoPins(json: String): List<GeoPin> {
        if (json.isBlank()) return emptyList()
        val list = mutableListOf<GeoPin>()
        try {
            val array = JSONArray(json)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    GeoPin(
                        id = obj.optString("id"),
                        title = obj.optString("title"),
                        description = obj.optString("description"),
                        lat = obj.optDouble("lat"),
                        lng = obj.optDouble("lng"),
                        category = obj.optString("category")
                    )
                )
            }
        } catch (e: Exception) {
            // Ignore parse errors
        }
        return list
    }
}
