package com.example.data.api

import android.util.Log
import com.example.BuildConfig
import com.example.data.model.GeoPin
import com.example.data.model.GroundingSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

data class GeminiTurn(
    val role: String, // "user" or "model"
    val text: String
)

data class GeminiGenerationResult(
    val responseText: String,
    val groundingSources: List<GroundingSource>,
    val searchQueries: List<String>,
    val extractedPins: List<GeoPin>,
    val isError: Boolean = false
)

object GeminiApiClient {
    private const val TAG = "GeminiApiClient"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private const val SYSTEM_INSTRUCTION = """You are MapPulse, an elite interactive location intelligence and real-time mapping assistant.
You provide accurate, real-time, grounded location details, opening hours, local context, transit advice, fact-checking local claims, and geographical insights.
When answering about places or cities:
1. Always give precise, actionable details (exact neighborhood, opening hours, best times to visit, vibe, price range, historical context).
2. Whenever you recommend or reference specific places, attractions, restaurants, viewpoints, or venues, include a machine-readable tag in your response formatted exactly as:
[GEO: latitude, longitude, "Place Name", "Category", "Short Highlight"]
Example: [GEO: 37.8086, -122.4098, "Pier 39", "Sightseeing", "Lively waterfront with sea lions and street performers"]
3. If fact-checking a local rumor or news about a location, clearly state the verdict (TRUE, FALSE, or MISLEADING) and cite verified real-time sources."""

    suspend fun generateResponse(
        modelId: String,
        history: List<GeminiTurn>,
        currentPrompt: String,
        currentLocationContext: String? = null
    ): GeminiGenerationResult = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }

        if (apiKey.isNullOrBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext getMockResponse(modelId, currentPrompt, currentLocationContext)
        }

        try {
            val endpoint = "$BASE_URL$modelId:generateContent?key=$apiKey"

            val requestJson = JSONObject()

            // System instruction
            val systemInstructionObj = JSONObject()
            val sysPartsArray = JSONArray()
            sysPartsArray.put(JSONObject().put("text", SYSTEM_INSTRUCTION))
            systemInstructionObj.put("parts", sysPartsArray)
            requestJson.put("systemInstruction", systemInstructionObj)

            // Search Grounding Tool for gemini-3.5-flash
            if (modelId == "gemini-3.5-flash") {
                val toolsArray = JSONArray()
                val googleSearchTool = JSONObject()
                googleSearchTool.put("googleSearch", JSONObject())
                toolsArray.put(googleSearchTool)
                requestJson.put("tools", toolsArray)
            }

            // Generation config
            val genConfig = JSONObject()
            genConfig.put("temperature", 0.7)
            requestJson.put("generationConfig", genConfig)

            // Conversation contents
            val contentsArray = JSONArray()

            // Prior conversation turns (limit to last 8 turns to stay focused and fast)
            val recentHistory = history.takeLast(8)
            for (turn in recentHistory) {
                val turnObj = JSONObject()
                turnObj.put("role", if (turn.role == "model") "model" else "user")
                val parts = JSONArray()
                parts.put(JSONObject().put("text", turn.text))
                turnObj.put("parts", parts)
                contentsArray.put(turnObj)
            }

            // Current prompt with location context if available
            val promptText = if (!currentLocationContext.isNullOrBlank()) {
                "Location Context: $currentLocationContext\n\nQuestion: $currentPrompt"
            } else {
                currentPrompt
            }

            val currentTurnObj = JSONObject()
            currentTurnObj.put("role", "user")
            val currentParts = JSONArray()
            currentParts.put(JSONObject().put("text", promptText))
            currentTurnObj.put("parts", currentParts)
            contentsArray.put(currentTurnObj)

            requestJson.put("contents", contentsArray)

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val body = requestJson.toString().toRequestBody(mediaType)

            val request = Request.Builder()
                .url(endpoint)
                .post(body)
                .build()

            val response = okHttpClient.newCall(request).execute()
            val responseBodyString = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                Log.e(TAG, "API Error: ${response.code} - $responseBodyString")
                return@withContext GeminiGenerationResult(
                    responseText = "API Error (${response.code}): Unable to fetch data from Gemini. Please verify your GEMINI_API_KEY.",
                    groundingSources = emptyList(),
                    searchQueries = emptyList(),
                    extractedPins = emptyList(),
                    isError = true
                )
            }

            val responseObj = JSONObject(responseBodyString)
            val candidates = responseObj.optJSONArray("candidates")
            if (candidates == null || candidates.length() == 0) {
                return@withContext GeminiGenerationResult(
                    responseText = "No response generated for this query.",
                    groundingSources = emptyList(),
                    searchQueries = emptyList(),
                    extractedPins = emptyList(),
                    isError = false
                )
            }

            val firstCandidate = candidates.getJSONObject(0)
            val contentObj = firstCandidate.optJSONObject("content")
            val partsArray = contentObj?.optJSONArray("parts")

            val textBuilder = StringBuilder()
            if (partsArray != null) {
                for (i in 0 until partsArray.length()) {
                    val part = partsArray.getJSONObject(i)
                    if (part.has("text")) {
                        textBuilder.append(part.getString("text"))
                    }
                }
            }

            val rawText = textBuilder.toString()

            // Extract Grounding Metadata
            val groundingSources = mutableListOf<GroundingSource>()
            val searchQueries = mutableListOf<String>()

            val groundingMeta = firstCandidate.optJSONObject("groundingMetadata")
            if (groundingMeta != null) {
                // Search queries
                val queriesArray = groundingMeta.optJSONArray("webSearchQueries")
                if (queriesArray != null) {
                    for (i in 0 until queriesArray.length()) {
                        searchQueries.add(queriesArray.getString(i))
                    }
                }

                // Grounding Chunks
                val chunksArray = groundingMeta.optJSONArray("groundingChunks")
                if (chunksArray != null) {
                    for (i in 0 until chunksArray.length()) {
                        val chunk = chunksArray.getJSONObject(i)
                        val web = chunk.optJSONObject("web")
                        if (web != null) {
                            val uri = web.optString("uri", "")
                            val title = web.optString("title", "Google Search Reference")
                            if (uri.isNotBlank()) {
                                groundingSources.add(GroundingSource(title, uri))
                            }
                        }
                    }
                }
            }

            // Extract GEO pins from text
            val extractedPins = extractGeoPins(rawText)

            // Clean GEO tags from display text if desired, or keep them with nice formatting
            val cleanedText = cleanGeoTags(rawText)

            GeminiGenerationResult(
                responseText = cleanedText,
                groundingSources = groundingSources.distinctBy { it.url },
                searchQueries = searchQueries,
                extractedPins = extractedPins,
                isError = false
            )
        } catch (e: Exception) {
            Log.e(TAG, "Request failed", e)
            GeminiGenerationResult(
                responseText = "Request failed: ${e.localizedMessage ?: "Unknown network error"}. Please check your internet connection.",
                groundingSources = emptyList(),
                searchQueries = emptyList(),
                extractedPins = emptyList(),
                isError = true
            )
        }
    }

    private fun extractGeoPins(text: String): List<GeoPin> {
        val pins = mutableListOf<GeoPin>()
        // Pattern: [GEO: lat, lng, "Name", "Category", "Description"]
        val regex = Pattern.compile("\\[GEO:\\s*([-\\d.]+)\\s*,\\s*([-\\d.]+)\\s*,\\s*\"([^\"]+)\"\\s*,\\s*\"([^\"]+)\"\\s*,\\s*\"([^\"]+)\"\\]")
        val matcher = regex.matcher(text)
        var count = 0
        while (matcher.find()) {
            try {
                val lat = matcher.group(1)?.toDoubleOrNull() ?: continue
                val lng = matcher.group(2)?.toDoubleOrNull() ?: continue
                val name = matcher.group(3) ?: "Point of Interest"
                val category = matcher.group(4) ?: "Place"
                val desc = matcher.group(5) ?: ""
                count++
                pins.add(
                    GeoPin(
                        id = "pin_$count",
                        title = name,
                        description = desc,
                        lat = lat,
                        lng = lng,
                        category = category
                    )
                )
            } catch (e: Exception) {
                // Ignore parse errors
            }
        }
        return pins
    }

    private fun cleanGeoTags(text: String): String {
        return text.replace(Regex("\\[GEO:[^\\]]+\\]"), "").trim()
    }

    private fun getMockResponse(
        modelId: String,
        prompt: String,
        locationContext: String?
    ): GeminiGenerationResult {
        val loc = locationContext ?: "Selected Location"
        val samplePins = listOf(
            GeoPin("mock_1", "Central Plaza & Lookout", "Spectacular panoramic viewpoint and popular gathering spot.", 37.7749, -122.4194, "Sightseeing"),
            GeoPin("mock_2", "Artisan Roastery Cafe", "Locally-roasted espresso, fresh pastries, and fast Wi-Fi.", 37.7780, -122.4150, "Cafe"),
            GeoPin("mock_3", "Heritage Public Market", "Historic food hall with over 30 artisan culinary vendors.", 37.7810, -122.4220, "Food")
        )

        val sources = listOf(
            GroundingSource("Google Maps Places & Hours Guide", "https://maps.google.com"),
            GroundingSource("Live City Transit & Alerts Portal", "https://news.google.com")
        )

        val response = """### 📍 Location Intelligence Report ($loc)

Here is verified live context and recommendations:

1. **Top Highlights & Atmosphere**:
   - **Central Plaza**: Renowned for scenic vistas, public art installations, and active pedestrian promenade.
   - **Artisan Roastery**: Voted top cafe for remote work and single-origin pour-overs. Open daily 7:00 AM – 6:00 PM.
   - **Heritage Market**: Ideal for lunch and dinner with authentic regional flavors.

2. **Operating Hours & Peak Times**:
   - Most venues are open right now. Weekday mornings (9 AM - 11 AM) offer the lightest crowd density.

3. **Travel & Transit Tips**:
   - Easily accessible via public transit with dedicated bike lanes and rideshare drop-off zones nearby.

*(Notice: Add your real GEMINI_API_KEY in the AI Studio Secrets panel to unlock live Google Search grounding.)*"""

        return GeminiGenerationResult(
            responseText = response,
            groundingSources = sources,
            searchQueries = listOf("best places near $loc", "$loc live hours and reviews"),
            extractedPins = samplePins,
            isError = false
        )
    }
}
