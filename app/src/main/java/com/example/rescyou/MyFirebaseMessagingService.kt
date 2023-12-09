package com.example.rescyou
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

private var otherUser: UserModel? = null

class MyFirebaseMessagingService: FirebaseMessagingService() {


    override fun onNewToken(token: String) {
        // Called when a new token is generated or the existing one is refreshed.
        Log.d(TAG, "Refreshed token: $token")

        // You can send the token to your server or store it locally.
        sendRegistrationToServer(token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        if (remoteMessage.data.isNotEmpty()) {
            val rescuerName = remoteMessage.data["rescuerName"]
            val pinId = remoteMessage.data["pinId"]

            sendNotification(remoteMessage.notification!!.body, rescuerName, pinId)
        }
    }

    private fun sendNotification(messageBody: String?, rescuerName: String?, pinId: String?) {
        val channelId = "default_notification_channel_id"
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

//        // Get the current user
//        val sharedPreferences = getSharedPreferences("MySharedPref", MODE_PRIVATE)
//        val rescuerName = sharedPreferences.getString("rescuerName", null)

        // Create an explicit intent for an Activity in your app
        val intent = Intent(this, DialogActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("rescuerName", rescuerName)
            putExtra("pinId", pinId)// Pass the rescuerName as an extra
        }
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.app_logo_2)  // Set your app's icon
            .setContentTitle("Help is on the way...")
            .setContentText(messageBody)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)  // Set the intent that will fire when the user taps the notification

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Channel human readable title", NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build())
    }

    private fun sendRegistrationToServer(token: String) {
        // Implement your logic to send the FCM token to your server.
        // This is where you can associate the token with a user ID on your server.
        // You might make a network request to your server to store the token.
    }

    companion object {
        private const val TAG = "MyFirebaseMsgService"
    }

}

