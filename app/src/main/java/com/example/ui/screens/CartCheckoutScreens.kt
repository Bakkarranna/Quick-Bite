package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.*
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
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.data.CartItem
import com.example.data.OrderHistory
import com.example.viewmodel.QuickBiteViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    viewModel: QuickBiteViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToCheckout: () -> Unit
) {
    val context = LocalContext.current
    val cartItems by viewModel.cartItems.collectAsState()
    val subtotal by viewModel.cartSubtotal.collectAsState()

    var promoCode by remember { mutableStateOf("") }
    var discountAmount by remember { mutableStateOf(0.0) }

    val deliveryFee = if (cartItems.isEmpty()) 0.0 else 100.0 // Rs. 100
    val total = (subtotal + deliveryFee - discountAmount).coerceAtLeast(0.0)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Cart", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (cartItems.isNotEmpty()) {
                        TextButton(onClick = { viewModel.clearCart() }) {
                            Text("Clear All", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        if (cartItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Your cart is empty.", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Vendor header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Icon(Icons.Default.Storefront, contentDescription = null, tint = Color(0xFFFFA500))
                    Text("From: ", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                    Text("Spice Grill & Biryani", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }

                // Cart item list cards
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(1.dp)
                ) {
                    Column {
                        cartItems.forEachIndexed { idx, item ->
                            CartItemRow(
                                item = item,
                                onIncrease = { viewModel.updateCartQuantity(item.id, item.quantity + 1) },
                                onDecrease = { viewModel.updateCartQuantity(item.id, item.quantity - 1) }
                            )

                            if (idx < cartItems.size - 1) {
                                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)))
                            }
                        }
                    }
                }

                // Add more items button
                OutlinedButton(
                    onClick = onNavigateBack,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.AddCircle, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add more items", fontWeight = FontWeight.Bold)
                }

                // Promo code row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = promoCode,
                        onValueChange = { promoCode = it },
                        placeholder = { Text("Promo code") },
                        leadingIcon = { Icon(Icons.Default.LocalOffer, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                        )
                    )
                    Button(
                        onClick = {
                            if (promoCode.trim().equals("CLAIM50", ignoreCase = true)) {
                                discountAmount = subtotal * 0.5
                                Toast.makeText(context, "Promo applied: 50% discount!", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Invalid promo code.", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF684100)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(56.dp)
                    ) {
                        Text("Apply", fontWeight = FontWeight.Bold)
                    }
                }

                // Summary calculations
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("Order Summary", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Subtotal", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                            Text("Rs. ${subtotal.toInt()}", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Delivery Fee", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                            Text("Rs. ${deliveryFee.toInt()}", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                        if (discountAmount > 0) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Discount", color = MaterialTheme.colorScheme.primary, fontSize = 14.sp)
                                Text("-Rs. ${discountAmount.toInt()}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text("Rs. ${total.toInt()}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(90.dp))
            }
        }

        // Fixed checkout footer
        if (cartItems.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 16.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                Button(
                    onClick = onNavigateToCheckout,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(28.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .height(56.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Proceed to Checkout", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White.copy(alpha = 0.2f))
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text("Rs. ${total.toInt()}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CartItemRow(
    item: CartItem,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = rememberAsyncImagePainter(item.imageUrl),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(12.dp))
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(item.name, fontWeight = FontWeight.Bold, fontSize = 15.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(text = "Rs. ${item.price.toInt()}", fontSize = 13.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Extra spicy, mint sauce on side", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        // Stepper Quantity Increments
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                .padding(horizontal = 8.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(onClick = onDecrease, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Default.Remove, contentDescription = "Decrease", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
            }
            Text(text = item.quantity.toString(), fontWeight = FontWeight.Bold, fontSize = 14.sp)
            IconButton(onClick = onIncrease, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Default.Add, contentDescription = "Increase", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    viewModel: QuickBiteViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToPlaced: (String) -> Unit
) {
    val context = LocalContext.current
    val subtotal by viewModel.cartSubtotal.collectAsState()
    val deliveryFee = 100.0
    val total = subtotal + deliveryFee

    var paymentMethod by remember { mutableStateOf("Cash on Delivery") }
    var deliveryTime by remember { mutableStateOf("ASAP") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Checkout", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Delivery Address Header section
            SectionTitle("Delivery Address")
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(1.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Map, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text("Home", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text("123 Foodie Lane, Tech District, Cityville", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    TextButton(onClick = { Toast.makeText(context, "Switch target address from profile list", Toast.LENGTH_SHORT).show() }) {
                        Text("Change", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }

            // Delivery Time asap/schedule grid
            SectionTitle("Delivery Time")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // ASAP option card
                TimeSelectorCard(
                    title = "ASAP",
                    description = "(~30 min)",
                    icon = Icons.Default.Schedule,
                    isSelected = deliveryTime == "ASAP",
                    modifier = Modifier.weight(1f)
                ) { deliveryTime = "ASAP" }

                // Schedule option card
                TimeSelectorCard(
                    title = "Schedule",
                    description = "for Later",
                    icon = Icons.Default.CalendarMonth,
                    isSelected = deliveryTime == "Schedule",
                    modifier = Modifier.weight(1f)
                ) { deliveryTime = "Schedule" }
            }

            // Payment Options list
            SectionTitle("Payment Method")
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(1.dp)
            ) {
                Column {
                    listOf("Cash on Delivery", "JazzCash", "Easypaisa", "Credit/Debit Card").forEachIndexed { idx, pMethod ->
                        PaymentMethodRow(
                            label = pMethod,
                            isSelected = paymentMethod == pMethod,
                            icon = when (pMethod) {
                                "Cash on Delivery" -> Icons.Default.Payments
                                "Credit/Debit Card" -> Icons.Default.CreditCard
                                else -> Icons.Default.AccountBalanceWallet
                            }
                        ) { paymentMethod = pMethod }

                        if (idx < 3) {
                            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)))
                        }
                    }
                }
            }

            // Final Brief Order Calculations
            SectionTitle("Order Summary")
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Subtotal", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                        Text("Rs. ${subtotal.toInt()}", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Delivery Fee", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                        Text("Rs. ${deliveryFee.toInt()}", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Total", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("Rs. ${total.toInt()}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(90.dp))
        }

        // Place order footer
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 16.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Button(
                onClick = {
                    viewModel.checkOutAndPlaceOrder { orderId ->
                        onNavigateToPlaced(orderId)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(28.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(56.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Place Order", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.2f))
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text("Rs. ${total.toInt()}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 8.dp)
    )
}

@Composable
fun TimeSelectorCard(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = modifier.height(100.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            1.5.dp,
            if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(text = description, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun PaymentMethodRow(
    label: String,
    isSelected: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = isSelected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.width(12.dp))
        Text(text = label, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
    }
}

@Composable
fun OrderPlacedScreen(
    orderId: String,
    onTrackOrder: () -> Unit,
    onBackHome: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Gigantic green check circle
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(Color(0xFF4CAF50)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(56.dp))
        }

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = "Order Placed!",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Your order has been confirmed. Get ready for delicious food!",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Order #$orderId",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Scheduled delivery ETA notification banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFFFA500).copy(alpha = 0.2f))
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Schedule, contentDescription = null, tint = Color(0xFF684100))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Estimated Delivery Time: 30–40 min",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF684100),
                    fontSize = 13.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        // Action Buttons stack-md
        Button(
            onClick = onTrackOrder,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text("Track My Order", fontWeight = FontWeight.Bold, color = Color.White)
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = onBackHome,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
            shape = RoundedCornerShape(24.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text("Back to Home", fontWeight = FontWeight.Bold)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackOrderScreen(
    viewModel: QuickBiteViewModel,
    orderId: String,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val orderState by viewModel.getOrderFlow(orderId).collectAsState(initial = null)

    val currentStatus = orderState?.status ?: "Placed"

    // Progress bar calculations
    val (progressIndex, progressPercent) = when (currentStatus) {
        "Placed" -> Pair(0, 0.15f)
        "Preparing" -> Pair(1, 0.45f)
        "On the way" -> Pair(2, 0.75f)
        "Delivered" -> Pair(3, 1f)
        else -> Pair(0, 0.15f)
    }

    Scaffold { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            // Map Area (Background 52%)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.52f)
            ) {
                // Mock Image Map loaded using Coil
                Image(
                    painter = rememberAsyncImagePainter("https://lh3.googleusercontent.com/aida-public/AB6AXuDNY465aJV1GYW-McL-9b9i0J1QWUp5WU1iOBIE3M4lA8dE2QtRF91ny4G5NjGZm8Omp3AK0Et5OMni4tyOruHoqBEbsDEP-2HK6732YCRnIa63M_14FTLTbd4L5f417QotEUp-K16jepPjMNd0Qhauq3fNIi5dACkI49cuGCkMB9e_mygNw_YmvE36ezeS3xbS1_zvlUz1VRTjWoaTmwdmsC97DvbslQcKKhbWxcX4vuvCnoH8dezeu35xnukUxkXy9_O_fackZ5w"),
                    contentDescription = "Rider Path Live Map",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Back Button
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 24.dp)
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.Black)
                }

                // Restaurant start pin
                Box(
                    modifier = Modifier
                        .offset(x = 60.dp, y = 140.dp)
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(1.dp, Color.Gray, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Storefront, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                }

                // Rider Active pin (drawn with pulsating dynamic overlay as in Screen 8!)
                Box(
                    modifier = Modifier
                        .offset(x = 160.dp, y = 110.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                            .border(2.dp, Color.White, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.TwoWheeler, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                    Box(
                        modifier = Modifier
                            .offset(y = 42.dp, x = (-4).dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color.White)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("12 min", fontSize = 9.sp, fontWeight = FontWeight.Black)
                    }
                }

                // Home destination Pin
                Box(
                    modifier = Modifier
                        .offset(x = 280.dp, y = 60.dp)
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Home, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                }
            }

            // Bottom Status Progression sheet (Overlapping slightly, occupying bottom 48%)
            Card(
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(8.dp),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .fillMaxHeight(0.50f)
            ) {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Drag bar
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .size(width = 44.dp, height = 5.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.outlineVariant)
                    )

                    // Header ETA
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Arriving in ~12 min",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Estimated arrival: 7:45 PM",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Interactive Status Stepper Track
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        // Background track bar (connecting line)
                        Box(
                            modifier = Modifier
                                .align(Alignment.CenterStart)
                                .fillMaxWidth()
                                .height(4.dp)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        )
                        // Active track bar progress fill
                        Box(
                            modifier = Modifier
                                .align(Alignment.CenterStart)
                                .fillMaxWidth(progressPercent)
                                .height(4.dp)
                                .background(MaterialTheme.colorScheme.primary)
                        )

                        // 4 Interactive node items
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            listOf("Placed", "Preparing", "On the way", "Delivered").forEachIndexed { index, title ->
                                val completedNode = index <= progressIndex
                                val activeNode = index == progressIndex

                                val bkg = if (completedNode) MaterialTheme.colorScheme.primary else Color.White
                                val borderStroke = if (completedNode) null else BorderStroke(2.dp, MaterialTheme.colorScheme.outlineVariant)

                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(bkg)
                                            .then(if (borderStroke != null) Modifier.border(borderStroke, CircleShape) else Modifier),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = if (index < progressIndex || currentStatus == "Delivered") Icons.Default.Check else {
                                                when (title) {
                                                    "On the way" -> Icons.Default.TwoWheeler
                                                    "Delivered" -> Icons.Default.Home
                                                    else -> Icons.Default.Check
                                                }
                                            },
                                            contentDescription = null,
                                            tint = if (completedNode) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = title,
                                        fontSize = 10.sp,
                                        color = if (activeNode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = if (activeNode) FontWeight.Bold else FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }

                    // Rider profile Usman Card (Screen 8)
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                painter = rememberAsyncImagePainter("https://lh3.googleusercontent.com/aida-public/AB6AXuBgBHAXEegOVYxO42FEn3UAF6wVu_I-PM1o9W-uxlS0IUbW2-22E5cqFj_u9wa8NsSZhF8luUCTiW2SwKbahPHAKNf1b3M4tiw3ELSfW5Hqie_UduxCa4CgxfLXj9EeAv56MSkgR_Het3UNvpgj150XCSi9XoyyB1tOWkVAtWXj53QKUXcCTqi6LXWmbIz5RrbmtKhVpHWaD_GXXQCvguNyD-sBF49iNsz_QhZ0wcnCr15a0MFWBJ5kp-eRqzJ4T6bD4eVK2OsqNYg"),
                                contentDescription = "Usman",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text("Usman", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFA500), modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("4.9", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("(2k+ trips)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                IconButton(
                                    onClick = { Toast.makeText(context, "Chat screen currently simulated.", Toast.LENGTH_SHORT).show() },
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.surfaceVariant),
                                ) {
                                    Icon(Icons.Default.Chat, contentDescription = "Chat", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                }
                                IconButton(
                                    onClick = { Toast.makeText(context, "Dialing rider Usman (+92 301 2345678)...", Toast.LENGTH_SHORT).show() },
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary),
                                ) {
                                    Icon(Icons.Default.Call, contentDescription = "Call", tint = Color.White, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }

                    // Collapsed food menu items list details drawer
                    var isDrawerExpanded by remember { mutableStateOf(false) }
                    Card(
                        onClick = { isDrawerExpanded = !isDrawerExpanded },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("Chicken Biryani Combo", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text("+ 2 more items", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            Icon(imageVector = if (isDrawerExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore, contentDescription = null)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RateOrderScreen(
    viewModel: QuickBiteViewModel,
    orderId: String,
    onFinish: () -> Unit
) {
    val context = LocalContext.current
    var rating by remember { mutableStateOf(4) }
    var reviewComment by remember { mutableStateOf("") }
    
    // Tag feedbacks toggle listing state
    val defaultTags = listOf("Fast Delivery", "Hot Food", "Good Packaging", "Friendly Rider", "Tasty!")
    val selectedTags = remember { mutableStateListOf<String>("Fast Delivery", "Good Packaging") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Rate Your Order", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onFinish) {
                        Icon(Icons.Default.Close, contentDescription = "Cancel")
                    }
                }
            )
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
            // Restaurant brief logo banner info Row (Screen 9)
            Card(
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = rememberAsyncImagePainter("https://lh3.googleusercontent.com/aida-public/AB6AXuB9KVdx714jtz-S5J2wCNac9F573m8IkmeUaQdxmksy4X7FL3Nv_XXveydQFNk3_K_KZ_sLDxc8WQdTGO4ltqh447grblcghXTJy3YoyzTRMA1RKGe_sobrHd4vtQ6zQMsyCVaxjpt6D3ldFS62ea4Xe8R3TexLE7UEG5q8k1wihM6ooWu8XJnZZ_eyQWeOyIL3RD8lp1Kncebr99vzk_0cfmkd3OsgIWzQWKnf3VLj8Q-566-QQ_AtQs9uSUsB09PtwS1Jgdsf3Ng"),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Burger & Co.", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("Delivered 15 mins ago", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            // Clickable Star rating block (Screen 9)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(vertical = 12.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    (1..5).forEach { starIndex ->
                        val active = starIndex <= rating
                        IconButton(onClick = { rating = starIndex }) {
                            Icon(
                                imageVector = if (active) Icons.Default.Star else Icons.Default.StarBorder,
                                contentDescription = "Star $starIndex",
                                tint = if (active) Color(0xFFFFA500) else MaterialTheme.colorScheme.outlineVariant,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text("Great!", fontWeight = FontWeight.Black, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                Text("Tap to rate", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            // Chip feedback selectors
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("What did you like?", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                val chunkedTags = defaultTags.chunked(3)
                chunkedTags.forEach { rowTags ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowTags.forEach { tagText ->
                            val isSelected = selectedTags.contains(tagText)
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    if (isSelected) selectedTags.remove(tagText)
                                    else selectedTags.add(tagText)
                                },
                                label = { Text(tagText, fontSize = 12.sp) },
                                shape = RoundedCornerShape(20.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Text comments write feedback box
            OutlinedTextField(
                value = reviewComment,
                onValueChange = { reviewComment = it },
                placeholder = { Text("Tell us more about your experience...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Submit review buttons
            Button(
                onClick = {
                    viewModel.submitOrderReview(
                        orderId = orderId,
                        restaurantName = "Burger & Co.",
                        rating = rating.toFloat(),
                        comment = reviewComment,
                        tags = selectedTags.joinToString(",")
                    )
                    Toast.makeText(context, "Review submitted successfully! Thank you Ahmed.", Toast.LENGTH_LONG).show()
                    onFinish()
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text("Submit Review", fontWeight = FontWeight.Bold, color = Color.White)
            }

            Spacer(modifier = Modifier.height(12.dp))

            TextButton(onClick = onFinish) {
                Text("Skip for now", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
            }
        }
    }
}
