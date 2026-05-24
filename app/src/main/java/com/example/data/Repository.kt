package com.example.data

import android.content.Context
import kotlinx.coroutines.flow.Flow

class QuickBiteRepository(private val db: AppDatabase) {

    // User Profile
    fun getUserProfile(email: String): Flow<UserProfile?> = db.userProfileDao().getProfile(email)
    suspend fun saveUserProfile(profile: UserProfile) = db.userProfileDao().insertOrUpdateProfile(profile)
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
    suspend fun placeOrder(order: OrderHistory) = db.orderHistoryDao().insertOrder(order)
    suspend fun updateOrderStatus(orderId: String, status: String) = db.orderHistoryDao().updateOrderStatus(orderId, status)
    suspend fun cancelOrder(orderId: String) = db.orderHistoryDao().updateOrderStatus(orderId, "Cancelled")

    // Address Index
    val allAddresses: Flow<List<DeliveryAddress>> = db.deliveryAddressDao().getAllAddresses()
    suspend fun addAddress(address: DeliveryAddress) = db.deliveryAddressDao().insertAddress(address)
    suspend fun removeAddress(id: Int) = db.deliveryAddressDao().deleteAddress(id)

    // Reviews Schema
    val allReviews: Flow<List<ReviewItem>> = db.reviewItemDao().getAllReviews()
    suspend fun submitReview(review: ReviewItem) = db.reviewItemDao().insertReview(review)
    suspend fun removeReview(id: String) = db.reviewItemDao().deleteReview(id)

    companion object {
        @Volatile
        private var INSTANCE: QuickBiteRepository? = null

        fun getInstance(context: Context): QuickBiteRepository {
            return INSTANCE ?: synchronized(this) {
                val db = AppDatabase.getDatabase(context)
                val instance = QuickBiteRepository(db)
                INSTANCE = instance
                instance
            }
        }
    }
}
