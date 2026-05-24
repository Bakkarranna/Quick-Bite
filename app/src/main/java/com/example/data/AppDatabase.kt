package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        UserProfile::class,
        CartItem::class,
        ReviewItem::class,
        DeliveryAddress::class,
        OrderHistory::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userProfileDao(): UserProfileDao
    abstract fun cartItemDao(): CartItemDao
    abstract fun reviewItemDao(): ReviewItemDao
    abstract fun deliveryAddressDao(): DeliveryAddressDao
    abstract fun orderHistoryDao(): OrderHistoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "quickbite_database"
                )
                .addCallback(DatabaseCallback(context.applicationContext))
                .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val context: Context
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                // Seed database on creation
                CoroutineScope(Dispatchers.IO).launch {
                    val database = getDatabase(context)
                    
                    // Seed standard addresses
                    database.deliveryAddressDao().insertAddress(
                        DeliveryAddress(label = "Home", detail = "123 Foodie Lane, Tech District, Cityville")
                    )
                    database.deliveryAddressDao().insertAddress(
                        DeliveryAddress(label = "Office", detail = "456 Corporate Plaza, Block C, Faisalabad")
                    )

                    // Seed standard completed orders to look like screen visual history
                    database.orderHistoryDao().insertOrder(
                        OrderHistory(
                            orderId = "QB-1040",
                            restaurantName = "Luigi's Woodfire Pizza",
                            status = "Delivered",
                            totalAmount = 28.00,
                            itemsDescription = "Large Margherita + Garlic Bread",
                            timestamp = System.currentTimeMillis() - 86400000 * 2 // 2 days ago
                        )
                    )
                    database.orderHistoryDao().insertOrder(
                        OrderHistory(
                            orderId = "QB-1038",
                            restaurantName = "The Burger Factory",
                            status = "Delivered",
                            totalAmount = 18.99,
                            itemsDescription = "Double Smash Burger Combo",
                            timestamp = System.currentTimeMillis() - 86400000 * 4 // 4 days ago
                        )
                    )
                    database.orderHistoryDao().insertOrder(
                        OrderHistory(
                            orderId = "QB-1035",
                            restaurantName = "Sushi Master",
                            status = "Cancelled",
                            totalAmount = 24.00,
                            itemsDescription = "Dragon Roll + Miso Soup",
                            timestamp = System.currentTimeMillis() - 86400000 * 6 // 6 days ago
                        )
                    )

                    // Seed standard reviews matching review screen tags
                    database.reviewItemDao().insertReview(
                        ReviewItem(
                            id = "REV-01",
                            restaurantId = "rest-burger-co",
                            restaurantName = "Burger & Co.",
                            rating = 4f,
                            comment = "The cheeseburger was incredibly juicy and came super fast!",
                            tags = "Fast Delivery,Tasty!",
                            timestamp = System.currentTimeMillis() - 3600000 // 1 hr ago
                        )
                    )
                }
            }
        }
    }
}
