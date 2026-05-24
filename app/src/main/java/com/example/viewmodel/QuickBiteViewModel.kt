package com.example.viewmodel

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.*
import com.example.service.DeliverySimulationService
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.random.Random

class QuickBiteViewModel(
    private val context: Context,
    private val repository: QuickBiteRepository
) : ViewModel() {

    private val sharedPrefs = context.getSharedPreferences("quickbite_prefs", Context.MODE_PRIVATE)

    // Dark Mode Theme Preference
    var isDarkMode = MutableStateFlow(sharedPrefs.getBoolean("dark_mode", false))
        private set

    // Active logged in user
    val activeUserEmail = sharedPrefs.getString("logged_in_email", "ahmed.khan@example.com") ?: "ahmed.khan@example.com"
    
    val userProfile: StateFlow<UserProfile?> = repository.getUserProfile(activeUserEmail)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    // reactive listings from SQLite
    val cartItems: StateFlow<List<CartItem>> = repository.cartItems
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val orders: StateFlow<List<OrderHistory>> = repository.allOrders
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val addresses: StateFlow<List<DeliveryAddress>> = repository.allAddresses
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val reviews: StateFlow<List<ReviewItem>> = repository.allReviews
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Cart calculations
    val cartSubtotal: StateFlow<Double> = cartItems
        .map { list -> list.sumOf { it.price * it.quantity } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val cartCount: StateFlow<Int> = cartItems
        .map { list -> list.sumOf { it.quantity } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun toggleDarkMode() {
        val nextVal = !isDarkMode.value
        sharedPrefs.edit().putBoolean("dark_mode", nextVal).apply()
        isDarkMode.value = nextVal
    }

    // SQLite operations (Write actions inside coroutine threads)
    fun addItemToCart(id: String, name: String, price: Double, imageUrl: String, notes: String = "") {
        viewModelScope.launch {
            val existing = cartItems.value.find { it.id == id }
            if (existing != null) {
                repository.addToCart(existing.copy(quantity = existing.quantity + 1))
            } else {
                repository.addToCart(CartItem(id = id, name = name, price = price, quantity = 1, imageUrl = imageUrl, notes = notes))
            }
        }
    }

    fun updateCartQuantity(id: String, quantity: Int) {
        viewModelScope.launch {
            repository.updateCartQuantity(id, quantity)
        }
    }

    fun deleteItemFromCart(id: String) {
        viewModelScope.launch {
            repository.removeFromCart(id)
        }
    }

    fun clearCart() {
        viewModelScope.launch {
            repository.clearCart()
        }
    }

    fun addUserAddress(label: String, detail: String) {
        viewModelScope.launch {
            repository.addAddress(DeliveryAddress(label = label, detail = detail))
        }
    }

    fun deleteUserAddress(id: Int) {
        viewModelScope.launch {
            repository.removeAddress(id)
        }
    }

    fun updateProfile(name: String, phone: String, photoUri: String? = null) {
        viewModelScope.launch {
            repository.saveUserProfile(
                UserProfile(
                    email = activeUserEmail,
                    name = name,
                    phone = phone,
                    profilePhotoUri = photoUri ?: userProfile.value?.profilePhotoUri
                )
            )
        }
    }

    fun getOrderFlow(orderId: String): Flow<OrderHistory?> = repository.getOrderFlow(orderId)

    // Complete Checkout Action
    fun checkOutAndPlaceOrder(onPlaced: (String) -> Unit) {
        viewModelScope.launch {
            val items = cartItems.value
            if (items.isEmpty()) return@launch

            val subtotal = items.sumOf { it.price * it.quantity }
            val deliveryFee = 100.0 // Rs. 100
            val total = subtotal + deliveryFee
            
            val itemSummary = items.joinToString(", ") { "${it.quantity}x ${it.name}" }
            val orderId = "QB-${Random.nextInt(1000, 9999)}"

            val newOrder = OrderHistory(
                orderId = orderId,
                restaurantName = "Spice Grill & Biryani",
                status = "Placed",
                totalAmount = total,
                itemsDescription = itemSummary,
                timestamp = System.currentTimeMillis()
            )

            // 1. Save order inside Room SQLite database
            repository.placeOrder(newOrder)

            // 2. Erase user current shop cart
            repository.clearCart()

            // 3. Initiate background Service update simulator with Explicit Intent
            val serviceIntent = Intent(context, DeliverySimulationService::class.java).apply {
                putExtra("order_id", orderId)
            }
            context.startService(serviceIntent)

            onPlaced(orderId)
        }
    }

    fun submitOrderReview(orderId: String, restaurantName: String, rating: Float, comment: String, tags: String) {
        viewModelScope.launch {
            val newReview = ReviewItem(
                id = "REV-${Random.nextInt(1000, 9999)}",
                restaurantId = "rest-${Random.nextInt(100, 999)}",
                restaurantName = restaurantName,
                rating = rating,
                comment = comment,
                tags = tags,
                timestamp = System.currentTimeMillis()
            )
            repository.submitReview(newReview)
        }
    }

    fun logoutUser() {
        sharedPrefs.edit().remove("logged_in_email").remove("logged_in_name").apply()
        // clear local cart too
        viewModelScope.launch {
            repository.clearCart()
        }
    }

    class Factory(
        private val context: Context,
        private val repository: QuickBiteRepository
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(QuickBiteViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return QuickBiteViewModel(context, repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
