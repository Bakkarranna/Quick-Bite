package com.example.ui.screens

import android.content.Context
import android.graphics.Bitmap
import android.widget.Toast
import com.example.ui.components.ToastHelper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.data.DeliveryAddress
import com.example.data.OrderHistory
import com.example.service.FirebaseConfig
import com.example.service.FirebaseManager
import com.example.viewmodel.QuickBiteViewModel
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import androidx.compose.ui.geometry.Offset
import kotlin.random.Random


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: QuickBiteViewModel,
    onNavigateToRestaurant: (String) -> Unit,
    onNavigateToCart: () -> Unit
) {
    val context = LocalContext.current
    val cartCount by viewModel.cartCount.collectAsState()
    val restaurants by viewModel.restaurantsList.collectAsState()
    val profile by viewModel.userProfile.collectAsState()
    
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Biryani") }
    var activeSortFilter by remember { mutableStateOf("All") } // "All", "Rating", "Fastest", "Near"
    
    var showAiDialog by remember { mutableStateOf(false) }

    val categories = listOf("Biryani", "Pizza", "Burgers", "BBQ", "Desi", "Desserts", "Chinese", "Drinks")

    // Filtered and Sorted Restaurant Listing
    val filteredRestaurants = remember(restaurants, searchQuery, selectedCategory, activeSortFilter) {
        restaurants.filter { rest ->
            val matchesCategory = rest.categories.contains(selectedCategory)
            val matchesQuery = if (searchQuery.isBlank()) true else {
                rest.title.contains(searchQuery, ignoreCase = true) ||
                rest.tags.contains(searchQuery, ignoreCase = true)
            }
            matchesCategory && matchesQuery
        }.run {
            when (activeSortFilter) {
                "Rating" -> sortedByDescending { it.rating.toDoubleOrNull() ?: 0.0 }
                "Fastest" -> sortedBy { 
                    it.time.split("-").firstOrNull()?.trim()?.filter { it.isDigit() }?.toIntOrNull() ?: 99
                }
                "Near" -> sortedBy {
                    it.distance.replace(" km", "").trim().toDoubleOrNull() ?: 99.0
                }
                else -> this
            }
        }
    }

    // AI Assistant Dialog states
    val aiRecommendation by viewModel.aiRecommendation.collectAsState()
    val isAiLoading by viewModel.isAiLoading.collectAsState()
    var aiQuery by remember { mutableStateOf("") }

    if (showAiDialog) {
        AlertDialog(
            onDismissRequest = { 
                viewModel.clearAiRecommendation()
                showAiDialog = false 
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = Color(0xFFFF416C),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "QuickBite AI Assistant",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Let Google Gemini help you choose your next favorite meal from the best spots in Faisalabad!",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = aiQuery,
                        onValueChange = { aiQuery = it },
                        placeholder = { Text("e.g., I want something spicy & traditional with rice...") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        maxLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFFF416C)
                        )
                    )

                    // Quick suggestion chips
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            "🔥 Spicy Desi", "🍕 Cheesy Fast Food", "🍨 Sweet Dessert", "🥑 Healthy Light"
                        ).forEach { tag ->
                            SuggestionChip(
                                onClick = {
                                    aiQuery = when (tag) {
                                        "🔥 Spicy Desi" -> "I want something authentic, spicy, with rice or beef kebabs"
                                        "🍕 Cheesy Fast Food" -> "Looking for extra cheesy pizza or a double cheeseburger"
                                        "🍨 Sweet Dessert" -> "Satisfy my sweet tooth with traditional pudding or sweet chocolate cakes"
                                        "🥑 Healthy Light" -> "Suggest a healthy, light item with rich proteins"
                                        else -> tag
                                    }
                                },
                                label = { Text(tag, fontSize = 11.sp) }
                            )
                        }
                    }

                    if (isAiLoading) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(color = Color(0xFFFF416C))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "AI is consulting local recipes... 🍳",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFF416C)
                            )
                        }
                    } else if (aiRecommendation != null) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Color(0xFFFF416C).copy(alpha = 0.3f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = aiRecommendation!!,
                                    fontSize = 13.sp,
                                    lineHeight = 18.sp,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                
                                Spacer(modifier = Modifier.height(12.dp))
                                
                                Button(
                                    onClick = {
                                        val recLower = aiRecommendation!!.lowercase()
                                        val appliedSearch = when {
                                            recLower.contains("biryani") -> "Biryani"
                                            recLower.contains("pizza") -> "Pizza"
                                            recLower.contains("burger") -> "Burger"
                                            recLower.contains("kebab") -> "Kebab"
                                            recLower.contains("tikka") -> "Tikka"
                                            else -> "Biryani"
                                        }
                                        searchQuery = appliedSearch
                                        ToastHelper.showToast("Searching for: $appliedSearch")
                                        viewModel.clearAiRecommendation()
                                        showAiDialog = false
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF416C)),
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Apply Search Filter", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (aiQuery.isNotBlank()) {
                            viewModel.getAiRecommendation(aiQuery)
                        } else {
                            ToastHelper.showToast("Please enter your craving details.")
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFFF416C))
                ) {
                    Text("Ask Gemini ✨", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    viewModel.clearAiRecommendation()
                    showAiDialog = false 
                }) {
                    Text("Close")
                }
            }
        )
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        TopAppBar(
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text("Delivering to", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Faisalabad, Punjab", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            },
            actions = {
                IconButton(onClick = { ToastHelper.showToast("No unread notifications.") }) {
                    Box {
                        Icon(Icons.Outlined.Notifications, contentDescription = "Notifications", tint = MaterialTheme.colorScheme.onSurface)
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                                .align(Alignment.TopEnd)
                        )
                    }
                }
                IconButton(onClick = onNavigateToCart) {
                    Box {
                        Icon(Icons.Outlined.ShoppingCart, contentDescription = "My Cart", tint = MaterialTheme.colorScheme.onSurface)
                        if (cartCount > 0) {
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary)
                                    .align(Alignment.TopEnd),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = cartCount.toString(),
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Welcome Greeting with AI Trigger Card inline
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Hi, ${profile?.name?.split(" ")?.firstOrNull() ?: "Ahmad"} 👋",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "What are you craving today?",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // AI Assistant Magic trigger banner
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFFFF416C),
                                    Color(0xFFFF4B2B)
                                )
                            )
                        )
                        .clickable { showAiDialog = true }
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "AI Assistant",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Not sure what to eat? ⚡",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Ask chef Gemini for smart AI suggestions!",
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 11.sp
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Open AI",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Search input field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search restaurants or dishes...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                ),
                singleLine = true,
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                }
            )

            // Dynamic sorting chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    "All" to "🍽️ All",
                    "Rating" to "⭐ Top Rated (4.7+)",
                    "Fastest" to "⚡ Express (<35m)",
                    "Near" to "📍 Nearby (<2km)"
                ).forEach { (filterVal, label) ->
                    val isSelected = activeSortFilter == filterVal
                    FilterChip(
                        selected = isSelected,
                        onClick = { activeSortFilter = filterVal },
                        label = { Text(label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                        shape = RoundedCornerShape(20.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
            }

            // Promo Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1.3f)
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "50% OFF",
                            color = Color.White,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "your first order!",
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { 
                                viewModel.activePromoCode.value = "CLAIM50"
                                ToastHelper.showToast("Promo code CLAIM50 saved! Go to cart to checkout.") 
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = MaterialTheme.colorScheme.primary),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.height(36.dp)
                        ) {
                            Text("Claim Now", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    ) {
                        Image(
                            painter = rememberAsyncImagePainter("https://lh3.googleusercontent.com/aida-public/AB6AXuDBrL6c9xuYqNackMqOuSTfMweK1PbSBU-VKsAx7o7y9D6YFS5pdLl_GQadf_e-Fl942Q4QlCTF4kozIMBCJymaUVWt0VVCB-P3QfMxPppzvZs0D1fxaDBe6JWn3-ANmTeIHOb_BoD92S_JI7BLiQkNgfIKRXeNjbRsIGtoImUvzAFlnZG4QJQPTKyBrmrfKltQ1HyFhjXK9VGOdNKyajJvSkgmbEWQhzqH8wXsODN1Sx3A35X5XmLtjml0wV6X0TEVr6FJkaMVlec"),
                            contentDescription = "Double cheeseburger",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(12.dp))
                        )
                    }
                }
            }

            // Categories list (Horizontal Slider)
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(categories) { category ->
                    val isSelected = category == selectedCategory
                    Card(
                        onClick = { selectedCategory = category },
                        modifier = Modifier
                            .width(80.dp)
                            .height(100.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
                        ),
                        border = if (!isSelected) BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)) else null
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelected) Color.White else MaterialTheme.colorScheme.surfaceVariant)
                                    .padding(6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                val iconUrl = when (category) {
                                    "Biryani" -> "https://lh3.googleusercontent.com/aida-public/AB6AXuB_2VT0zVTe32z1dQQ0QkIJGi8MmZvzyJpmS6IsoHC1tBiP9s0p7twKIu0r4ucOG3nIgK2y5Zw29XlzkXLc1Avm2rPVgLPe5kC_ySvB0cHvcg_y178cvuTFevzg-Xzfq3K6ZnP5iYDDSq79Vh4gXDRhajAprV-BCqt-Rdo30iGzdlDyxlc6fD9TFmZMUSZ24bvq-MjZIT-OOYJj0TxLe7Y9yA4dXqOdF_9TFveciv3sP4mBII71hU1ttH5jd52BJSDm0eIpfXVlXWU"
                                    "Pizza" -> "https://lh3.googleusercontent.com/aida-public/AB6AXuA65KNguaZK-LgcyGoTJYDtV9zjN2VtLIdi6673a9CtmpTJRn1kyuZIhS2OzJGvFf66fh_BYghVg5fHrMh3b5pE0EYfcAf031xAbpA8iwoKlgI2DKTfrVKhSVqi5HbFqO18TGOpy5NsBFGHgQMmDNu6FhBpcyfp7Cz1oi0_Ew0tve1iy5OPYpkmgSWwj8PFF8M3MGD3XqiqDMNMZf0cAGEea4rbkSbVWNB9L0bIyIh63x140kHGLJyik9NevJBsT-srbMr7yojU2ds"
                                    "Burgers" -> "https://lh3.googleusercontent.com/aida-public/AB6AXuAQTFiTr9m8oTY307_lNxjgdk38JLMzVERqpPi-GGw02fkuSyc4wOIP1c79Pl9q1pg7N567utDKDGgfyISNav8appCt4fK53ccZ6jpzw4ZK3SJ5nvNzwWeZhuvXW9EKCywkjRz-m_xIsj0wgAqHg5cMxe_QYQq36jBksiqZp31f5T2LeCemIawAMyzEFDhe0AKiZwMYj4UcFq99k92SKSEERgd7COdhUAzfR3Euuo9rKS51P0pmL0KE7js5rY5SZwd08y9I-AyD7-0"
                                    else -> "https://lh3.googleusercontent.com/aida-public/AB6AXuB_2VT0zVTe32z1dQQ0QkIJGi8MmZvzyJpmS6IsoHC1tBiP9s0p7twKIu0r4ucOG3nIgK2y5Zw29XlzkXLc1Avm2rPVgLPe5kC_ySvB0cHvcg_y178cvuTFevzg-Xzfq3K6ZnP5iYDDSq79Vh4gXDRhajAprV-BCqt-Rdo30iGzdlDyxlc6fD9TFmZMUSZ24bvq-MjZIT-OOYJj0TxLe7Y9yA4dXqOdF_9TFveciv3sP4mBII71hU1ttH5jd52BJSDm0eIpfXVlXWU"
                                }
                                Image(
                                    painter = rememberAsyncImagePainter(iconUrl),
                                    contentDescription = category,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = category,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            // Popular Near You List
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = "Popular Near You (${filteredRestaurants.size})",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "See All",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { ToastHelper.showToast("Showing ${filteredRestaurants.size} results.") }
                )
            }

            // Horizontal Scroll Cards
            if (filteredRestaurants.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.HourglassEmpty, contentDescription = null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No restaurants found under '$selectedCategory'", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Text("Try clearing the search or sort filters", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                    }
                }
            } else {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(filteredRestaurants) { rest ->
                        RestaurantCard(
                            title = rest.title,
                            tags = rest.tags,
                            rating = rest.rating,
                            price = rest.price,
                            distance = rest.distance,
                            time = rest.time,
                            imageUrl = rest.imageUrl,
                            onClick = { onNavigateToRestaurant(rest.id) }
                        )
                    }
                }
            }

            // SECTION 2: Trending Food Menu & Deals 🍖 (Scrollable Food Listing with direct Cart actions)
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Trending Food & Deals 🍖",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Popular hot items. Click quickly to add details straight to cart!",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Horizontal row of individual hot trending food items
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                val trendingMeals = listOf(
                    Triple("Classic Beef Cheese Burger", 380.0, "https://lh3.googleusercontent.com/aida-public/AB6AXuB9KVdx714jtz-S5J2wCNac9F573m8IkmeUaQdxmksy4X7FL3Nv_XXveydQFNk3_K_KZ_sLDxc8WQdTGO4ltqh447grblcghXTJy3YoyzTRMA1RKGe_sobrHd4vtQ6zQMsyCVaxjpt6D3ldFS62ea4Xe8R3TexLE7UEG5q8k1wihM6ooWu8XJnZZ_eyQWeOyIL3RD8lp1Kncebr99vzk_0cfmkd3OsgIWzQWKnf3VLj8Q-566-QQ_AtQs9uSUsB09PtwS1Jgdsf3Ng"),
                    Triple("Royal Chicken Mughlai Biryani", 320.0, "https://lh3.googleusercontent.com/aida-public/AB6AXuCDR2sEJYHUbyL7JGFr4hrczv9zJKvyDHR88AmKAuPGDO4yBi_V3kH38PylG5OmPvAeFlvqVfD4D_VlpU_eDODrtFNETnx6jIaLuVTob1tTUMFNl10-g1LPd1KtH7hTiGE2L8GNo46ZMz6A9YvU04NH3eio5o1tdYxh8i6F1y_vWd5jCWmAzIud9JIux5XPEefhCyG3Eusq1-mrNKsgmHw8ON49fcmQQ9ZvmsKZKCnTN6tqNhHb0KvPKewnvFZzkcOT4z4fbqFWFnM"),
                    Triple("Overloaded Double Cheese Pizza", 590.0, "https://lh3.googleusercontent.com/aida-public/AB6AXuA65KNguaZK-LgcyGoTJYDtV9zjN2VtLIdi6673a9CtmpTJRn1kyuZIhS2OzJGvFf66fh_BYghVg5fHrMh3b5pE0EYfcAf031xAbpA8iwoKlgI2DKTfrVKhSVqi5HbFqO18TGOpy5NsBFGHgQMmDNu6FhBpcyfp7Cz1oi0_Ew0tve1iy5OPYpkmgSWwj8PFF8M3MGD3XqiqDMNMZf0cAGEea4rbkSbVWNB9L0bIyIh63x140kHGLJyik9NevJBsT-srbMr7yojU2ds"),
                    Triple("Sizzling Tandoori Kebab Platter", 490.0, "https://lh3.googleusercontent.com/aida-public/AB6AXuBlQEY9aRHy4z7ZODjXo_8x684HC4_9ju4c3fqsBzOKDQ99G6s0cskqUf43pFflaLkZokLJ_TUn2rafpgZ9xVHoLpp8pIaGwJjt1L6pAaGj0wYHNvpHQywg3rPFSaQ35HWC40t1pAAc8do7s2tOkSu6L1pfCbRT-ezNp11av_VusNW6QwuxmyZCILVaJzrEa1EVF0AyYzBMh-L2IpoNWY1TFSEXUYRWVOnQFljooKKaZMLegjGMR1zlzQSexebU_hd4EnC060A_aDo"),
                    Triple("Thick Gourmet Chocolate Cake", 250.0, "https://lh3.googleusercontent.com/aida-public/AB6AXuDBrL6c9xuYqNackMqOuSTfMweK1PbSBU-VKsAx7o7y9D6YFS5pdLl_GQadf_e-Fl942Q4QlCTF4kozIMBCJymaUVWt0VVCB-P3QfMxPppzvZs0D1fxaDBe6JWn3-ANmTeIHOb_BoD92S_JI7BLiQkNgfIKRXeNjbRsIGtoImUvzAFlnZG4QJQPTKyBrmrfKltQ1HyFhjXK9VGOdNKyajJvSkgmbEWQhzqH8wXsODN1Sx3A35X5XmLtjml0wV6X0TEVr6FJkaMVlec")
                )
                
                items(trendingMeals) { (name, price, img) ->
                    Card(
                        modifier = Modifier
                            .width(180.dp)
                            .height(230.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                    ) {
                        Column(modifier = Modifier.fillMaxSize()) {
                            Image(
                                painter = rememberAsyncImagePainter(img),
                                contentDescription = name,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(110.dp)
                            )
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = name,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.height(34.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Rs. ${price.toInt()}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = {
                                        viewModel.addItemToCart(
                                            id = "trend-${name.hashCode()}",
                                            name = name,
                                            price = price,
                                            imageUrl = img
                                        )
                                        ToastHelper.showToast("$name added to Cart!")
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(30.dp),
                                    contentPadding = PaddingValues(0.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(12.dp), tint = Color.White)
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text("Add to Cart", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }
                    }
                }
            }

            // SECTION 3: Live Map Data & Location Search 🗺️ (Interactive map, dynamic geocoder, direct live construction of listings)
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Live Map & Local Search 🗺️",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Search locations in Faisalabad to discover or spawn new restaurant listings!",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Google Search Address Bar
            var mapSearchQuery by remember { mutableStateOf("") }
            var geocodeResults by remember { mutableStateOf<List<com.example.ui.components.GeocodeResult>>(emptyList()) }
            var selectedGeocode by remember { mutableStateOf<com.example.ui.components.GeocodeResult?>(null) }
            
            OutlinedTextField(
                value = mapSearchQuery,
                onValueChange = { 
                    mapSearchQuery = it
                    geocodeResults = com.example.ui.components.MapDataHelper.search(it)
                },
                placeholder = { Text("Search places in Faisalabad (D-Ground, Kohinoor...)...") },
                leadingIcon = { Icon(Icons.Default.Map, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                ),
                singleLine = true,
                trailingIcon = {
                    if (mapSearchQuery.isNotEmpty()) {
                        IconButton(onClick = { 
                            mapSearchQuery = ""
                            geocodeResults = emptyList()
                        }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear search")
                        }
                    }
                }
            )

            // Geocode results dropdown
            if (geocodeResults.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(
                            text = "Matching Places found via Live Maps API:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                        geocodeResults.take(4).forEach { res ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedGeocode = res
                                        mapSearchQuery = res.title
                                        geocodeResults = emptyList()
                                        ToastHelper.showToast("Location selected: ${res.title}")
                                    }
                                    .padding(vertical = 8.dp, horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(res.title, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    Text(res.details, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }

            // Interactive Vector Map Rendering representing Faisalabad
            val baseMarkers = remember {
                listOf(
                    com.example.ui.components.MapMarker("Naseeb Biryani Center", Offset(400f, 300f), com.example.ui.components.MarkerType.RESTAURANT),
                    com.example.ui.components.MapMarker("Spice Grill", Offset(250f, 450f), com.example.ui.components.MarkerType.RESTAURANT),
                    com.example.ui.components.MapMarker("Burger & Co.", Offset(310f, 210f), com.example.ui.components.MarkerType.RESTAURANT)
                )
            }
            
            // Map markers merged with currently selected search result
            val mapMarkers = remember(selectedGeocode) {
                if (selectedGeocode != null) {
                    baseMarkers + com.example.ui.components.MapMarker(
                        selectedGeocode!!.title,
                        selectedGeocode!!.offset,
                        com.example.ui.components.MarkerType.DESTINATION,
                        "Selected dynamic location tag"
                    )
                } else {
                    baseMarkers
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .border(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            ) {
                var isMapUnlocked by remember { mutableStateOf(false) }

                com.example.ui.components.InteractiveVectorMap(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable { if (!isMapUnlocked) { isMapUnlocked = true } },
                    markers = mapMarkers,
                    selectedMarker = selectedGeocode?.let { 
                        com.example.ui.components.MapMarker(it.title, it.offset, com.example.ui.components.MarkerType.DESTINATION)
                    },
                    centerOffset = selectedGeocode?.offset ?: Offset(300f, 300f),
                    isInteractive = isMapUnlocked
                )

                // Lock state toggle overlay
                Row(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(10.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Black.copy(alpha = 0.7f))
                        .clickable { isMapUnlocked = !isMapUnlocked }
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isMapUnlocked) Icons.Default.LockOpen else Icons.Default.Lock,
                        contentDescription = "Map Lock",
                        tint = if (isMapUnlocked) Color.Green else Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isMapUnlocked) "Unlocked" else "Locked (Tap to pan/zoom)",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Hint overlay
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(10.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Black.copy(alpha = 0.6f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("Interactive Live Map Viewer", color = Color.White, fontSize = 10.sp)
                }
            }

            // Add Food Listing Form
            if (selectedGeocode != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                ) {
                    var newRestName by remember { mutableStateOf("") }
                    var selectedRestCategory by remember { mutableStateOf("Biryani") }
                    var newRestTags by remember { mutableStateOf("Traditional Spiced Biryani • Desi") }

                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AddHome, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Add New Restaurant at selected location:",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Text(
                            text = "Location: ${selectedGeocode!!.details}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        OutlinedTextField(
                            value = newRestName,
                            onValueChange = { newRestName = it },
                            label = { Text("Restaurant Name") },
                            placeholder = { Text("e.g., Al-Madina Pulao, Pizza Point") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true
                        )

                        // Selector for category
                        Column {
                            Text("Category:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                categories.forEach { cat ->
                                    val isCatSelected = cat == selectedRestCategory
                                    FilterChip(
                                        selected = isCatSelected,
                                        onClick = { 
                                            selectedRestCategory = cat
                                            // auto set pre-filled tags
                                            newRestTags = when (cat) {
                                                "Biryani" -> "Pakistani • Traditional Spiced Biryani • Desi"
                                                "Pizza" -> "Pizza • Cheesy Crust • Italian Style"
                                                "Burgers" -> "Burgers • Smash Burgers • Crisp Fries • American"
                                                "BBQ" -> "Pakistani BBQ • Charcoal Grilled • Skewers"
                                                "Desi" -> "Pakistani • Karahi • Traditional Handi"
                                                "Desserts" -> "Sweets • Gourmet Cakes • Bakery"
                                                "Chinese" -> "Chinese Noodles • Chow Mein • Fried Rice"
                                                "Drinks" -> "Fruit Drinks • Shakes • Refreshing Mocktails"
                                                else -> "Gourmet Flavors • QuickBite Selected"
                                            }
                                        },
                                        label = { Text(cat, fontSize = 11.sp) }
                                    )
                                }
                            }
                        }

                        Button(
                            onClick = {
                                if (newRestName.trim().isEmpty()) {
                                    ToastHelper.showToast("Please specify a valid restaurant name!")
                                    return@Button
                                }
                                val finalTags = if (newRestTags.isBlank()) {
                                    "$selectedRestCategory • QuickBite Approved • Faisalabad"
                                } else {
                                    newRestTags
                                }
                                
                                val customId = "new-rest-${Random.nextInt(1000, 9999)}"
                                val distanceDouble = Random.nextDouble(0.5, 6.0)
                                val finalDistance = String.format("%.1f km", distanceDouble)

                                val brandNewRestaurant = com.example.data.RestaurantData(
                                    id = customId,
                                    title = newRestName.trim(),
                                    tags = finalTags,
                                    rating = String.format("%.1f", Random.nextDouble(4.3, 5.0)),
                                    price = "Min. Rs. 250",
                                    distance = finalDistance,
                                    time = "20-35 min",
                                    imageUrl = when (selectedRestCategory) {
                                        "Pizza" -> "https://lh3.googleusercontent.com/aida-public/AB6AXuA65KNguaZK-LgcyGoTJYDtV9zjN2VtLIdi6673a9CtmpTJRn1kyuZIhS2OzJGvFf66fh_BYghVg5fHrMh3b5pE0EYfcAf031xAbpA8iwoKlgI2DKTfrVKhSVqi5HbFqO18TGOpy5NsBFGHgQMmDNu6FhBpcyfp7Cz1oi0_Ew0tve1iy5OPYpkmgSWwj8PFF8M3MGD3XqiqDMNMZf0cAGEea4rbkSbVWNB9L0bIyIh63x140kHGLJyik9NevJBsT-srbMr7yojU2ds"
                                        "Burgers" -> "https://lh3.googleusercontent.com/aida-public/AB6AXuB9KVdx714jtz-S5J2wCNac9F573m8IkmeUaQdxmksy4X7FL3Nv_XXveydQFNk3_K_KZ_sLDxc8WQdTGO4ltqh447grblcghXTJy3YoyzTRMA1RKGe_sobrHd4vtQ6zQMsyCVaxjpt6D3ldFS62ea4Xe8R3TexLE7UEG5q8k1wihM6ooWu8XJnZZ_eyQWeOyIL3RD8lp1Kncebr99vzk_0cfmkd3OsgIWzQWKnf3VLj8Q-566-QQ_AtQs9uSUsB09PtwS1Jgdsf3Ng"
                                        "Desserts", "Drinks" -> "https://lh3.googleusercontent.com/aida-public/AB6AXuDBrL6c9xuYqNackMqOuSTfMweK1PbSBU-VKsAx7o7y9D6YFS5pdLl_GQadf_e-Fl942Q4QlCTF4kozIMBCJymaUVWt0VVCB-P3QfMxPppzvZs0D1fxaDBe6JWn3-ANmTeIHOb_BoD92S_JI7BLiQkNgfIKRXeNjbRsIGtoImUvzAFlnZG4QJQPTKyBrmrfKltQ1HyFhjXK9VGOdNKyajJvSkgmbEWQhzqH8wXsODN1Sx3A35X5XmLtjml0wV6X0TEVr6FJkaMVlec"
                                        else -> "https://lh3.googleusercontent.com/aida-public/AB6AXuCDR2sEJYHUbyL7JGFr4hrczv9zJKvyDHR88AmKAuPGDO4yBi_V3kH38PylG5OmPvAeFlvqVfD4D_VlpU_eDODrtFNETnx6jIaLuVTob1tTUMFNl10-g1LPd1KtH7hTiGE2L8GNo46ZMz6A9YvU04NH3eio5o1tdYxh8i6F1y_vWd5jCWmAzIud9JIux5XPEefhCyG3Eusq1-mrNKsgmHw8ON49fcmQQ9ZvmsKZKCnTN6tqNhHb0KvPKewnvFZzkcOT4z4fbqFWFnM"
                                    },
                                    categories = listOf(selectedRestCategory)
                                )

                                viewModel.addRestaurant(brandNewRestaurant)
                                ToastHelper.showToast("Constructed & Added ${brandNewRestaurant.title} listing live! 🚀")
                                
                                // Reset form inputs
                                newRestName = ""
                                selectedGeocode = null
                                mapSearchQuery = ""
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Confirm & Construct Listing", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.TouchApp, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Type in search box above and select a place to place a brand-new restaurant pin!",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(60.dp))
        }
    }
}

@Composable
fun RestaurantCard(
    title: String,
    tags: String,
    rating: String,
    price: String,
    distance: String,
    time: String,
    imageUrl: String,
    onClick: () -> Unit
) {
    var isFavorite by remember { mutableStateOf(false) }

    Card(
        onClick = onClick,
        modifier = Modifier
            .width(280.dp)
            .height(290.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
            ) {
                Image(
                    painter = rememberAsyncImagePainter(imageUrl),
                    contentDescription = title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Favorite button
                IconButton(
                    onClick = { isFavorite = !isFavorite },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (isFavorite) Color.Red else Color.Gray,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // ETA Badge
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(12.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(text = time, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                }
            }

            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1.0f)
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFA500), modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = rating, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(tags, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Moped, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                    Text(text = price, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Box(modifier = Modifier.size(4.dp).clip(CircleShape).background(MaterialTheme.colorScheme.onSurfaceVariant))
                    Text(text = distance, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyOrdersScreen(
    viewModel: QuickBiteViewModel,
    onTrackOrder: (String) -> Unit,
    onRateOrder: (String) -> Unit
) {
    val orders by viewModel.orders.collectAsState()
    var selectedTab by remember { mutableStateOf("All") } // "All", "Active", "Completed", "Cancelled"

    val filteredOrders = when (selectedTab) {
        "Active" -> orders.filter { it.status == "Placed" || it.status == "Preparing" || it.status == "On the way" }
        "Completed" -> orders.filter { it.status == "Delivered" }
        "Cancelled" -> orders.filter { it.status == "Cancelled" }
        else -> orders
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Orders", fontWeight = FontWeight.Bold) }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            // Tabs Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                listOf("All", "Active", "Completed", "Cancelled").forEach { tab ->
                    val isSelected = tab == selectedTab
                    Column(
                        modifier = Modifier
                            .clickable { selectedTab = tab }
                            .padding(vertical = 4.dp, horizontal = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = tab,
                            fontSize = 14.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .width(20.dp)
                                .height(2.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (filteredOrders.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.ReceiptLong,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No orders found.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredOrders) { order ->
                        OrderCardItem(order, onTrackOrder, onRateOrder)
                    }
                }
            }
        }
    }
}

@Composable
fun OrderCardItem(
    order: OrderHistory,
    onTrackOrder: (String) -> Unit,
    onRateOrder: (String) -> Unit
) {
    val statusColor = when (order.status) {
        "Placed", "Preparing", "On the way" -> Color(0xFFFFA500) // orange
        "Delivered" -> Color(0xFF4CAF50) // green
        else -> Color.Red
    }

    val imgUrl = when (order.restaurantName) {
        "Luigi's Woodfire Pizza" -> "https://lh3.googleusercontent.com/aida-public/AB6AXuA65KNguaZK-LgcyGoTJYDtV9zjN2VtLIdi6673a9CtmpTJRn1kyuZIhS2OzJGvFf66fh_BYghVg5fHrMh3b5pE0EYfcAf031xAbpA8iwoKlgI2DKTfrVKhSVqi5HbFqO18TGOpy5NsBFGHgQMmDNu6FhBpcyfp7Cz1oi0_Ew0tve1iy5OPYpkmgSWwj8PFF8M3MGD3XqiqDMNMZf0cAGEea4rbkSbVWNB9L0bIyIh63x140kHGLJyik9NevJBsT-srbMr7yojU2ds"
        "The Burger Factory" -> "https://lh3.googleusercontent.com/aida-public/AB6AXuAQTFiTr9m8oTY307_lNxjgdk38JLMzVERqpPi-GGw02fkuSyc4wOIP1c79Pl9q1pg7N567utDKDGgfyISNav8appCt4fK53ccZ6jpzw4ZK3SJ5nvNzwWeZhuvXW9EKCywkjRz-m_xIsj0wgAqHg5cMxe_QYQq36jBksiqZp31f5T2LeCemIawAMyzEFDhe0AKiZwMYj4UcFq99k92SKSEERgd7COdhUAzfR3Euuo9rKS51P0pmL0KE7js5rY5SZwd08y9I-AyD7-0"
        else -> "https://lh3.googleusercontent.com/aida-public/AB6AXuBlQEY9aRHy4z7ZODjXo_8x684HC4_9ju4c3fqsBzOKDQ99G6s0cskqUf43pFflaLkZokLJ_TUn2rafpgZ9xVHoLpp8pIaGwJjt1L6pAaGj0wYHNvpHQywg3rPFSaQ35HWC40t1pAAc8do7s2tOkSu6L1pfCbRT-ezNp11av_VusNW6QwuxmyZCILVaJzrEa1EVF0AyYzBMh-L2IpoNWY1TFSEXUYRWVOnQFljooKKaZMLegjGMR1zlzQSexebU_hd4EnC060A_aDo"
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(statusColor.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = order.status,
                        color = statusColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = "Order #${order.orderId}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Body
            Row(modifier = Modifier.fillMaxWidth()) {
                Image(
                    painter = rememberAsyncImagePainter(imgUrl),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(12.dp))
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = order.restaurantName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = order.itemsDescription,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Total: Rs. ${order.totalAmount.toInt()}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Footer Button Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                if (order.status == "Delivered") {
                    OutlinedButton(
                        onClick = { onRateOrder(order.orderId) },
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Text("Rate Order", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                } else if (order.status != "Cancelled") {
                    Button(
                        onClick = { onTrackOrder(order.orderId) },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Text("Track Order", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: QuickBiteViewModel,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    val profile by viewModel.userProfile.collectAsState()
    val isDarkTheme by viewModel.isDarkMode.collectAsState()
    val addresses by viewModel.addresses.collectAsState()

    var showAddressDialog by remember { mutableStateOf(false) }
    var addressLabel by remember { mutableStateOf("") }
    var addressDetail by remember { mutableStateOf("") }

    // Camera picture intent launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            // Save bitmap to file system and update profile
            try {
                val file = File(context.cacheDir, "saved_profile_image.jpg")
                val stream = FileOutputStream(file)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream)
                stream.flush()
                stream.close()
                viewModel.updateProfile(
                    name = profile?.name ?: "Ahmad Khan",
                    phone = profile?.phone ?: "+92 321 1234567",
                    photoUri = file.absolutePath
                )
                ToastHelper.showToast("Profile picture updated!")
            } catch (e: Exception) {
                ToastHelper.showToast("Failed to save shot.")
            }
        }
    }

    var showFullMapPicker by remember { mutableStateOf(false) }

    if (showFullMapPicker) {
        com.example.ui.components.FullScreenMapSearchAndSelectDialog(
            initialLabel = addressLabel,
            initialDetail = addressDetail,
            onDismiss = { showFullMapPicker = false },
            onAddressSelected = { label, detail ->
                addressLabel = label
                addressDetail = detail
                showFullMapPicker = false
                showAddressDialog = true // Bring back the confirmation dialog with values pre-populated!
            }
        )
    }

    if (showAddressDialog) {
        AlertDialog(
            onDismissRequest = { showAddressDialog = false },
            title = { Text("Add Delivery Address", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Quick Map Picker trigger button
                    OutlinedButton(
                        onClick = {
                            showAddressDialog = false
                            showFullMapPicker = true
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(Icons.Default.Map, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Locate on Interactive Map", fontWeight = FontWeight.Bold)
                    }

                    OutlinedTextField(
                        value = addressLabel,
                        onValueChange = { addressLabel = it },
                        label = { Text("Address Label (e.g., Home)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = addressDetail,
                        onValueChange = { addressDetail = it },
                        label = { Text("Complete Location Address Details") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (addressLabel.isNotBlank() && addressDetail.isNotBlank()) {
                            viewModel.addUserAddress(addressLabel, addressDetail)
                            addressLabel = ""
                            addressDetail = ""
                            showAddressDialog = false
                            ToastHelper.showToast("Address added successfully!")
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddressDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("My Profile", fontWeight = FontWeight.Bold) })
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Center Profile Picture Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(modifier = Modifier.size(100.dp)) {
                        val defaultAvatar = "https://lh3.googleusercontent.com/aida-public/AB6AXuCeSJOItpdNyqv0yP2ycMMyleWixqRMnW5AVVlHef-FRRfYcaP0IEM8uZvm-_IkpNAgaT57WhHfbeNcOsPBJVK2aIACvJobTziY_PVo68sJaHCLdG0MRjY7q2lEJ-6g8MynPAe4x5Lkv0f0LyaqeDghjTubBs3GVBJ0fUk7YGzjJrLTFXTn6_g3Il-LvWWDICYtydt4R8hagXM8N9F775pvzLqvLONiBynHOzTXReUxTBlceok1CVjDC0DrDBu8kcoa4a1FkAjyzKI"
                        val imageModel = profile?.profilePhotoUri ?: defaultAvatar

                        Image(
                            painter = rememberAsyncImagePainter(model = imageModel),
                            contentDescription = "Ahmad Profile Picture",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                        )
                        
                        // Pencil edit photo button (triggers camera launch!)
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                                .clickable {
                                    // Trigger Camera Intent!
                                    cameraLauncher.launch()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit photo", tint = Color.White, modifier = Modifier.size(14.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = profile?.name ?: "Ahmad Khan",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = profile?.email ?: "ahmad.khan@example.com",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Stats row
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("24", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text("Orders", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Box(modifier = Modifier.width(1.dp).height(32.dp).background(MaterialTheme.colorScheme.outlineVariant))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("8", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text("Saved", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Box(modifier = Modifier.width(1.dp).height(32.dp).background(MaterialTheme.colorScheme.outlineVariant))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("11", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text("Reviews", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            // Options List
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    // Dark Mode support switcher (dynamic toggling as per brief!)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isDarkTheme) Icons.Default.DarkMode else Icons.Default.LightMode,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Dark Theme Support", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                        }
                        Switch(
                            checked = isDarkTheme,
                            onCheckedChange = { viewModel.toggleDarkMode() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = MaterialTheme.colorScheme.primary,
                                uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        )
                    }

                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)))

                    // My Addresses Drawer toggle and lists
                    var isAddressExpanded by remember { mutableStateOf(false) }
                    Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { isAddressExpanded = !isAddressExpanded }
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("My Addresses", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                            }
                            Icon(
                                imageVector = if (isAddressExpanded) Icons.Default.ExpandLess else Icons.Default.ChevronRight,
                                contentDescription = null
                            )
                        }

                        // Addresses view expansion (CRUD create & delete addresses!)
                        if (isAddressExpanded) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                addresses.forEach { addr ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(addr.label, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            Text(addr.detail, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        IconButton(onClick = { viewModel.deleteUserAddress(addr.id) }) {
                                            Icon(Icons.Default.Delete, contentDescription = "Delete Address", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { showAddressDialog = true }
                                        .padding(vertical = 12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Add, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Add New Address", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    }
                                }
                            }
                        }
                    }

                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)))

                    // Helper row
                    ProfileOptionRow(icon = Icons.Default.Info, title = "About QuickBite") {
                        ToastHelper.showToast("QuickBite E-Commerce App v1.0. For Course SE 512.")
                    }
                }
            }

            // Red Logout Button
            Button(
                onClick = onLogout,
                modifier = Modifier
                    .fillButtonWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Logout Account", fontWeight = FontWeight.Bold, color = Color.White)
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun ProfileOptionRow(
    imageVector: androidx.compose.ui.graphics.vector.ImageVector? = null,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
        }
        Icon(Icons.Default.ChevronRight, contentDescription = null)
    }
}

// fluid helper extensions to satisfy Responsive rules "Fluidity"
fun Modifier.fillButtonWidth(): Modifier = this.fillMaxWidth().widthIn(max = 320.dp)
