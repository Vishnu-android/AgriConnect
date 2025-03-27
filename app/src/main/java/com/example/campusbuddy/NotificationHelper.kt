package com.example.campusbuddy

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging

object NotificationHelper {
    const val CHANNEL_ID = "farm_orders_channel"
    const val ORDERS_COLLECTION = "orders"
    const val NOTIFICATION_TYPE_ORDER = "new_order"

    // Initialize notification channel (call this in your main activity)
    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Order Notifications"
            val description = "Notifications for new orders"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                this.description = description
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    // Store FCM token for current user
    fun registerFCMToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                FirebaseAuth.getInstance().currentUser?.uid?.let { userId ->
                    FirebaseFirestore.getInstance().collection("users")
                        .document(userId)
                        .update("fcmToken", token)
                }
            }
        }
    }

    // Send order notification to farmer
    fun notifyFarmer(farmerId: String, productName: String, context: Context) {
        // 1. Create local notification immediately
        showLocalNotification(context, productName)

        // 2. Save notification to Firestore (for in-app notification badge)
        saveNotificationToFirestore(farmerId, productName)

        // 3. Send FCM push notification (will work even if app is closed)
        sendFCMPushNotification(farmerId, productName)
    }

    private fun showLocalNotification(context: Context, productName: String) {
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("New Order!")
            .setContentText("You received an order for $productName")
            .setSmallIcon(R.drawable.ic_notifications_black_24dp) // Create this icon in drawable
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }

    private fun saveNotificationToFirestore(farmerId: String, productName: String) {
        val notificationData = hashMapOf(
            "type" to NOTIFICATION_TYPE_ORDER,
            "productName" to productName,
            "timestamp" to FieldValue.serverTimestamp(),
            "read" to false
        )

        FirebaseFirestore.getInstance().collection("users")
            .document(farmerId)
            .collection("notifications")
            .add(notificationData)
    }

    private fun sendFCMPushNotification(farmerId: String, productName: String) {
        // Get farmer's FCM token
        FirebaseFirestore.getInstance().collection("users")
            .document(farmerId)
            .get()
            .addOnSuccessListener { document ->
                val token = document.getString("fcmToken")
                token?.let {
                    // In production, you would send this to your backend server
                    // For now, we'll just log it
                    Log.d("NotificationHelper", "Would send FCM to token: $token")
                }
            }
    }
}