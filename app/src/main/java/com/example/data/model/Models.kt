package com.example.data.model

data class GeoPin(
    val id: String,
    val title: String,
    val description: String,
    val lat: Double,
    val lng: Double,
    val category: String = "place"
)

data class GroundingSource(
    val title: String,
    val url: String
)

data class GeminiModelInfo(
    val id: String,
    val displayName: String,
    val shortName: String,
    val description: String,
    val badge: String,
    val supportsSearchGrounding: Boolean
)

val AVAILABLE_MODELS = listOf(
    GeminiModelInfo(
        id = "gemini-3.5-flash",
        displayName = "Gemini 3.5 Flash",
        shortName = "3.5 Flash",
        description = "Real-time Google Search Grounding for live hours, reviews, local news, and facts.",
        badge = "LIVE SEARCH",
        supportsSearchGrounding = true
    ),
    GeminiModelInfo(
        id = "gemini-3.1-pro-preview",
        displayName = "Gemini 3.1 Pro",
        shortName = "3.1 Pro",
        description = "Advanced reasoning for complex itineraries, deep history, and architectural analysis.",
        badge = "COMPLEX REASONING",
        supportsSearchGrounding = false
    ),
    GeminiModelInfo(
        id = "gemini-3.1-flash-lite-preview",
        displayName = "Gemini 3.1 Flash Lite",
        shortName = "Flash Lite",
        description = "Lightning-fast responses for quick translation, directions, and instant Q&A.",
        badge = "TURBO SPEED",
        supportsSearchGrounding = false
    )
)

data class CityPreset(
    val name: String,
    val country: String,
    val lat: Double,
    val lng: Double,
    val emoji: String
)

val POPULAR_CITIES = listOf(
    CityPreset("San Francisco", "USA", 37.7749, -122.4194, "🌁"),
    CityPreset("Tokyo", "Japan", 35.6762, 139.6503, "🗼"),
    CityPreset("Paris", "France", 48.8566, 2.3522, "🥐"),
    CityPreset("New York", "USA", 40.7128, -74.0060, "🗽"),
    CityPreset("London", "UK", 51.5074, -0.1278, "💂"),
    CityPreset("Rome", "Italy", 41.9028, 12.4964, "🏛️"),
    CityPreset("Sydney", "Australia", -33.8688, 151.2093, "🦘"),
    CityPreset("Dubai", "UAE", 25.2048, 55.2708, "🏙️")
)

enum class AppViewMode {
    SPLIT,
    FULL_MAP,
    FULL_CHAT
}
