package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.ChatMessageEntity
import com.example.data.db.SavedPlaceEntity
import com.example.data.model.AVAILABLE_MODELS
import com.example.data.model.GeminiModelInfo
import com.example.data.model.GeoPin
import com.example.data.model.GroundingSource
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.BotBubbleBg
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.GroundingCardBg
import com.example.ui.theme.MapDarkBg
import com.example.ui.theme.MapSurface
import com.example.ui.theme.MapSurfaceBorder
import com.example.ui.theme.MapSurfaceVariant
import com.example.ui.theme.RedAlert
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.UserBubbleBg
import com.example.util.LocationHelper

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ChatBubbleItem(
    message: ChatMessageEntity,
    groundingSources: List<GroundingSource>,
    searchQueries: List<String>,
    geoPins: List<GeoPin>,
    onPinClicked: (GeoPin) -> Unit,
    onSavePin: (GeoPin) -> Unit,
    onDeleteMessage: () -> Unit
) {
    val isUser = message.role == "user"
    val context = LocalContext.current
    var showSourcesExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        // Sender Badge & Timestamp
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 4.dp, start = 4.dp, end = 4.dp)
        ) {
            if (!isUser) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(CyanNeon, EmeraldGreen))),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.SmartToy,
                        contentDescription = "AI",
                        tint = Color(0xFF070D18),
                        modifier = Modifier.size(13.dp)
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "MapPulse AI",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyanNeon
                )
                Spacer(modifier = Modifier.width(6.dp))
                // Model pill
                Surface(
                    color = MapSurfaceVariant,
                    shape = RoundedCornerShape(4.dp),
                    border = BorderStroke(0.5.dp, MapSurfaceBorder)
                ) {
                    Text(
                        text = if (message.modelId.contains("pro")) "3.1 Pro" else if (message.modelId.contains("lite")) "Lite" else "3.5 Flash",
                        fontSize = 10.sp,
                        color = TextSecondary,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                    )
                }
            } else {
                Text(
                    text = "You",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextSecondary
                )
            }
        }

        // Message Body Card
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (isUser) UserBubbleBg else BotBubbleBg
            ),
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isUser) 16.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 16.dp
            ),
            border = BorderStroke(
                width = 1.dp,
                color = if (isUser) Color(0xFF2B5282) else MapSurfaceBorder
            ),
            modifier = Modifier
                .testTag(if (isUser) "user_message_card" else "model_message_card")
                .fillMaxWidth(if (isUser) 0.88f else 0.98f)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                // Content Text
                Text(
                    text = message.content,
                    color = if (message.isError) RedAlert else TextPrimary,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )

                // Google Search Grounding Box
                if (!isUser && (groundingSources.isNotEmpty() || searchQueries.isNotEmpty())) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = GroundingCardBg),
                        border = BorderStroke(1.dp, Color(0xFF1E3A5F)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showSourcesExpanded = !showSourcesExpanded }
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = "Search",
                                        tint = EmeraldGreen,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Google Search Grounding",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = EmeraldGreen
                                    )
                                }
                                Text(
                                    text = if (showSourcesExpanded) "Hide ▲" else "${groundingSources.size} Sources ▼",
                                    fontSize = 11.sp,
                                    color = CyanNeon
                                )
                            }

                            if (searchQueries.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Searched: ${searchQueries.joinToString(" • ")}",
                                    fontSize = 11.sp,
                                    color = TextMuted,
                                    maxLines = if (showSourcesExpanded) 4 else 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            AnimatedVisibility(visible = showSourcesExpanded) {
                                Column(modifier = Modifier.padding(top = 8.dp)) {
                                    FlowRow(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        groundingSources.forEach { source ->
                                            Surface(
                                                color = MapSurfaceVariant,
                                                shape = RoundedCornerShape(12.dp),
                                                border = BorderStroke(0.5.dp, Color(0xFF2B4C7E)),
                                                modifier = Modifier.clickable {
                                                    LocationHelper.openWebUrl(context, source.url)
                                                }
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Language,
                                                        contentDescription = null,
                                                        tint = CyanNeon,
                                                        modifier = Modifier.size(12.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(
                                                        text = source.title,
                                                        fontSize = 11.sp,
                                                        color = TextPrimary,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                    Spacer(modifier = Modifier.width(2.dp))
                                                    Icon(
                                                        imageVector = Icons.Default.OpenInNew,
                                                        contentDescription = null,
                                                        tint = TextSecondary,
                                                        modifier = Modifier.size(10.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Extracted Geo Pins Carousel
                if (!isUser && geoPins.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "📍 Recommended Map Spots (${geoPins.size}):",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyanNeon
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(vertical = 4.dp)
                    ) {
                        items(geoPins) { pin ->
                            GeoPinCard(
                                pin = pin,
                                onFlyTo = { onPinClicked(pin) },
                                onSave = { onSavePin(pin) },
                                onOpenExternal = {
                                    LocationHelper.openInGoogleMaps(context, pin.lat, pin.lng, pin.title)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GeoPinCard(
    pin: GeoPin,
    onFlyTo: () -> Unit,
    onSave: () -> Unit,
    onOpenExternal: () -> Unit
) {
    var saved by remember { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(containerColor = MapSurface),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, Color(0xFF1E3A6E)),
        modifier = Modifier
            .width(220.dp)
            .clickable { onFlyTo() }
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    color = Color(0xFF003847),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = pin.category.uppercase(),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyanNeon,
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                    )
                }

                IconButton(
                    onClick = {
                        saved = !saved
                        onSave()
                    },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = if (saved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = "Save place",
                        tint = if (saved) EmeraldGreen else TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = pin.title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (pin.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = pin.description,
                    fontSize = 11.sp,
                    color = TextSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 15.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = onFlyTo,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF005580)),
                    modifier = Modifier
                        .weight(1f)
                        .height(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Map,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Pin", fontSize = 11.sp)
                }

                OutlinedButton(
                    onClick = onOpenExternal,
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.dp, MapSurfaceBorder),
                    modifier = Modifier
                        .weight(1f)
                        .height(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Directions,
                        contentDescription = null,
                        tint = CyanNeon,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Maps", fontSize = 11.sp, color = TextPrimary)
                }
            }
        }
    }
}

@Composable
fun ModelSelectionDialog(
    currentModel: GeminiModelInfo,
    onModelSelected: (GeminiModelInfo) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.SmartToy,
                    contentDescription = null,
                    tint = CyanNeon,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Select Gemini AI Engine", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Choose model based on your task speed and reasoning requirements:",
                    color = TextSecondary,
                    fontSize = 13.sp
                )

                AVAILABLE_MODELS.forEach { model ->
                    val isSelected = model.id == currentModel.id
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) Color(0xFF132A48) else MapSurface
                        ),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(
                            width = if (isSelected) 1.5.dp else 1.dp,
                            color = if (isSelected) CyanNeon else MapSurfaceBorder
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onModelSelected(model)
                                onDismiss()
                            }
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = model.displayName,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = if (isSelected) CyanNeon else TextPrimary
                                    )
                                }
                                Surface(
                                    color = if (model.supportsSearchGrounding) Color(0xFF003D20) else Color(0xFF1E293B),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = model.badge,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (model.supportsSearchGrounding) EmeraldGreen else TextSecondary,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = model.description,
                                fontSize = 12.sp,
                                color = TextSecondary,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = CyanNeon)
            }
        },
        containerColor = MapDarkBg,
        textContentColor = TextPrimary
    )
}

@Composable
fun SavedPlacesDialog(
    savedPlaces: List<SavedPlaceEntity>,
    onPlaceClicked: (SavedPlaceEntity) -> Unit,
    onDeletePlace: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Bookmark,
                    contentDescription = null,
                    tint = EmeraldGreen,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Saved Locations (${savedPlaces.size})", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            if (savedPlaces.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No saved locations yet.",
                            color = TextSecondary,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Tap the bookmark icon on any recommended spot in chat to save it.",
                            color = TextMuted,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.height(300.dp)
                ) {
                    items(savedPlaces) { place ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MapSurface),
                            border = BorderStroke(1.dp, MapSurfaceBorder),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable {
                                            onPlaceClicked(place)
                                            onDismiss()
                                        }
                                ) {
                                    Text(
                                        text = place.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "${place.category} • ${place.address}",
                                        fontSize = 11.sp,
                                        color = TextSecondary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = {
                                            LocationHelper.openInGoogleMaps(context, place.latitude, place.longitude, place.name)
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Directions,
                                            contentDescription = "Maps",
                                            tint = CyanNeon,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }

                                    IconButton(
                                        onClick = { onDeletePlace(place.id) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete",
                                            tint = RedAlert,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = CyanNeon)
            }
        },
        containerColor = MapDarkBg
    )
}
