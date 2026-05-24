package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProfileDao {
    @Query("SELECT * FROM user_profiles WHERE email = :email LIMIT 1")
    fun getProfile(email: String): Flow<UserProfile?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProfile(profile: UserProfile)

    @Delete
    suspend fun deleteProfile(profile: UserProfile)
}

@Dao
interface CartItemDao {
    @Query("SELECT * FROM cart_items")
    fun getAllCartItems(): Flow<List<CartItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addOrUpdateCartItem(item: CartItem)

    @Query("UPDATE cart_items SET quantity = :quantity WHERE id = :id")
    suspend fun updateItemQuantity(id: String, quantity: Int)

    @Query("DELETE FROM cart_items WHERE id = :id")
    suspend fun deleteCartItem(id: String)

    @Query("DELETE FROM cart_items")
    suspend fun clearCart()
}

@Dao
interface ReviewItemDao {
    @Query("SELECT * FROM review_items ORDER BY timestamp DESC")
    fun getAllReviews(): Flow<List<ReviewItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReview(review: ReviewItem)

    @Query("DELETE FROM review_items WHERE id = :id")
    suspend fun deleteReview(id: String)
}

@Dao
interface DeliveryAddressDao {
    @Query("SELECT * FROM delivery_addresses")
    fun getAllAddresses(): Flow<List<DeliveryAddress>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAddress(address: DeliveryAddress)

    @Query("DELETE FROM delivery_addresses WHERE id = :id")
    suspend fun deleteAddress(id: Int)
}

@Dao
interface OrderHistoryDao {
    @Query("SELECT * FROM order_history ORDER BY timestamp DESC")
    fun getAllOrders(): Flow<List<OrderHistory>>

    @Query("SELECT * FROM order_history WHERE orderId = :orderId LIMIT 1")
    fun getOrderByIdFlow(orderId: String): Flow<OrderHistory?>

    @Query("SELECT * FROM order_history WHERE orderId = :orderId LIMIT 1")
    suspend fun getOrderById(orderId: String): OrderHistory?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: OrderHistory)

    @Query("UPDATE order_history SET status = :status WHERE orderId = :orderId")
    suspend fun updateOrderStatus(orderId: String, status: String)

    @Query("DELETE FROM order_history WHERE orderId = :orderId")
    suspend fun deleteOrder(orderId: String)
}
