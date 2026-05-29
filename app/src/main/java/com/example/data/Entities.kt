package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profiles")
data class UserProfile(
    @PrimaryKey val email: String,
    val name: String,
    val phone: String,
    val profilePhotoUri: String? = null
)

@Entity(tableName = "cart_items")
data class CartItem(
    @PrimaryKey val id: String,
    val name: String,
    val price: Double,
    val quantity: Int,
    val imageUrl: String,
    val category: String = "",
    val notes: String = ""
)

@Entity(tableName = "review_items")
data class ReviewItem(
    @PrimaryKey val id: String,
    val restaurantId: String,
    val restaurantName: String,
    val rating: Float,
    val comment: String,
    val tags: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "delivery_addresses")
data class DeliveryAddress(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val label: String,
    val detail: String
)

@Entity(tableName = "order_history")
data class OrderHistory(
    @PrimaryKey val orderId: String,
    val restaurantName: String,
    val status: String, // "Placed", "Preparing", "On the way", "Delivered", "Cancelled"
    val totalAmount: Double,
    val itemsDescription: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class RestaurantData(
    val id: String,
    val title: String,
    val tags: String,
    val rating: String,
    val price: String,
    val distance: String,
    val time: String,
    val imageUrl: String,
    val categories: List<String>
)

