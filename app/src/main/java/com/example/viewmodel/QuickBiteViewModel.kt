package com.example.viewmodel

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.*
import com.example.service.DeliverySimulationService
import com.example.service.GeminiService
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.random.Random

private val seededRestaurantsList = listOf(
    RestaurantData(
        id = "naseeb-biryani",
        title = "Naseeb Biryani Center",
        tags = "Pakistani • Biryani • Desi",
        rating = "4.8",
        price = "Rs. 99",
        distance = "1.2 km",
        time = "35-45 min",
        imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuCDR2sEJYHUbyL7JGFr4hrczv9zJKvyDHR88AmKAuPGDO4yBi_V3kH38PylG5OmPvAeFlvqVfD4D_VlpU_eDODrtFNETnx6jIaLuVTob1tTUMFNl10-g1LPd1KtH7hTiGE2L8GNo46ZMz6A9YvU04NH3eio5o1tdYxh8i6F1y_vWd5jCWmAzIud9JIux5XPEefhCyG3Eusq1-mrNKsgmHw8ON49fcmQQ9ZvmsKZKCnTN6tqNhHb0KvPKewnvFZzkcOT4z4fbqFWFnM",
        categories = listOf("Biryani", "Desi")
    ),
    RestaurantData(
        id = "spice-grill",
        title = "Spice Grill & Biryani",
        tags = "Pakistani • BBQ • Grill",
        rating = "4.8",
        price = "Min. Rs. 500",
        distance = "2.5 km",
        time = "30-45 min",
        imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuBlQEY9aRHy4z7ZODjXo_8x684HC4_9ju4c3fqsBzOKDQ99G6s0cskqUf43pFflaLkZokLJ_TUn2rafpgZ9xVHoLpp8pIaGwJjt1L6pAaGj0wYHNvpHQywg3rPFSaQ35HWC40t1pAAc8do7s2tOkSu6L1pfCbRT-ezNp11av_VusNW6QwuxmyZCILVaJzrEa1EVF0AyYzBMh-L2IpoNWY1TFSEXUYRWVOnQFljooKKaZMLegjGMR1zlzQSexebU_hd4EnC060A_aDo",
        categories = listOf("Biryani", "BBQ", "Desi")
    ),
    RestaurantData(
        id = "burger-co",
        title = "Burger & Co.",
        tags = "Burgers • Fast Food • American • Crisp Fries",
        rating = "4.6",
        price = "Min. Rs. 350",
        distance = "3.1 km",
        time = "25-35 min",
        imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuB9KVdx714jtz-S5J2wCNac9F573m8IkmeUaQdxmksy4X7FL3Nv_XXveydQFNk3_K_KZ_sLDxc8WQdTGO4ltqh447grblcghXTJy3YoyzTRMA1RKGe_sobrHd4vtQ6zQMsyCVaxjpt6D3ldFS62ea4Xe8R3TexLE7UEG5q8k1wihM6ooWu8XJnZZ_eyQWeOyIL3RD8lp1Kncebr99vzk_0cfmkd3OsgIWzQWKnf3VLj8Q-566-QQ_AtQs9uSUsB09PtwS1Jgdsf3Ng",
        categories = listOf("Burgers", "Desserts")
    ),
    RestaurantData(
        id = "luigis-pizza",
        title = "Luigi's Woodfire Pizza",
        tags = "Pizza • Italian • Cheesy • Garlic",
        rating = "4.7",
        price = "Min. Rs. 600",
        distance = "4.0 km",
        time = "35-50 min",
        imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuA65KNguaZK-LgcyGoTJYDtV9zjN2VtLIdi6673a9CtmpTJRn1kyuZIhS2OzJGvFf66fh_BYghVg5fHrMh3b5pE0EYfcAf031xAbpA8iwoKlgI2DKTfrVKhSVqi5HbFqO18TGOpy5NsBFGHgQMmDNu6FhBpcyfp7Cz1oi0_Ew0tve1iy5OPYpkmgSWwj8PFF8M3MGD3XqiqDMNMZf0cAGEea4rbkSbVWNB9L0bIyIh63x140kHGLJyik9NevJBsT-srbMr7yojU2ds",
        categories = listOf("Pizza", "Desserts")
    ),
    RestaurantData(
        id = "canal-view-chinese",
        title = "Canal View Chinese",
        tags = "Chinese • Noodles • Soups",
        rating = "4.5",
        price = "Min. Rs. 400",
        distance = "1.8 km",
        time = "20-30 min",
        imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuCDR2sEJYHUbyL7JGFr4hrczv9zJKvyDHR88AmKAuPGDO4yBi_V3kH38PylG5OmPvAeFlvqVfD4D_VlpU_eDODrtFNETnx6jIaLuVTob1tTUMFNl10-g1LPd1KtH7hTiGE2L8GNo46ZMz6A9YvU04NH3eio5o1tdYxh8i6F1y_vWd5jCWmAzIud9JIux5XPEefhCyG3Eusq1-mrNKsgmHw8ON49fcmQQ9ZvmsKZKCnTN6tqNhHb0KvPKewnvFZzkcOT4z4fbqFWFnM",
        categories = listOf("Chinese", "Desi")
    ),
    RestaurantData(
        id = "sweet-tooth",
        title = "Sweet Tooth Cakes & Desserts",
        tags = "Bakery • Cakes • Ice Cream • Donuts",
        rating = "4.9",
        price = "Min. Rs. 200",
        distance = "0.8 km",
        time = "15-25 min",
        imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuDBrL6c9xuYqNackMqOuSTfMweK1PbSBU-VKsAx7o7y9D6YFS5pdLl_GQadf_e-Fl942Q4QlCTF4kozIMBCJymaUVWt0VVCB-P3QfMxPppzvZs0D1fxaDBe6JWn3-ANmTeIHOb_BoD92S_JI7BLiQkNgfIKRXeNjbRsIGtoImUvzAFlnZG4QJQPTKyBrmrfKltQ1HyFhjXK9VGOdNKyajJvSkgmbEWQhzqH8wXsODN1Sx3A35X5XmLtjml0wV6X0TEVr6FJkaMVlec",
        categories = listOf("Desserts", "Drinks")
    ),
    RestaurantData(
        id = "kababish",
        title = "Kababish Tandoori",
        tags = "Traditional Desi • BBQ • Handi",
        rating = "4.7",
        price = "Min. Rs. 450",
        distance = "2.9 km",
        time = "25-35 min",
        imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuBlQEY9aRHy4z7ZODjXo_8x684HC4_9ju4c3fqsBzOKDQ99G6s0cskqUf43pFflaLkZokLJ_TUn2rafpgZ9xVHoLpp8pIaGwJjt1L6pAaGj0wYHNvpHQywg3rPFSaQ35HWC40t1pAAc8do7s2tOkSu6L1pfCbRT-ezNp11av_VusNW6QwuxmyZCILVaJzrEa1EVF0AyYzBMh-L2IpoNWY1TFSEXUYRWVOnQFljooKKaZMLegjGMR1zlzQSexebU_hd4EnC060A_aDo",
        categories = listOf("BBQ", "Desi")
    ),
    RestaurantData(
        id = "shake-bar",
        title = "The Golden Fruit Shake Bar",
        tags = "Fresh Juices • Thick Shakes • Smoothies",
        rating = "4.4",
        price = "Rs. 150",
        distance = "1.0 km",
        time = "10-20 min",
        imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuDBrL6c9xuYqNackMqOuSTfMweK1PbSBU-VKsAx7o7y9D6YFS5pdLl_GQadf_e-Fl942Q4QlCTF4kozIMBCJymaUVWt0VVCB-P3QfMxPppzvZs0D1fxaDBe6JWn3-ANmTeIHOb_BoD92S_JI7BLiQkNgfIKRXeNjbRsIGtoImUvzAFlnZG4QJQPTKyBrmrfKltQ1HyFhjXK9VGOdNKyajJvSkgmbEWQhzqH8wXsODN1Sx3A35X5XmLtjml0wV6X0TEVr6FJkaMVlec",
        categories = listOf("Drinks", "Desserts")
    ),
    RestaurantData(
        id = "faisalabad-steaks",
        title = "Faisalabad Gourmet Steakhouse",
        tags = "Steaks • Sizzling • Continental • Grill",
        rating = "4.8",
        price = "Min. Rs. 1200",
        distance = "5.2 km",
        time = "40-50 min",
        imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuB9KVdx714jtz-S5J2wCNac9F573m8IkmeUaQdxmksy4X7FL3Nv_XXveydQFNk3_K_KZ_sLDxc8WQdTGO4ltqh447grblcghXTJy3YoyzTRMA1RKGe_sobrHd4vtQ6zQMsyCVaxjpt6D3ldFS62ea4Xe8R3TexLE7UEG5q8k1wihM6ooWu8XJnZZ_eyQWeOyIL3RD8lp1Kncebr99vzk_0cfmkd3OsgIWzQWKnf3VLj8Q-566-QQ_AtQs9uSUsB09PtwS1Jgdsf3Ng",
        categories = listOf("Burgers", "BBQ")
    )
)

class QuickBiteViewModel(
    private val context: Context,
    private val repository: QuickBiteRepository
) : ViewModel() {

    private val sharedPrefs = context.getSharedPreferences("quickbite_prefs", Context.MODE_PRIVATE)

    // Reactive list of restaurant listings
    val restaurantsList = MutableStateFlow<List<RestaurantData>>(seededRestaurantsList)

    fun addRestaurant(newRest: RestaurantData) {
        restaurantsList.value = restaurantsList.value + newRest
    }

    // Dark Mode Theme Preference
    var isDarkMode = MutableStateFlow(sharedPrefs.getBoolean("dark_mode", false))
        private set

    // Active promo code applied from dashboard
    val activePromoCode = MutableStateFlow("")

    // Active logged in user
    val activeUserEmail = run {
        val email = sharedPrefs.getString("logged_in_email", "ahmad.khan@example.com") ?: "ahmad.khan@example.com"
        if (email.contains("bakarrkhann") || email.contains("bakar")) {
            "ahmad.khan@example.com"
        } else {
            email
        }
    }
    
    init {
        val currentEmail = sharedPrefs.getString("logged_in_email", "") ?: ""
        val currentName = sharedPrefs.getString("logged_in_name", "") ?: ""
        if (currentEmail.contains("bakarrkhann") || currentEmail.contains("bakar") || currentName.contains("Bakar")) {
            sharedPrefs.edit()
                .putString("logged_in_email", "ahmad.khan@example.com")
                .putString("logged_in_name", "Ahmad Khan")
                .apply()
        }
        viewModelScope.launch {
            repository.saveUserProfile(
                UserProfile(
                    email = "ahmad.khan@example.com",
                    name = "Ahmad Khan",
                    phone = "+92 321 1234567"
                )
            )
        }
    }
    
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

    // AI Craving recommendation states
    var aiRecommendation = MutableStateFlow<String?>(null)
        private set
    var isAiLoading = MutableStateFlow(false)
        private set

    fun getAiRecommendation(prompt: String) {
        viewModelScope.launch {
            isAiLoading.value = true
            aiRecommendation.value = null
            try {
                val recommendation = GeminiService.recommendCravings(prompt)
                aiRecommendation.value = recommendation
            } catch (e: Exception) {
                aiRecommendation.value = "Failed to load AI suggestions. Please try again."
            } finally {
                isAiLoading.value = false
            }
        }
    }

    fun clearAiRecommendation() {
        aiRecommendation.value = null
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

    fun syncFromFirebase(email: String) {
        viewModelScope.launch {
            repository.syncFromFirebase(email)
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
