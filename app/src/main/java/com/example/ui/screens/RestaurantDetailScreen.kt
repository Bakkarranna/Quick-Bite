package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.viewmodel.QuickBiteViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RestaurantDetailScreen(
    viewModel: QuickBiteViewModel,
    restaurantId: String,
    onNavigateBack: () -> Unit,
    onNavigateToCart: () -> Unit
) {
    val context = LocalContext.current
    val cartCount by viewModel.cartCount.collectAsState()
    val cartSubtotal by viewModel.cartSubtotal.collectAsState()

    var isFavorite by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf("Starters") }

    val categories = listOf("Starters", "Main Course", "Biryani", "Drinks", "Desserts")

    // Mock Menu Items
    val menuItems = listOf(
        MenuItemData(
            id = "item-01",
            title = "Chicken Tikka",
            desc = "Spicy, marinated chicken chunks grilled to perfection over charcoal. Served with mint chutney.",
            price = 450.0,
            imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuCDvlugPMIPbpn-BsAKBpemsm7UxrDicSnC2pklBmCCncUa6UGsR-TI543oZsW0rhajvmXuv5QgsZEbfXrKJ4hLalyuTH6ptasBiYqpVTnSAhPj_5NUWkfQaHRmTdZ3flZbqwQ8d6NjY1wdSCSGGY7XYWbjq_R8hnQ6eQEJHE0d2X7iYRk7NU75N0466KsLZbH1uMZCjD1WDji9L3m7Yu0uUFTc8GA7E6LsXyswPrNArYxCRyp0gTXjHaIAH9uALH0b4-DC-fmB3AA",
            isBestseller = true
        ),
        MenuItemData(
            id = "item-02",
            title = "Seekh Kebab (4pcs)",
            desc = "Minced beef mixed with aromatic spices, skewered and grilled. A classic favorite.",
            price = 600.0,
            imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuAnbRj_5Qtr8FgEa7GsEPh7oO73tqObMqPITpQpepmHAuOPFg5FloNdAAsKayHOLbYygGMqOwcEXalIiid8HQN8PyVgR-FHyLcOyNZ9LwS4BtNcvpjPMgqlxTo-AZZ3xRZ9xzad3UbKKNLJrxg9dNvPJG8s6kXAERxapf_v1M_Y3c3vHHsE9aRq2n-js51_8vRRMze3GHejyPscjJbu76VxeOhI7Fd2QHid6x8KH4uH2jOr05nbiCkM9lYy263OP6yiFko1yjw_kTI",
            isBestseller = false
        ),
        MenuItemData(
            id = "item-03",
            title = "Paneer Tikka",
            desc = "Vegetarian delight featuring marinated cottage cheese cubes grilled with capsicum and onions.",
            price = 550.0,
            imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuDewwk-mu7gPw0FA1Ko3gguqoEwn_vKlCnxPQ00F7zn3XsA7koFfsjTTeLhdAAFgdhKFB94KydpQHZU2s9hP-h6_gYS62qSTqMJtClijDAD5fFuJw0SeGIC5EMEAia6ZiuSd8J4L4yMAwFQ2eP2aVHdr-4BIapiFqa4Y_yLDlq67wcmpVgLqq5FbwUo20gxiQfg6l2HSMXlUDC0NseB4DcpvQiEiaegS7_yGDJltL99i_1can7l-ehXPp4HjplLow9CDVC9qoWpIpw",
            isBestseller = false
        )
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Immersive Backdrop Image and Navs
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
            ) {
                Image(
                    painter = rememberAsyncImagePainter("https://lh3.googleusercontent.com/aida-public/AB6AXuBlQEY9aRHy4z7ZODjXo_8x684HC4_9ju4c3fqsBzOKDQ99G6s0cskqUf43pFflaLkZokLJ_TUn2rafpgZ9xVHoLpp8pIaGwJjt1L6pAaGj0wYHNvpHQywg3rPFSaQ35HWC40t1pAAc8do7s2tOkSu6L1pfCbRT-ezNp11av_VusNW6QwuxmyZCILVaJzrEa1EVF0AyYzBMh-L2IpoNWY1TFSEXUYRWVOnQFljooKKaZMLegjGMR1zlzQSexebU_hd4EnC060A_aDo"),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Back Button and Favorite Header row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 40.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.8f))
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.Black)
                    }
                    IconButton(
                        onClick = { isFavorite = !isFavorite },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.8f))
                    ) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (isFavorite) Color.Red else Color.Black
                        )
                    }
                }
            }

            // Info Card header overlapping slightly
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 16.dp, vertical = 20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Spice Grill & Biryani",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFA500), modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("4.8", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text("(500+)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Cuisine chips
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Pakistani", "BBQ", "Grill").forEach { cuisine ->
                        SuggestionChip(
                            onClick = { },
                            label = { Text(cuisine, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Detail metadata times row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.Schedule, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                        Text("30-45 min", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Box(modifier = Modifier.size(4.dp).clip(CircleShape).background(MaterialTheme.colorScheme.outline))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.LocalMall, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                        Text("Min. Rs. 500", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Box(modifier = Modifier.size(4.dp).clip(CircleShape).background(MaterialTheme.colorScheme.outline))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                        Text("2.5 km", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            // Categories Switch Sticky-equivalent Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                categories.forEach { cat ->
                    val isActive = cat == selectedCategory
                    Column(
                        modifier = Modifier
                            .clickable { selectedCategory = cat }
                            .padding(vertical = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = cat,
                            fontSize = 15.sp,
                            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                            color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .width(20.dp)
                                .height(2.dp)
                                .clip(CircleShape)
                                .background(if (isActive) MaterialTheme.colorScheme.primary else Color.Transparent)
                        )
                    }
                }
            }

            // Starters content list
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Starters",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                menuItems.forEach { item ->
                    MenuCardRow(
                        item = item,
                        onAdd = {
                            viewModel.addItemToCart(
                                id = item.id,
                                name = item.title,
                                price = item.price,
                                imageUrl = item.imageUrl
                            )
                            Toast.makeText(context, "${item.title} added to cart!", Toast.LENGTH_SHORT).show()
                        }
                    )
                    Spacer(modifier = Modifier.height(1.dp).fillMaxWidth().background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)))
                }
            }

            Spacer(modifier = Modifier.height(100.dp)) // padding safe area for bottom floating trigger
        }

        // View Cart bottom drawer floating trigger
        AnimatedVisibility(
            visible = cartCount > 0,
            enter = slideInVertically(initialOffsetY = { it }, animationSpec = tween(300)) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }, animationSpec = tween(300)) + fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        ) {
            Button(
                onClick = onNavigateToCart,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(28.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(Color.White),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = cartCount.toString(),
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("View Cart", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }

                    Text(
                        text = "Rs. ${cartSubtotal.toInt()}",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
fun MenuCardRow(
    item: MenuItemData,
    onAdd: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = rememberAsyncImagePainter(item.imageUrl),
            contentDescription = item.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(12.dp))
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier
                .weight(1.0f)
                .padding(end = 8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = item.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (item.isBestseller) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFFFFA500).copy(alpha = 0.2f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "BESTSELLER",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF684100)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = item.desc,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Rs. ${item.price.toInt()}",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        // Circular Add button
        IconButton(
            onClick = onAdd,
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add item", tint = Color.White)
        }
    }
}

data class MenuItemData(
    val id: String,
    val title: String,
    val desc: String,
    val price: Double,
    val imageUrl: String,
    val isBestseller: Boolean
)
