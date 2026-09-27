package com.example.ui.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VerticalSplit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppViewMode
import com.example.data.model.GeoPin
import com.example.data.model.POPULAR_CITIES
import com.example.ui.components.ChatBubbleItem
import com.example.ui.components.MapWebView
import com.example.ui.components.ModelSelectionDialog
import com.example.ui.components.SavedPlacesDialog
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.MapDarkBg
import com.example.ui.theme.MapSurface
import com.example.ui.theme.MapSurfaceBorder
import com.example.ui.theme.MapSurfaceVariant
import com.example.ui.theme.RedAlert
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.LocationHelper
import com.example.viewmodel.MapChatViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: MapChatViewModel) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val messages by viewModel.messages.collectAsState()
    val savedPlaces by viewModel.savedPlaces.collectAsState()

    var showModelDialog by remember { mutableStateOf(false) }
    var showSavedPlacesDialog by remember { mutableStateOf(false) }
    var showClearConfirmDialog by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()

    // Auto-scroll chat to bottom on new messages
    LaunchedEffect(messages.size, uiState.isModelTyping) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    // Permission launcher for Location
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        if (fineGranted || coarseGranted) {
            viewModel.fetchCurrentGpsLocation()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Brush.linearGradient(listOf(CyanNeon, Color(0xFF0077FF)))),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Explore,
                                contentDescription = null,
                                tint = Color(0xFF070D18),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "MapPulse",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    color = Color(0xFF003847),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = "AI MAP",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = CyanNeon,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                            Text(
                                text = "Real-Time Google Search Grounding",
                                fontSize = 10.sp,
                                color = EmeraldGreen
                            )
                        }
                    }
                },
                actions = {
                    // Model switcher chip
                    Surface(
                        color = MapSurfaceVariant,
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, Color(0xFF1E3A6E)),
                        modifier = Modifier
                            .clickable { showModelDialog = true }
                            .padding(end = 4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.SmartToy,
                                contentDescription = "Model",
                                tint = CyanNeon,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = uiState.selectedModel.shortName,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                        }
                    }

                    // GPS Locate Button
                    IconButton(
                        onClick = {
                            locationPermissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                            )
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.MyLocation,
                            contentDescription = "My GPS Location",
                            tint = CyanNeon
                        )
                    }

                    // Saved Places
                    IconButton(onClick = { showSavedPlacesDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Bookmark,
                            contentDescription = "Saved Places",
                            tint = if (savedPlaces.isNotEmpty()) EmeraldGreen else TextSecondary
                        )
                    }

                    // Clear Chat
                    IconButton(onClick = { showClearConfirmDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.ClearAll,
                            contentDescription = "Clear Chat",
                            tint = TextMuted
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MapDarkBg,
                    titleContentColor = TextPrimary
                )
            )
        },
        containerColor = MapDarkBg
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .imePadding()
        ) {
            // View Mode Selector & City Quick-Switch Bar
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MapSurface)
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                // View Mode Segmented Control
                Row(
                    modifier = Modifier
                        .background(Color(0xFF070D18), RoundedCornerShape(8.dp))
                        .padding(2.dp)
                ) {
                    ViewModeButton(
                        icon = Icons.Default.VerticalSplit,
                        label = "Split",
                        isSelected = uiState.viewMode == AppViewMode.SPLIT,
                        onClick = { viewModel.setViewMode(AppViewMode.SPLIT) }
                    )
                    ViewModeButton(
                        icon = Icons.Default.Map,
                        label = "Map",
                        isSelected = uiState.viewMode == AppViewMode.FULL_MAP,
                        onClick = { viewModel.setViewMode(AppViewMode.FULL_MAP) }
                    )
                    ViewModeButton(
                        icon = Icons.Default.Forum,
                        label = "Chat",
                        isSelected = uiState.viewMode == AppViewMode.FULL_CHAT,
                        onClick = { viewModel.setViewMode(AppViewMode.FULL_CHAT) }
                    )
                }

                // Selected Location Chip
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF070D18))
                        .clickable { viewModel.askAboutSelectedPoint() }
                        .padding(horizontal = 8.dp, vertical = 5.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = CyanNeon,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = uiState.currentLocationName,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.widthIn(max = 140.dp)
                    )
                }
            }

            // City Quick Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MapSurface)
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                POPULAR_CITIES.forEach { city ->
                    Surface(
                        color = Color(0xFF132238),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(0.5.dp, Color(0xFF1E3A6E)),
                        modifier = Modifier.clickable { viewModel.flyToCity(city) }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(text = city.emoji, fontSize = 12.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = city.name, fontSize = 11.sp, color = TextPrimary)
                        }
                    }
                }
            }

            Divider(color = MapSurfaceBorder, thickness = 0.5.dp)

            // Main Content Area based on ViewMode
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                when (uiState.viewMode) {
                    AppViewMode.SPLIT -> {
                        Column(modifier = Modifier.fillMaxSize()) {
                            // Map occupies top 42%
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(0.42f)
                            ) {
                                MapWebView(
                                    currentLat = uiState.currentLat,
                                    currentLng = uiState.currentLng,
                                    activePins = uiState.activePins,
                                    pendingFlyTo = uiState.pendingFlyTo,
                                    onLocationSelected = { lat, lng ->
                                        viewModel.selectLocationOnMap(lat, lng)
                                    },
                                    onAskAboutLocation = { lat, lng ->
                                        viewModel.selectLocationOnMap(lat, lng)
                                        viewModel.askAboutSelectedPoint()
                                    },
                                    onPoiSelected = { pin ->
                                        viewModel.selectPin(pin)
                                    },
                                    onPendingFlyToConsumed = {
                                        viewModel.clearPendingFlyTo()
                                    }
                                )

                                // Overlay badge for map touch hint
                                Surface(
                                    color = Color(0xCC070D18),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(8.dp)
                                ) {
                                    Text(
                                        text = "Tap map to pin point",
                                        fontSize = 10.sp,
                                        color = TextSecondary,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Divider(color = CyanNeon, thickness = 1.dp)

                            // Chat occupies bottom 58%
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(0.58f)
                            ) {
                                ChatThreadSection(
                                    viewModel = viewModel,
                                    listState = listState,
                                    messages = messages
                                )
                            }
                        }
                    }

                    AppViewMode.FULL_MAP -> {
                        Box(modifier = Modifier.fillMaxSize()) {
                            MapWebView(
                                currentLat = uiState.currentLat,
                                currentLng = uiState.currentLng,
                                activePins = uiState.activePins,
                                pendingFlyTo = uiState.pendingFlyTo,
                                onLocationSelected = { lat, lng ->
                                    viewModel.selectLocationOnMap(lat, lng)
                                },
                                onAskAboutLocation = { lat, lng ->
                                    viewModel.selectLocationOnMap(lat, lng)
                                    viewModel.askAboutSelectedPoint()
                                },
                                onPoiSelected = { pin ->
                                    viewModel.selectPin(pin)
                                },
                                onPendingFlyToConsumed = {
                                    viewModel.clearPendingFlyTo()
                                }
                            )

                            // Floating pill to query about current map center
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xEE0B172A)),
                                border = BorderStroke(1.dp, CyanNeon),
                                shape = RoundedCornerShape(20.dp),
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(bottom = 12.dp)
                                    .clickable { viewModel.askAboutSelectedPoint() }
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = CyanNeon,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Ask MapPulse AI about ${uiState.currentLocationName}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }

                    AppViewMode.FULL_CHAT -> {
                        ChatThreadSection(
                            viewModel = viewModel,
                            listState = listState,
                            messages = messages
                        )
                    }
                }
            }

            // Quick Category Prompts Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MapSurface)
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                QuickPromptChip(
                    emoji = "☕",
                    text = "Work Cafes",
                    onClick = { viewModel.quickAsk("cafes") }
                )
                QuickPromptChip(
                    emoji = "🍽️",
                    text = "Top Food",
                    onClick = { viewModel.quickAsk("food") }
                )
                QuickPromptChip(
                    emoji = "🏛️",
                    text = "Sights",
                    onClick = { viewModel.quickAsk("sights") }
                )
                QuickPromptChip(
                    emoji = "🚨",
                    text = "Fact-Check",
                    onClick = { viewModel.quickAsk("fact_check") }
                )
                QuickPromptChip(
                    emoji = "🕒",
                    text = "Live Hours",
                    onClick = { viewModel.quickAsk("transit") }
                )
            }

            // Model Typing Banner
            AnimatedVisibility(visible = uiState.isModelTyping) {
                val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                val alpha by infiniteTransition.animateFloat(
                    initialValue = 0.5f,
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(600),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "alpha"
                )

                Surface(
                    color = Color(0xFF003847),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                            .alpha(alpha)
                    ) {
                        CircularProgressIndicator(
                            color = CyanNeon,
                            modifier = Modifier.size(14.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = uiState.statusMessage ?: "Grounding location with Google Search...",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = CyanNeon
                        )
                    }
                }
            }

            // Bottom Chat Input Bar
            Surface(
                color = MapDarkBg,
                border = BorderStroke(1.dp, MapSurfaceBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                ) {
                    OutlinedTextField(
                        value = uiState.inputText,
                        onValueChange = { viewModel.onInputTextChanged(it) },
                        placeholder = {
                            Text(
                                text = "Ask about places, live hours, safety, history...",
                                color = TextMuted,
                                fontSize = 13.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(onSend = { viewModel.submitPrompt() }),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanNeon,
                            unfocusedBorderColor = MapSurfaceBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            cursorColor = CyanNeon,
                            focusedContainerColor = MapSurface,
                            unfocusedContainerColor = MapSurface
                        ),
                        shape = RoundedCornerShape(20.dp),
                        singleLine = false,
                        maxLines = 3,
                        modifier = Modifier
                            .testTag("chat_input_field")
                            .weight(1f)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = { viewModel.submitPrompt() },
                        enabled = uiState.inputText.isNotBlank() && !uiState.isModelTyping,
                        modifier = Modifier
                            .testTag("submit_button")
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(
                                if (uiState.inputText.isNotBlank() && !uiState.isModelTyping)
                                    Brush.linearGradient(listOf(CyanNeon, Color(0xFF0077FF)))
                                else Brush.linearGradient(listOf(Color(0xFF1E293B), Color(0xFF1E293B)))
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Send",
                            tint = if (uiState.inputText.isNotBlank() && !uiState.isModelTyping) Color(0xFF070D18) else TextMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }

    // Dialogs
    if (showModelDialog) {
        ModelSelectionDialog(
            currentModel = uiState.selectedModel,
            onModelSelected = { viewModel.setModel(it) },
            onDismiss = { showModelDialog = false }
        )
    }

    if (showSavedPlacesDialog) {
        SavedPlacesDialog(
            savedPlaces = savedPlaces,
            onPlaceClicked = { place ->
                viewModel.flyToPin(
                    GeoPin(
                        id = place.id.toString(),
                        title = place.name,
                        description = place.notes,
                        lat = place.latitude,
                        lng = place.longitude,
                        category = place.category
                    )
                )
            },
            onDeletePlace = { viewModel.deleteSavedPlace(it) },
            onDismiss = { showSavedPlacesDialog = false }
        )
    }

    if (showClearConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showClearConfirmDialog = false },
            title = { Text("Clear Chat History", color = TextPrimary) },
            text = { Text("Are you sure you want to delete all messages in this conversation thread?", color = TextSecondary) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearChat()
                        showClearConfirmDialog = false
                    }
                ) {
                    Text("Clear All", color = RedAlert)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirmDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = MapDarkBg
        )
    }
}

@Composable
fun ChatThreadSection(
    viewModel: MapChatViewModel,
    listState: androidx.compose.foundation.lazy.LazyListState,
    messages: List<com.example.data.db.ChatMessageEntity>
) {
    if (messages.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF0F2642)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Explore,
                        contentDescription = null,
                        tint = CyanNeon,
                        modifier = Modifier.size(32.dp)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Welcome to MapPulse",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Ask real-time questions about any location, verify local rumors, find hidden gems, or check live opening hours.",
                    fontSize = 13.sp,
                    color = TextSecondary,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    lineHeight = 18.sp
                )
                Spacer(modifier = Modifier.height(14.dp))
                Surface(
                    color = Color(0xFF003847),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(0.5.dp, CyanNeon)
                ) {
                    Text(
                        text = "⚡ Powered by Gemini 3.5 Flash & Google Search Grounding",
                        fontSize = 11.sp,
                        color = CyanNeon,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }
    } else {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(messages) { message ->
                val sources = viewModel.deserializeSources(message.groundingSourcesJson)
                val queries = viewModel.deserializeQueries(message.searchQueriesJson)
                val pins = viewModel.deserializePins(message.geoPinsJson)

                ChatBubbleItem(
                    message = message,
                    groundingSources = sources,
                    searchQueries = queries,
                    geoPins = pins,
                    onPinClicked = { pin -> viewModel.flyToPin(pin) },
                    onSavePin = { pin -> viewModel.savePlace(pin) },
                    onDeleteMessage = { viewModel.deleteMessage(message.id) }
                )
            }
        }
    }
}

@Composable
fun ViewModeButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        color = if (isSelected) CyanNeon else Color.Transparent,
        shape = RoundedCornerShape(6.dp),
        modifier = Modifier
            .clickable { onClick() }
            .padding(1.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected) Color(0xFF070D18) else TextSecondary,
                modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(3.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) Color(0xFF070D18) else TextSecondary
            )
        }
    }
}

@Composable
fun QuickPromptChip(
    emoji: String,
    text: String,
    onClick: () -> Unit
) {
    Surface(
        color = Color(0xFF132238),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(0.5.dp, Color(0xFF1E3A6E)),
        modifier = Modifier.clickable { onClick() }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
        ) {
            Text(text = emoji, fontSize = 12.sp)
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = text,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )
        }
    }
}
