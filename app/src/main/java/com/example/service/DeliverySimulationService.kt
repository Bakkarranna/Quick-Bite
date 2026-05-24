package com.example.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.data.QuickBiteRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class DeliverySimulationService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.IO)
    private lateinit var repository: QuickBiteRepository
    private val CHANNEL_ID = "delivery_updates"

    override fun onCreate() {
        super.onCreate()
        repository = QuickBiteRepository.getInstance(applicationContext)
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val orderId = intent?.getStringExtra("order_id") ?: return START_NOT_STICKY

        // Start order status progression loop
        serviceScope.launch {
            simulateOrderProgression(orderId)
        }

        return START_NOT_STICKY
    }

    private suspend fun simulateOrderProgression(orderId: String) {
        // Init Placed State
        repository.updateOrderStatus(orderId, "Placed")
        sendNotification("Order Confirmed", "Your order #$orderId has been placed!", 1001)
        delay(12000) // 12 seconds in Placed

        // Move to Preparing
        repository.updateOrderStatus(orderId, "Preparing")
        sendNotification("Food is Preparing", "Ahmed, your food is being freshly cooked!", 1002)
        delay(15000) // 15 seconds in Preparing

        // Move to On the way
        repository.updateOrderStatus(orderId, "On the way")
        sendNotification("Rider is On the Way", "Usman is speeding towards your delivery address!", 1003)
        delay(18000) // 18 seconds on the way

        // Move to Delivered
        repository.updateOrderStatus(orderId, "Delivered")
        sendNotification("Order Delivered", "Your meals from QuickBite are delivered! Enjoy!", 1004)

        // Stop our simulation service
        stopSelf()
    }

    private fun sendNotification(title: String, body: String, notificationId: Int) {
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(notificationId, builder.build())
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "QuickBite Delivery Channel"
            val descriptionText = "Real-time updates regarding food prep and deliveries"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
