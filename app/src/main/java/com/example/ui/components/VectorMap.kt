package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.sin

data class GeocodeResult(
    val title: String,
    val details: String,
    val offset: Offset
)

enum class MarkerType { RESTAURANT, RIDER, DESTINATION, GENERAL }

data class MapMarker(
    val title: String,
    val position: Offset,
    val type: MarkerType,
    val snippet: String = ""
)

object MapDataHelper {
    val searchResults = listOf(
        GeocodeResult("D-Ground Market", "D-Ground Commercial Block, Faisalabad, Pakistan", Offset(400f, 300f)),
        GeocodeResult("Kohinoor Town", "Kohinoor Town Main Jaranwala Road, Faisalabad, Pakistan", Offset(200f, 150f)),
        GeocodeResult("Faisalabad Tower", "Faisalabad Business Plaza, East Canal Road, Faisalabad", Offset(550f, 250f)),
        GeocodeResult("Samanabad Park", "Samanabad Road Park Sector B, Faisalabad, Pakistan", Offset(150f, 550f)),
        GeocodeResult("Faisalabad Airport", "Main Civil Airport Rd, Faisalabad, Pakistan", Offset(700f, 700f)),
        GeocodeResult("Tech District Bypass", "102 Technology Lane, Faisalabad Tech Hub, Pakistan", Offset(280f, 400f)),
        GeocodeResult("Sargodha Road Chowk", "Sargodha Road Circle Junction, Faisalabad, Pakistan", Offset(480f, 180f)),
        GeocodeResult("Chenab Club Civil Lines", "Chenab Club Road, Civil Lines Near Court, Faisalabad", Offset(320f, 480f))
    )

    fun search(query: String): List<GeocodeResult> {
        if (query.isBlank()) return searchResults
        return searchResults.filter {
            it.title.contains(query, ignoreCase = true) ||
            it.details.contains(query, ignoreCase = true)
        }
    }
}

/**
 * High-performance, highly styled vector map rendered and simulated purely in Jetpack Compose Canvas.
 * Supports Panning, Pinch-to-Zoom gestures, animated active rider progress path, and dynamic markers.
 */
@Composable
fun InteractiveVectorMap(
    modifier: Modifier = Modifier,
    riderProgress: Float = 0f, // 0.0 to 1.0 representing rider transit
    showRiderProgress: Boolean = false,
    markers: List<MapMarker> = emptyList(),
    selectedMarker: MapMarker? = null,
    onMarkerClick: (MapMarker) -> Unit = {},
    centerOffset: Offset = Offset(300f, 300f),
    onCenterOffsetChange: (Offset) -> Unit = {},
    isInteractive: Boolean = true
) {
    var zoomScale by remember { mutableStateOf(1.2f) }
    var panOffset by remember { mutableStateOf(Offset.Zero) }

    // Pulsing animation for active tracker dots
    val infiniteTransition = rememberInfiniteTransition(label = "RadarPulsing")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Alpha"
    )
    val pulseRadius by infiniteTransition.animateFloat(
        initialValue = 10f,
        targetValue = 44f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Radius"
    )

    val textMeasurer = rememberTextMeasurer()

    val mapModifier = if (isInteractive) {
        modifier
            .background(Color(0xFFE8ECEF))
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    zoomScale = (zoomScale * zoom).coerceIn(0.6f, 3f)
                    panOffset += pan
                }
            }
    } else {
        modifier
            .background(Color(0xFFE8ECEF))
    }

    Box(
        modifier = mapModifier
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height

            // Calculate origin based on base offset + panning + center offset
            val originX = (canvasWidth / 2) + panOffset.x + (centerOffset.x * zoomScale)
            val originY = (canvasHeight / 2) + panOffset.y + (centerOffset.y * zoomScale)

            // Draw beautiful natural backgrounds (lake/parks)
            drawPark(this, originX, originY, zoomScale, Offset(-200f, -100f), 220f)
            drawPark(this, originX, originY, zoomScale, Offset(400f, 500f), 180f)
            drawPark(this, originX, originY, zoomScale, Offset(100f, 300f), 120f)
            drawWaterbody(this, originX, originY, zoomScale, Offset(-500f, 600f), Offset(1000f, -100f))

            // Draw local grid streets
            drawStreetGrid(this, originX, originY, zoomScale)

            // Principal Highway 1 "East Canal Rd"
            val highway1 = Path().apply {
                moveTo(-1000f * zoomScale + originX, -500f * zoomScale + originY)
                lineTo(1000f * zoomScale + originX, 800f * zoomScale + originY)
            }
            drawPath(
                path = highway1,
                color = Color(0xFFFFD54F),
                style = Stroke(width = 10f * zoomScale, cap = StrokeCap.Round)
            )
            drawPath(
                path = highway1,
                color = Color.White,
                style = Stroke(width = 2f * zoomScale, cap = StrokeCap.Round, pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 15f), 0f))
            )

            // Principal Highway 2 "Jaranwala Road"
            val highway2 = Path().apply {
                moveTo(-800f * zoomScale + originX, 400f * zoomScale + originY)
                lineTo(900f * zoomScale + originX, -600f * zoomScale + originY)
            }
            drawPath(
                path = highway2,
                color = Color(0xFFFFB74D),
                style = Stroke(width = 12f * zoomScale, cap = StrokeCap.Round)
            )

            // Secondary Ring Road
            drawCircle(
                color = Color(0xFFCFD8DC),
                radius = 350f * zoomScale,
                center = Offset(originX, originY),
                style = Stroke(width = 6f * zoomScale)
            )

            // If active tracking is enabled, draw the simulated delivery route
            // Route from Restaurant (Offset(-200f, -100f)) to Destination (Offset(280f, 400f))
            val startPt = Offset(-200f * zoomScale + originX, -100f * zoomScale + originY)
            val endPt = Offset(280f * zoomScale + originX, 400f * zoomScale + originY)

            if (showRiderProgress) {
                // Route line
                drawLine(
                    color = Color(0xFFFF4B3E),
                    start = startPt,
                    end = endPt,
                    strokeWidth = 5f * zoomScale,
                    cap = StrokeCap.Round,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                )

                // Current rider location interpolation
                val riderX = startPt.x + (endPt.x - startPt.x) * riderProgress
                val riderY = startPt.y + (endPt.y - startPt.y) * riderProgress
                val riderPos = Offset(riderX, riderY)

                // Outer pulsing locator rings
                drawCircle(
                    color = Color(0xFFFF4B3E).copy(alpha = pulseAlpha),
                    radius = pulseRadius * zoomScale,
                    center = riderPos
                )

                // Draw Rider motorbike marker
                drawCircle(
                    color = Color(0xFFFF4B3E),
                    radius = 16f * zoomScale,
                    center = riderPos
                )
                drawCircle(
                    color = Color.White,
                    radius = 13f * zoomScale,
                    center = riderPos
                )
                drawCircle(
                    color = Color(0xFFFF4B3E),
                    radius = 9f * zoomScale,
                    center = riderPos
                )
            }

            // Draw custom map markers
            markers.forEach { marker ->
                val markerX = marker.position.x * zoomScale + originX
                val markerY = marker.position.y * zoomScale + originY
                val markerPos = Offset(markerX, markerY)

                val isSelected = selectedMarker?.title == marker.title

                // Pin Base shadow
                drawCircle(
                    color = Color.Black.copy(alpha = 0.2f),
                    radius = 14f * zoomScale,
                    center = Offset(markerX, markerY + 4f)
                )

                // Outer boundary
                val markerColor = when (marker.type) {
                    MarkerType.RESTAURANT -> Color(0xFFE53935)
                    MarkerType.DESTINATION -> Color(0xFF4CAF50)
                    MarkerType.RIDER -> Color(0xFF2196F3)
                    MarkerType.GENERAL -> Color(0xFF424242)
                }

                drawCircle(
                    color = if (isSelected) Color.White else markerColor,
                    radius = (if (isSelected) 18f else 14f) * zoomScale,
                    center = markerPos
                )

                drawCircle(
                    color = if (isSelected) markerColor else Color.White,
                    radius = (if (isSelected) 14f else 10f) * zoomScale,
                    center = markerPos
                )

                // Small center core
                drawCircle(
                    color = if (isSelected) Color.White else markerColor,
                    radius = 4f * zoomScale,
                    center = markerPos
                )

                // Landmark label text
                val labelText = marker.title
                val textLayoutResult = textMeasurer.measure(
                    text = AnnotatedString(labelText),
                    style = TextStyle(
                        color = Color(0xFF37474F),
                        fontSize = (10f * zoomScale).coerceIn(8f, 14f).sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                )

                val labelX = markerX - (textLayoutResult.size.width / 2)
                val labelY = markerY - (30f * zoomScale) - textLayoutResult.size.height

                // Brief background label box
                drawRoundRect(
                    color = Color.White.copy(alpha = 0.85f),
                    topLeft = Offset(labelX - 8f, labelY - 4f),
                    size = Size(textLayoutResult.size.width + 16f, textLayoutResult.size.height + 8f),
                    cornerRadius = CornerRadius(4f, 4f)
                )

                drawText(
                    textLayoutResult = textLayoutResult,
                    topLeft = Offset(labelX, labelY)
                )
            }

            // Draw major text labels for sectors
            drawSectorLabel(textMeasurer, this, originX + (100f * zoomScale), originY - (250f * zoomScale), "D-Ground Town Sector B")
            drawSectorLabel(textMeasurer, this, originX - (350f * zoomScale), originY + (200f * zoomScale), "Kohinoor Commercial")
            drawSectorLabel(textMeasurer, this, originX + (400f * zoomScale), originY + (350f * zoomScale), "Tech Hub & Residential Block")
        }

        // Action controls (Zoom Buttons overlaid bottom right)
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FloatingActionButton(
                onClick = { zoomScale = (zoomScale + 0.2f).coerceAtMost(3.0f) },
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(40.dp),
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Zoom In", modifier = Modifier.size(20.dp))
            }

            FloatingActionButton(
                onClick = { zoomScale = (zoomScale - 0.2f).coerceAtLeast(0.6f) },
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(40.dp),
                shape = CircleShape
            ) {
                Icon(Icons.Default.Remove, contentDescription = "Zoom Out", modifier = Modifier.size(20.dp))
            }

            FloatingActionButton(
                onClick = {
                    panOffset = Offset.Zero
                    zoomScale = 1.2f
                },
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(40.dp),
                shape = CircleShape
            ) {
                Icon(Icons.Default.MyLocation, contentDescription = "Recenter Map", modifier = Modifier.size(20.dp))
            }
        }
    }
}

private fun drawSectorLabel(measurer: TextMeasurer, scope: DrawScope, x: Float, y: Float, label: String) {
    val res = measurer.measure(
        text = AnnotatedString(label.uppercase()),
        style = TextStyle(
            color = Color(0xFF78909C),
            fontSize = 9.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 2.sp
        )
    )
    scope.drawText(res, topLeft = Offset(x - res.size.width / 2, y))
}

private fun drawPark(scope: DrawScope, originX: Float, originY: Float, scale: Float, loc: Offset, r: Float) {
    scope.drawCircle(
        color = Color(0xFFAED581).copy(alpha = 0.4f),
        radius = r * scale,
        center = Offset(loc.x * scale + originX, loc.y * scale + originY)
    )
}

private fun drawWaterbody(scope: DrawScope, originX: Float, originY: Float, scale: Float, from: Offset, to: Offset) {
    scope.drawLine(
        color = Color(0xFF80DEEA).copy(alpha = 0.5f),
        start = Offset(from.x * scale + originX, from.y * scale + originY),
        end = Offset(to.x * scale + originX, to.y * scale + originY),
        strokeWidth = 36f * scale,
        cap = StrokeCap.Round
    )
}

private fun drawStreetGrid(scope: DrawScope, originX: Float, originY: Float, scale: Float) {
    val step = 100f * scale
    val limit = 10

    // Draw grid grid lines as minor tertiary streets in Faisalabad sector grid layout
    for (i in -limit..limit) {
        val delta = i * step
        // horizontal street lines
        scope.drawLine(
            color = Color(0xFFE2E6E9),
            start = Offset(-limit * step + originX, delta + originY),
            end = Offset(limit * step + originX, delta + originY),
            strokeWidth = 3f * scale
        )
        // vertical street lines
        scope.drawLine(
            color = Color(0xFFE2E6E9),
            start = Offset(delta + originX, -limit * step + originY),
            end = Offset(delta + originX, limit * step + originY),
            strokeWidth = 3f * scale
        )
    }
}


/**
 * A beautiful comprehensive layout where the user can search, explore and set target Delivery Addresses on map.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullScreenMapSearchAndSelectDialog(
    initialLabel: String,
    initialDetail: String,
    onDismiss: () -> Unit,
    onAddressSelected: (label: String, detail: String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var addressLabel by remember { mutableStateOf(initialLabel) }
    var selectedDetails by remember { mutableStateOf(initialDetail.ifEmpty { "123 Foodie Blvd, D-Ground Market, Faisalabad" }) }
    var currentOffset by remember { mutableStateOf(Offset(-200f, -100f)) }

    val locations = remember(searchQuery) {
        MapDataHelper.search(searchQuery)
    }

    var showSuggestions by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Select Address on Map", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Close")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            // Geographic Search & autocomplete Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = {
                        searchQuery = it
                        showSuggestions = true
                    },
                    placeholder = { Text("Search location (D-Ground, Kohinoor...)") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = {
                                searchQuery = ""
                                showSuggestions = false
                            }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            if (showSuggestions && searchQuery.isNotBlank()) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .heightIn(max = 200.dp),
                    shape = RoundedCornerShape(12.dp),
                    tonalElevation = 8.dp,
                    shadowElevation = 8.dp
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp)
                    ) {
                        locations.forEach { item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        currentOffset = item.offset
                                        selectedDetails = item.details
                                        if (addressLabel.isBlank()) {
                                            addressLabel = item.title
                                        }
                                        showSuggestions = false
                                        searchQuery = item.title
                                    }
                                    .padding(vertical = 12.dp, horizontal = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(item.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text(item.details, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                        if (locations.isEmpty()) {
                            Text(
                                "No matched locations found.",
                                modifier = Modifier.padding(12.dp),
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Central Map Area (Occupies most of screen space)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                InteractiveVectorMap(
                    modifier = Modifier.fillMaxSize(),
                    centerOffset = currentOffset,
                    onCenterOffsetChange = { currentOffset = it },
                    markers = listOf(
                        MapMarker("Active Target", currentOffset, MarkerType.DESTINATION, selectedDetails)
                    )
                )

                // Central pinpoint indicator cursor
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .offset(y = (-16).dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(44.dp)
                    )
                    Box(
                        modifier = Modifier
                            .offset(y = 16.dp)
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.3f))
                    )
                }
            }

            // Bottom selection confirming controls
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                elevation = CardDefaults.cardElevation(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Selected Delivery Landmark", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                    
                    OutlinedTextField(
                        value = addressLabel,
                        onValueChange = { addressLabel = it },
                        label = { Text("Address Label (e.g. Home, Office, Gym)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )

                    OutlinedTextField(
                        value = selectedDetails,
                        onValueChange = { selectedDetails = it },
                        label = { Text("Complete Address Details") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3,
                        shape = RoundedCornerShape(8.dp)
                    )

                    Button(
                        onClick = {
                            if (addressLabel.isNotBlank() && selectedDetails.isNotBlank()) {
                                onAddressSelected(addressLabel, selectedDetails)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Confirm Selected Address", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
