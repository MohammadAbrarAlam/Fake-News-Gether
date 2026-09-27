package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.db.ChatMessageEntity
import com.example.data.db.SavedPlaceEntity
import com.example.data.model.AVAILABLE_MODELS
import com.example.data.model.AppViewMode
import com.example.data.model.CityPreset
import com.example.data.model.GeminiModelInfo
import com.example.data.model.GeoPin
import com.example.data.repository.MapChatRepository
import com.example.util.LocationHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class MapUiState(
    val currentLat: Double = 37.7749,
    val currentLng: Double = -122.4194,
    val currentLocationName: String = "San Francisco, CA",
    val selectedModel: GeminiModelInfo = AVAILABLE_MODELS.first(),
    val isModelTyping: Boolean = false,
    val activePins: List<GeoPin> = emptyList(),
    val selectedPin: GeoPin? = null,
    val viewMode: AppViewMode = AppViewMode.SPLIT,
    val inputText: String = "",
    val pendingFlyTo: Pair<Double, Double>? = null,
    val statusMessage: String? = null
)

class MapChatViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = MapChatRepository(AppDatabase.getDatabase(application))

    val messages: StateFlow<List<ChatMessageEntity>> = repository.allMessages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val savedPlaces: StateFlow<List<SavedPlaceEntity>> = repository.savedPlaces
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    init {
        // Observe messages to extract active pins from latest responses
        viewModelScope.launch {
            messages.collect { messageList ->
                val latestModelMsg = messageList.lastOrNull { it.role == "model" }
                if (latestModelMsg != null && latestModelMsg.geoPinsJson.isNotBlank()) {
                    val pins = repository.deserializeGeoPins(latestModelMsg.geoPinsJson)
                    if (pins.isNotEmpty()) {
                        _uiState.value = _uiState.value.copy(activePins = pins)
                    }
                }
            }
        }
    }

    fun onInputTextChanged(newText: String) {
        _uiState.value = _uiState.value.copy(inputText = newText)
    }

    fun setViewMode(mode: AppViewMode) {
        _uiState.value = _uiState.value.copy(viewMode = mode)
    }

    fun setModel(model: GeminiModelInfo) {
        _uiState.value = _uiState.value.copy(selectedModel = model)
    }

    fun selectLocationOnMap(lat: Double, lng: Double) {
        viewModelScope.launch {
            val address = LocationHelper.getAddressFromCoordinates(getApplication(), lat, lng)
            _uiState.value = _uiState.value.copy(
                currentLat = lat,
                currentLng = lng,
                currentLocationName = address,
                selectedPin = null
            )
        }
    }

    fun flyToCity(city: CityPreset) {
        _uiState.value = _uiState.value.copy(
            currentLat = city.lat,
            currentLng = city.lng,
            currentLocationName = "${city.name}, ${city.country}",
            pendingFlyTo = Pair(city.lat, city.lng)
        )
    }

    fun flyToPin(pin: GeoPin) {
        _uiState.value = _uiState.value.copy(
            currentLat = pin.lat,
            currentLng = pin.lng,
            currentLocationName = pin.title,
            selectedPin = pin,
            pendingFlyTo = Pair(pin.lat, pin.lng)
        )
    }

    fun clearPendingFlyTo() {
        _uiState.value = _uiState.value.copy(pendingFlyTo = null)
    }

    fun selectPin(pin: GeoPin) {
        _uiState.value = _uiState.value.copy(selectedPin = pin)
    }

    fun dismissPin() {
        _uiState.value = _uiState.value.copy(selectedPin = null)
    }

    fun submitPrompt(prompt: String = _uiState.value.inputText) {
        val trimmed = prompt.trim()
        if (trimmed.isBlank() || _uiState.value.isModelTyping) return

        _uiState.value = _uiState.value.copy(
            inputText = "",
            isModelTyping = true,
            statusMessage = "MapPulse is querying real-time location data..."
        )

        viewModelScope.launch {
            try {
                // Save user message
                repository.saveUserMessage(
                    content = trimmed,
                    modelId = _uiState.value.selectedModel.id
                )

                val locContext = "${_uiState.value.currentLocationName} (Coordinates: ${_uiState.value.currentLat}, ${_uiState.value.currentLng})"

                val result = repository.sendQueryToGemini(
                    modelId = _uiState.value.selectedModel.id,
                    allHistory = messages.value,
                    userPrompt = trimmed,
                    locationContext = locContext
                )

                if (result.extractedPins.isNotEmpty()) {
                    _uiState.value = _uiState.value.copy(
                        activePins = result.extractedPins,
                        pendingFlyTo = Pair(result.extractedPins[0].lat, result.extractedPins[0].lng)
                    )
                }
            } catch (e: Exception) {
                // handled in repository
            } finally {
                _uiState.value = _uiState.value.copy(
                    isModelTyping = false,
                    statusMessage = null
                )
            }
        }
    }

    fun quickAsk(presetQuery: String) {
        val loc = _uiState.value.currentLocationName
        val query = when (presetQuery) {
            "cafes" -> "What are the top artisan coffee shops and work cafes in $loc right now? Include opening hours and vibes."
            "food" -> "What are the best trending restaurants and authentic local dining spots near $loc?"
            "sights" -> "What are the top must-visit landmarks, historic architecture, and viewpoints in $loc?"
            "fact_check" -> "Fact-check: What are the current verified safety, travel advisories, or viral claims regarding $loc?"
            "transit" -> "How is public transit, parking, and navigability around $loc right now?"
            else -> presetQuery
        }
        submitPrompt(query)
    }

    fun askAboutSelectedPoint() {
        val loc = _uiState.value.currentLocationName
        submitPrompt("Tell me everything about $loc: what's here, historical background, best things to do, and practical visitor tips.")
    }

    fun savePlace(pin: GeoPin) {
        viewModelScope.launch {
            repository.savePlace(
                name = pin.title,
                category = pin.category,
                address = _uiState.value.currentLocationName,
                latitude = pin.lat,
                longitude = pin.lng,
                notes = pin.description
            )
        }
    }

    fun deleteSavedPlace(id: Long) {
        viewModelScope.launch {
            repository.deleteSavedPlace(id)
        }
    }

    fun clearChat() {
        viewModelScope.launch {
            repository.clearHistory()
            _uiState.value = _uiState.value.copy(activePins = emptyList(), selectedPin = null)
        }
    }

    fun deleteMessage(id: Long) {
        viewModelScope.launch {
            repository.deleteMessage(id)
        }
    }

    fun fetchCurrentGpsLocation() {
        viewModelScope.launch {
            val loc = LocationHelper.getCurrentLocation(getApplication())
            if (loc != null) {
                val addr = LocationHelper.getAddressFromCoordinates(getApplication(), loc.first, loc.second)
                _uiState.value = _uiState.value.copy(
                    currentLat = loc.first,
                    currentLng = loc.second,
                    currentLocationName = addr,
                    pendingFlyTo = Pair(loc.first, loc.second)
                )
            }
        }
    }

    fun deserializeSources(json: String) = repository.deserializeGroundingSources(json)
    fun deserializeQueries(json: String) = repository.deserializeSearchQueries(json)
    fun deserializePins(json: String) = repository.deserializeGeoPins(json)
}
