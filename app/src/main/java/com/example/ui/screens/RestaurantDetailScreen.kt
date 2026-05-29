package com.example.ui.screens

import android.widget.Toast
import com.example.ui.components.ToastHelper
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
    val restaurants by viewModel.restaurantsList.collectAsState()

    // Find the real dynamic restaurant
    val restaurant = remember(restaurants, restaurantId) {
        restaurants.find { it.id == restaurantId } ?: restaurants.first()
    }

    var isFavorite by remember { mutableStateOf(false) }
    
    // Dyn categories based on category tagging
    val categories = remember(restaurant) {
        if (restaurant.categories.contains("Pizza")) {
            listOf("Pizza Specials", "Appetizers", "Drinks", "Desserts")
        } else if (restaurant.categories.contains("Burgers")) {
            listOf("Burgers", "Sides", "Drinks", "Desserts")
        } else if (restaurant.categories.contains("Desserts")) {
            listOf("Gourmet Cakes", "Sweet Donuts", "Ice Cream", "Drinks")
        } else if (restaurant.categories.contains("Drinks")) {
            listOf("Specialty Shakes", "Juices & Mojitos", "Drinks")
        } else if (restaurant.categories.contains("Chinese")) {
            listOf("Noodles & Rice", "Soups & Starters", "Drinks")
        } else {
            listOf("Biryani Specials", "BBQ Grill Tikka", "Main Choice", "Drinks", "Desserts")
        }
    }

    var selectedCategory by remember(categories) { mutableStateOf(categories.firstOrNull() ?: "Starters") }

    // Dynamically populated menu items based on category and selected tab category
    val menuItems = remember(restaurant, selectedCategory) {
        val lowercaseCat = selectedCategory.lowercase()
        when {
            lowercaseCat.contains("biryani") -> listOf(
                MenuItemData(
                    id = "${restaurant.id}-b1",
                    title = "Royal Chicken Biryani",
                    desc = "Aromatic basmati rice cooked with tender chicken and authentic spices of Faisalabad.",
                    price = 320.0,
                    imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuCDR2sEJYHUbyL7JGFr4hrczv9zJKvyDHR88AmKAuPGDO4yBi_V3kH38PylG5OmPvAeFlvqVfD4D_VlpU_eDODrtFNETnx6jIaLuVTob1tTUMFNl10-g1LPd1KtH7hTiGE2L8GNo46ZMz6A9YvU04NH3eio5o1tdYxh8i6F1y_vWd5jCWmAzIud9JIux5XPEefhCyG3Eusq1-mrNKsgmHw8ON49fcmQQ9ZvmsKZKCnTN6tqNhHb0KvPKewnvFZzkcOT4z4fbqFWFnM",
                    isBestseller = true
                ),
                MenuItemData(
                    id = "${restaurant.id}-b2",
                    title = "Egg Shami Biryani Box",
                    desc = "Fragrant rice layers loaded with hard-boiled egg and crispy hand-made Desi shami kebabs.",
                    price = 280.0,
                    imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuCDvlugPMIPbpn-BsAKBpemsm7UxrDicSnC2pklBmCCncUa6UGsR-TI543oZsW0rhajvmXuv5QgsZEbfXrKJ4hLalyuTH6ptasBiYqpVTnSAhPj_5NUWkfQaHRmTdZ3flZbqwQ8d6NjY1wdSCSGGY7XYWbjq_R8hnQ6eQEJHE0d2X7iYRk7NU75N0466KsLZbH1uMZCjD1WDji9L3m7Yu0uUFTc8GA7E6LsXyswPrNArYxCRyp0gTXjHaIAH9uALH0b4-DC-fmB3AA",
                    isBestseller = false
                ),
                MenuItemData(
                    id = "${restaurant.id}-b3",
                    title = "Double Masala Raita Biryani",
                    desc = "Spiced up rice variant with extra green chilies and customized herbal raita yogurt yogurt.",
                    price = 350.0,
                    imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuCDR2sEJYHUbyL7JGFr4hrczv9zJKvyDHR88AmKAuPGDO4yBi_V3kH38PylG5OmPvAeFlvqVfD4D_VlpU_eDODrtFNETnx6jIaLuVTob1tTUMFNl10-g1LPd1KtH7hTiGE2L8GNo46ZMz6A9YvU04NH3eio5o1tdYxh8i6F1y_vWd5jCWmAzIud9JIux5XPEefhCyG3Eusq1-mrNKsgmHw8ON49fcmQQ9ZvmsKZKCnTN6tqNhHb0KvPKewnvFZzkcOT4z4fbqFWFnM",
                    isBestseller = true
                )
            )
            lowercaseCat.contains("pizza") -> listOf(
                MenuItemData(
                    id = "${restaurant.id}-p1",
                    title = "Faisalabad Sizzler Pizza",
                    desc = "Hot tikka chunks, jalapeños, onions, black olives, and premium mozzarella premium cheese.",
                    price = 680.0,
                    imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuA65KNguaZK-LgcyGoTJYDtV9zjN2VtLIdi6673a9CtmpTJRn1kyuZIhS2OzJGvFf66fh_BYghVg5fHrMh3b5pE0EYfcAf031xAbpA8iwoKlgI2DKTfrVKhSVqi5HbFqO18TGOpy5NsBFGHgQMmDNu6FhBpcyfp7Cz1oi0_Ew0tve1iy5OPYpkmgSWwj8PFF8M3MGD3XqiqDMNMZf0cAGEea4rbkSbVWNB9L0bIyIh63x140kHGLJyik9NevJBsT-srbMr7yojU2ds",
                    isBestseller = true
                ),
                MenuItemData(
                    id = "${restaurant.id}-p2",
                    title = "Creamy Garlic Crown Crust",
                    desc = "Overloaded chicken with deep garlic sauce dips, stuffed rich crown edges, extra cheese.",
                    price = 890.0,
                    imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuA65KNguaZK-LgcyGoTJYDtV9zjN2VtLIdi6673a9CtmpTJRn1kyuZIhS2OzJGvFf66fh_BYghVg5fHrMh3b5pE0EYfcAf031xAbpA8iwoKlgI2DKTfrVKhSVqi5HbFqO18TGOpy5NsBFGHgQMmDNu6FhBpcyfp7Cz1oi0_Ew0tve1iy5OPYpkmgSWwj8PFF8M3MGD3XqiqDMNMZf0cAGEea4rbkSbVWNB9L0bIyIh63x140kHGLJyik9NevJBsT-srbMr7yojU2ds",
                    isBestseller = false
                ),
                MenuItemData(
                    id = "${restaurant.id}-p3",
                    title = "Fajita Fiesta Slices",
                    desc = "Mexican-spiced loaded chicken pieces, bell peppers, fresh tomatoes, coriander toppings.",
                    price = 590.0,
                    imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuA65KNguaZK-LgcyGoTJYDtV9zjN2VtLIdi6673a9CtmpTJRn1kyuZIhS2OzJGvFf66fh_BYghVg5fHrMh3b5pE0EYfcAf031xAbpA8iwoKlgI2DKTfrVKhSVqi5HbFqO18TGOpy5NsBFGHgQMmDNu6FhBpcyfp7Cz1oi0_Ew0tve1iy5OPYpkmgSWwj8PFF8M3MGD3XqiqDMNMZf0cAGEea4rbkSbVWNB9L0bIyIh63x140kHGLJyik9NevJBsT-srbMr7yojU2ds",
                    isBestseller = false
                )
            )
            lowercaseCat.contains("burger") -> listOf(
                MenuItemData(
                    id = "${restaurant.id}-br1",
                    title = "Gourmet Mighty Zinger",
                    desc = "Crispy, deep-fried chicken breast with custom spicy mayo, crunchy iceberg lettuce layers.",
                    price = 330.0,
                    imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuB9KVdx714jtz-S5J2wCNac9F573m8IkmeUaQdxmksy4X7FL3Nv_XXveydQFNk3_K_KZ_sLDxc8WQdTGO4ltqh447grblcghXTJy3YoyzTRMA1RKGe_sobrHd4vtQ6zQMsyCVaxjpt6D3ldFS62ea4Xe8R3TexLE7UEG5q8k1wihM6ooWu8XJnZZ_eyQWeOyIL3RD8lp1Kncebr99vzk_0cfmkd3OsgIWzQWKnf3VLj8Q-566-QQ_AtQs9uSUsB09PtwS1Jgdsf3Ng",
                    isBestseller = true
                ),
                MenuItemData(
                    id = "${restaurant.id}-br2",
                    title = "Double Cheese Angus Smash",
                    desc = "Twin beef patties smashed flat over hot griddle, double cheddar, specialized burger sauce.",
                    price = 490.0,
                    imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuB9KVdx714jtz-S5J2wCNac9F573m8IkmeUaQdxmksy4X7FL3Nv_XXveydQFNk3_K_KZ_sLDxc8WQdTGO4ltqh447grblcghXTJy3YoyzTRMA1RKGe_sobrHd4vtQ6zQMsyCVaxjpt6D3ldFS62ea4Xe8R3TexLE7UEG5q8k1wihM6ooWu8XJnZZ_eyQWeOyIL3RD8lp1Kncebr99vzk_0cfmkd3OsgIWzQWKnf3VLj8Q-566-QQ_AtQs9uSUsB09PtwS1Jgdsf3Ng",
                    isBestseller = true
                )
            )
            lowercaseCat.contains("dessert") || lowercaseCat.contains("cake") || lowercaseCat.contains("sweet") -> listOf(
                MenuItemData(
                    id = "${restaurant.id}-d1",
                    title = "Molten Lava Deluxe Cake",
                    desc = "Rich warm cake with direct chocolate fluid core flow, topped with custom cocoa powder.",
                    price = 260.0,
                    imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuDBrL6c9xuYqNackMqOuSTfMweK1PbSBU-VKsAx7o7y9D6YFS5pdLl_GQadf_e-Fl942Q4QlCTF4kozIMBCJymaUVWt0VVCB-P3QfMxPppzvZs0D1fxaDBe6JWn3-ANmTeIHOb_BoD92S_JI7BLiQkNgfIKRXeNjbRsIGtoImUvzAFlnZG4QJQPTKyBrmrfKltQ1HyFhjXK9VGOdNKyajJvSkgmbEWQhzqH8wXsODN1Sx3A35X5XmLtjml0wV6X0TEVr6FJkaMVlec",
                    isBestseller = true
                ),
                MenuItemData(
                    id = "${restaurant.id}-d2",
                    title = "Royal Faisalabadi Falooda",
                    desc = "A dense bowl with sweet ice-chilled condensed kulfi sticks, sweet noodles, dry nuts.",
                    price = 190.0,
                    imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuDBrL6c9xuYqNackMqOuSTfMweK1PbSBU-VKsAx7o7y9D6YFS5pdLl_GQadf_e-Fl942Q4QlCTF4kozIMBCJymaUVWt0VVCB-P3QfMxPppzvZs0D1fxaDBe6JWn3-ANmTeIHOb_BoD92S_JI7BLiQkNgfIKRXeNjbRsIGtoImUvzAFlnZG4QJQPTKyBrmrfKltQ1HyFhjXK9VGOdNKyajJvSkgmbEWQhzqH8wXsODN1Sx3A35X5XmLtjml0wV6X0TEVr6FJkaMVlec",
                    isBestseller = false
                )
            )
            lowercaseCat.contains("drink") || lowercaseCat.contains("juice") || lowercaseCat.contains("shake") -> listOf(
                MenuItemData(
                    id = "${restaurant.id}-dr1",
                    title = "Mint Margarita Fizz",
                    desc = "Ice blend of fresh mint, lemon wedges, white soda syrup, sugar granules.",
                    price = 140.0,
                    imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuDBrL6c9xuYqNackMqOuSTfMweK1PbSBU-VKsAx7o7y9D6YFS5pdLl_GQadf_e-Fl942Q4QlCTF4kozIMBCJymaUVWt0VVCB-P3QfMxPppzvZs0D1fxaDBe6JWn3-ANmTeIHOb_BoD92S_JI7BLiQkNgfIKRXeNjbRsIGtoImUvzAFlnZG4QJQPTKyBrmrfKltQ1HyFhjXK9VGOdNKyajJvSkgmbEWQhzqH8wXsODN1Sx3A35X5XmLtjml0wV6X0TEVr6FJkaMVlec",
                    isBestseller = true
                ),
                MenuItemData(
                    id = "${restaurant.id}-dr2",
                    title = "Thick Mango Milkshake",
                    desc = "Pristine dairy blend cooked sweet mango chunks with dense whipping cream.",
                    price = 180.0,
                    imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuDBrL6c9xuYqNackMqOuSTfMweK1PbSBU-VKsAx7o7y9D6YFS5pdLl_GQadf_e-Fl942Q4QlCTF4kozIMBCJymaUVWt0VVCB-P3QfMxPppzvZs0D1fxaDBe6JWn3-ANmTeIHOb_BoD92S_JI7BLiQkNgfIKRXeNjbRsIGtoImUvzAFlnZG4QJQPTKyBrmrfKltQ1HyFhjXK9VGOdNKyajJvSkgmbEWQhzqH8wXsODN1Sx3A35X5XmLtjml0wV6X0TEVr6FJkaMVlec",
                    isBestseller = false
                )
            )
            lowercaseCat.contains("bbq") || lowercaseCat.contains("grill") || lowercaseCat.contains("tikka") -> listOf(
                MenuItemData(
                    id = "${restaurant.id}-q1",
                    title = "Spicy Malai Boti (Skewer)",
                    desc = "Marinated creamy chicken chunks skewered and slow charcoal cooked. Extemely tender.",
                    price = 420.0,
                    imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuBlQEY9aRHy4z7ZODjXo_8x684HC4_9ju4c3fqsBzOKDQ99G6s0cskqUf43pFflaLkZokLJ_TUn2rafpgZ9xVHoLpp8pIaGwJjt1L6pAaGj0wYHNvpHQywg3rPFSaQ35HWC40t1pAAc8do7s2tOkSu6L1pfCbRT-ezNp11av_VusNW6QwuxmyZCILVaJzrEa1EVF0AyYzBMh-L2IpoNWY1TFSEXUYRWVOnQFljooKKaZMLegjGMR1zlzQSexebU_hd4EnC060A_aDo",
                    isBestseller = true
                ),
                MenuItemData(
                    id = "${restaurant.id}-q2",
                    title = "Charcoal BBQ Tikka Leg Piece",
                    desc = "Tender leg quarter roasted with special tandoori red masalas, spicy juice flavor.",
                    price = 310.0,
                    imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuBlQEY9aRHy4z7ZODjXo_8x684HC4_9ju4c3fqsBzOKDQ99G6s0cskqUf43pFflaLkZokLJ_TUn2rafpgZ9xVHoLpp8pIaGwJjt1L6pAaGj0wYHNvpHQywg3rPFSaQ35HWC40t1pAAc8do7s2tOkSu6L1pfCbRT-ezNp11av_VusNW6QwuxmyZCILVaJzrEa1EVF0AyYzBMh-L2IpoNWY1TFSEXUYRWVOnQFljooKKaZMLegjGMR1zlzQSexebU_hd4EnC060A_aDo",
                    isBestseller = false
                )
            )
            lowercaseCat.contains("chinese") || lowercaseCat.contains("noodles") -> listOf(
                MenuItemData(
                    id = "${restaurant.id}-ch1",
                    title = "Egg Chicken Chow Mein Noodles",
                    desc = "Traditional wok fried noodles with soy sauce sprinkles, mixed capsicum cubes.",
                    price = 450.0,
                    imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuCDR2sEJYHUbyL7JGFr4hrczv9zJKvyDHR88AmKAuPGDO4yBi_V3kH38PylG5OmPvAeFlvqVfD4D_VlpU_eDODrtFNETnx6jIaLuVTob1tTUMFNl10-g1LPd1KtH7hTiGE2L8GNo46ZMz6A9YvU04NH3eio5o1tdYxh8i6F1y_vWd5jCWmAzIud9JIux5XPEefhCyG3Eusq1-mrNKsgmHw8ON49fcmQQ9ZvmsKZKCnTN6tqNhHb0KvPKewnvFZzkcOT4z4fbqFWFnM",
                    isBestseller = true
                ),
                MenuItemData(
                    id = "${restaurant.id}-ch2",
                    title = "Fiery Manchurian Rice Platter",
                    desc = "Spicy red chili chicken cubes in sticky gravy served alongside egg fried fried rice layers.",
                    price = 490.0,
                    imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuCDR2sEJYHUbyL7JGFr4hrczv9zJKvyDHR88AmKAuPGDO4yBi_V3kH38PylG5OmPvAeFlvqVfD4D_VlpU_eDODrtFNETnx6jIaLuVTob1tTUMFNl10-g1LPd1KtH7hTiGE2L8GNo46ZMz6A9YvU04NH3eio5o1tdYxh8i6F1y_vWd5jCWmAzIud9JIux5XPEefhCyG3Eusq1-mrNKsgmHw8ON49fcmQQ9ZvmsKZKCnTN6tqNhHb0KvPKewnvFZzkcOT4z4fbqFWFnM",
                    isBestseller = false
                )
            )
            else -> listOf(
                MenuItemData(
                    id = "${restaurant.id}-item-01",
                    title = "Signature Specialty Biryani",
                    desc = "Spicy, marinated, traditional recipe rice cooked in our custom wood-fired kitchen. Served high heat.",
                    price = 360.0,
                    imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuCDvlugPMIPbpn-BsAKBpemsm7UxrDicSnC2pklBmCCncUa6UGsR-TI543oZsW0rhajvmXuv5QgsZEbfXrKJ4hLalyuTH6ptasBiYqpVTnSAhPj_5NUWkfQaHRmTdZ3flZbqwQ8d6NjY1wdSCSGGY7XYWbjq_R8hnQ6eQEJHE0d2X7iYRk7NU75N0466KsLZbH1uMZCjD1WDji9L3m7Yu0uUFTc8GA7E6LsXyswPrNArYxCRyp0gTXjHaIAH9uALH0b4-DC-fmB3AA",
                    isBestseller = true
                ),
                MenuItemData(
                    id = "${restaurant.id}-item-02",
                    title = "Seekh Kebab Platter (4pcs)",
                    desc = "Minced meat cooked with classic herbs and char-broiled. Deliciously spiced.",
                    price = 450.0,
                    imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuAnbRj_5Qtr8FgEa7GsEPh7oO73tqObMqPITpQpepmHAuOPFg5FloNdAAsKayHOLbYygGMqOwcEXalIiid8HQN8PyVgR-FHyLcOyNZ9LwS4BtNcvpjPMgqlxTo-AZZ3xRZ9xzad3UbKKNLJrxg9dNvPJG8s6kXAERxapf_v1M_Y3c3vHHsE9aRq2n-js51_8vRRMze3GHejyPscjJbu76VxeOhI7Fd2QHid6x8KH4uH2jOr05nbiCkM9lYy263OP6yiFko1yjw_kTI",
                    isBestseller = false
                )
            )
        }
    }

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
                    painter = rememberAsyncImagePainter(restaurant.imageUrl),
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
                        text = restaurant.title,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFA500), modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(restaurant.rating, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Cuisine chips dynamically split
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.horizontalScroll(rememberScrollState())
                ) {
                    restaurant.tags.split("•").forEach { txt ->
                        val cleanCuisine = txt.trim()
                        if (cleanCuisine.isNotEmpty()) {
                            SuggestionChip(
                                onClick = { },
                                label = { Text(cleanCuisine, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                shape = RoundedCornerShape(8.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Detail metadata times row dynamically taken from restaurant state!
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
                        Text(restaurant.time, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Box(modifier = Modifier.size(4.dp).clip(CircleShape).background(MaterialTheme.colorScheme.outline))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.LocalMall, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                        Text(restaurant.price, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Box(modifier = Modifier.size(4.dp).clip(CircleShape).background(MaterialTheme.colorScheme.outline))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                        Text(restaurant.distance, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
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

            // Starters content list dynamically matching selections
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = selectedCategory,
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
                            ToastHelper.showToast("${item.title} added to cart!")
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
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "BESTSELLER",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary
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
