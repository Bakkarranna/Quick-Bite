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
                .fallbackToDestructiveMigration()
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
                // Seed database synchronously on creation within the active transaction
                try {
                    db.execSQL("INSERT INTO delivery_addresses (label, detail) VALUES ('Home', '123 Foodie Lane, Tech District, Cityville')")
                    db.execSQL("INSERT INTO delivery_addresses (label, detail) VALUES ('Office', '456 Corporate Plaza, Block C, Faisalabad')")

                    val now = System.currentTimeMillis()
                    val ts2DaysAgo = now - 86400000L * 2
                    val ts4DaysAgo = now - 86400000L * 4
                    val ts6DaysAgo = now - 86400000L * 6
                    val ts1HrAgo = now - 3600000L

                    db.execSQL("INSERT INTO order_history (orderId, restaurantName, status, totalAmount, itemsDescription, timestamp) VALUES ('QB-1040', 'Luigi''s Woodfire Pizza', 'Delivered', 28.00, 'Large Margherita + Garlic Bread', $ts2DaysAgo)")
                    db.execSQL("INSERT INTO order_history (orderId, restaurantName, status, totalAmount, itemsDescription, timestamp) VALUES ('QB-1038', 'The Burger Factory', 'Delivered', 18.99, 'Double Smash Burger Combo', $ts4DaysAgo)")
                    db.execSQL("INSERT INTO order_history (orderId, restaurantName, status, totalAmount, itemsDescription, timestamp) VALUES ('QB-1035', 'Sushi Master', 'Cancelled', 24.00, 'Dragon Roll + Miso Soup', $ts6DaysAgo)")

                    db.execSQL("INSERT INTO review_items (id, restaurantId, restaurantName, rating, comment, tags, timestamp) VALUES ('REV-01', 'rest-burger-co', 'Burger & Co.', 4.0, 'The cheeseburger was incredibly juicy and came super fast!', 'Fast Delivery,Tasty!', $ts1HrAgo)")
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
}
