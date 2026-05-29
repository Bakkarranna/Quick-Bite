package com.example.data

import android.content.Context
import android.util.Log
import com.example.service.FirebaseManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class QuickBiteRepository(private val db: AppDatabase, private val appContext: Context) {

    // User Profile
    fun getUserProfile(email: String): Flow<UserProfile?> = db.userProfileDao().getProfile(email)
    
    suspend fun saveUserProfile(profile: UserProfile) {
        db.userProfileDao().insertOrUpdateProfile(profile)
        
        // Sync with Firebase Firestore
        val firestore = FirebaseManager.getFirestore(appContext)
        if (firestore != null) {
            val userMap = hashMapOf(
                "email" to profile.email,
                "name" to profile.name,
                "phone" to profile.phone,
                "profilePhotoUri" to (profile.profilePhotoUri ?: "")
            )
            firestore.collection("user_profiles").document(profile.email)
                .set(userMap)
                .addOnSuccessListener { Log.d("QuickBiteRepo", "Synced profile with Firebase!") }
                .addOnFailureListener { Log.e("QuickBiteRepo", "Firebase profile sync failed", it) }
        }
    }
    
    suspend fun deleteUserProfile(profile: UserProfile) = db.userProfileDao().deleteProfile(profile)

    // Data Cart (E-commerce)
    val cartItems: Flow<List<CartItem>> = db.cartItemDao().getAllCartItems()
    suspend fun addToCart(item: CartItem) = db.cartItemDao().addOrUpdateCartItem(item)
    suspend fun updateCartQuantity(id: String, quantity: Int) {
        if (quantity <= 0) {
            db.cartItemDao().deleteCartItem(id)
        } else {
            db.cartItemDao().updateItemQuantity(id, quantity)
        }
    }
    suspend fun removeFromCart(id: String) = db.cartItemDao().deleteCartItem(id)
    suspend fun clearCart() = db.cartItemDao().clearCart()

    // Order History / Tracking Info
    val allOrders: Flow<List<OrderHistory>> = db.orderHistoryDao().getAllOrders()
    fun getOrderFlow(orderId: String): Flow<OrderHistory?> = db.orderHistoryDao().getOrderByIdFlow(orderId)
    suspend fun getOrder(orderId: String): OrderHistory? = db.orderHistoryDao().getOrderById(orderId)
    
    suspend fun placeOrder(order: OrderHistory) {
        db.orderHistoryDao().insertOrder(order)
        
        // Sync with Firebase Firestore
        val firestore = FirebaseManager.getFirestore(appContext)
        if (firestore != null) {
            val orderMap = hashMapOf(
                "orderId" to order.orderId,
                "restaurantName" to order.restaurantName,
                "status" to order.status,
                "totalAmount" to order.totalAmount,
                "itemsDescription" to order.itemsDescription,
                "timestamp" to order.timestamp
            )
            firestore.collection("orders").document(order.orderId)
                .set(orderMap)
                .addOnSuccessListener { Log.d("QuickBiteRepo", "Synced order with Firebase!") }
        }
    }
    
    suspend fun updateOrderStatus(orderId: String, status: String) {
        db.orderHistoryDao().updateOrderStatus(orderId, status)
        
        // Sync status with Firebase Firestore
        val firestore = FirebaseManager.getFirestore(appContext)
        firestore?.collection("orders")?.document(orderId)?.update("status", status)
    }
    
    suspend fun cancelOrder(orderId: String) {
        db.orderHistoryDao().updateOrderStatus(orderId, "Cancelled")
        
        // Sync status with Firebase Firestore
        val firestore = FirebaseManager.getFirestore(appContext)
        firestore?.collection("orders")?.document(orderId)?.update("status", "Cancelled")
    }

    // Address Index
    val allAddresses: Flow<List<DeliveryAddress>> = db.deliveryAddressDao().getAllAddresses()
    
    suspend fun addAddress(address: DeliveryAddress) {
        db.deliveryAddressDao().insertAddress(address)
        
        // Sync with Firebase Firestore
        val firestore = FirebaseManager.getFirestore(appContext)
        if (firestore != null) {
            val addressMap = hashMapOf(
                "id" to address.id,
                "label" to address.label,
                "detail" to address.detail
            )
            // Use local generated ID
            val docId = if (address.id == 0) System.currentTimeMillis().toString() else address.id.toString()
            firestore.collection("addresses").document(docId).set(addressMap)
        }
    }
    
    suspend fun removeAddress(id: Int) {
        db.deliveryAddressDao().deleteAddress(id)
        
        // Sync with Firebase Firestore
        val firestore = FirebaseManager.getFirestore(appContext)
        firestore?.collection("addresses")?.document(id.toString())?.delete()
    }

    // Reviews Schema
    val allReviews: Flow<List<ReviewItem>> = db.reviewItemDao().getAllReviews()
    
    suspend fun submitReview(review: ReviewItem) {
        db.reviewItemDao().insertReview(review)
        
        // Sync review in Firebase Firestore
        val firestore = FirebaseManager.getFirestore(appContext)
        if (firestore != null) {
            val reviewMap = hashMapOf(
                "id" to review.id,
                "restaurantId" to review.restaurantId,
                "restaurantName" to review.restaurantName,
                "rating" to review.rating,
                "comment" to review.comment,
                "tags" to review.tags,
                "timestamp" to review.timestamp
            )
            firestore.collection("reviews").document(review.id).set(reviewMap)
        }
    }
    
    suspend fun removeReview(id: String) {
        db.reviewItemDao().deleteReview(id)
        
        // Sync in Firebase Firestore
        val firestore = FirebaseManager.getFirestore(appContext)
        firestore?.collection("reviews")?.document(id)?.delete()
    }

    /**
     * Downloads user profile and order history from Firebase Firestore and restores them locally in Room.
     */
    fun syncFromFirebase(email: String) {
        val firestore = FirebaseManager.getFirestore(appContext) ?: return
        val coroutineScope = CoroutineScope(Dispatchers.IO)
        
        // 1. Sync User Profile
        firestore.collection("user_profiles").document(email).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val name = document.getString("name") ?: ""
                    val phone = document.getString("phone") ?: ""
                    val photoUri = document.getString("profilePhotoUri")
                    coroutineScope.launch {
                        db.userProfileDao().insertOrUpdateProfile(UserProfile(email, name, phone, photoUri))
                    }
                }
            }

        // 2. Sync Order History
        firestore.collection("orders").get()
            .addOnSuccessListener { result ->
                coroutineScope.launch {
                    for (document in result) {
                        val orderId = document.getString("orderId") ?: continue
                        val restaurantName = document.getString("restaurantName") ?: ""
                        val status = document.getString("status") ?: "Placed"
                        val totalAmount = document.getDouble("totalAmount") ?: 0.0
                        val itemsDescription = document.getString("itemsDescription") ?: ""
                        val timestamp = document.getLong("timestamp") ?: System.currentTimeMillis()
                        
                        db.orderHistoryDao().insertOrder(
                            OrderHistory(
                                orderId = orderId,
                                restaurantName = restaurantName,
                                status = status,
                                totalAmount = totalAmount,
                                itemsDescription = itemsDescription,
                                timestamp = timestamp
                            )
                        )
                    }
                }
            }
    }

    companion object {
        @Volatile
        private var INSTANCE: QuickBiteRepository? = null

        fun getInstance(context: Context): QuickBiteRepository {
            return INSTANCE ?: synchronized(this) {
                val db = AppDatabase.getDatabase(context.applicationContext)
                val instance = QuickBiteRepository(db, context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }
}
