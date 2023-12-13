package com.example.rescyou
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
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
            val notificationType = remoteMessage.data["type"]
                if (notificationType == "sendRequest"){
                    val rescuerName = remoteMessage.data["rescuerName"]
                    val otherUserID = remoteMessage.data["otherUserID"]
                    val pinId = remoteMessage.data["pinId"]

                    sendNotification(remoteMessage.notification!!.body, rescuerName, otherUserID, pinId)
                }else if (notificationType == "declineRequest"){
                    val pinId = remoteMessage.data["pinId"]

                    sendDeclineNotification(remoteMessage.notification!!.body, pinId)

        }else if (notificationType == "acceptRequest"){
                    val pinId = remoteMessage.data["pinId"]

                    sendAcceptNotification(remoteMessage.notification!!.body, pinId)

                }
    }}

    private fun sendNotification(messageBody: String?, rescuerName: String?, otherUserID:String?, pinId: String?) {
        val channelId = "new_notification_channel_id"  // Change this to a new ID
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val intent = Intent(this, DialogActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("rescuerName", rescuerName)
            putExtra("otherUserID", otherUserID)
            putExtra("pinId", pinId)
        }
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.app_logo_2)
            .setContentTitle("Help is on the way...")
            .setContentText(messageBody)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Channel human readable title", NotificationManager.IMPORTANCE_HIGH)
            channel.setSound(defaultSoundUri, AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .build())
            channel.enableVibration(true)
            channel.vibrationPattern = longArrayOf(0, 1000, 500, 1000)
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(0, notificationBuilder.build())
    }

    private fun sendAcceptNotification(
        messageBody: String?,
        pinId: String?,
    ) {
        val channelId = "accept_notification_channel_id"  // Change this to a new ID
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

//        val intent = Intent(this, DialogActivity::class.java).apply {
//            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//            putExtra("pinId", pinId)
//        }
//        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.app_logo_2)
            .setContentTitle("Sending help accepted")
            .setContentText("Your help request was accepted.")
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
//            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Accept Channel", NotificationManager.IMPORTANCE_HIGH)
            channel.setSound(defaultSoundUri, AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .build())
            channel.enableVibration(true)
            channel.vibrationPattern = longArrayOf(0, 1000, 500, 1000)
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(0, notificationBuilder.build())
    }
    private fun sendDeclineNotification(
        messageBody: String?,
        pinId: String?,
    ) {
        val channelId = "decline_notification_channel_id"  // Change this to a new ID
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

//        val intent = Intent(this, DialogActivity::class.java).apply {
//            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//            putExtra("pinId", pinId)
//        }
//        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.app_logo_2)
            .setContentTitle("Sending help declined")
            .setContentText("Your help request was declined.")
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
//            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Decline Channel", NotificationManager.IMPORTANCE_HIGH)
            channel.setSound(defaultSoundUri, AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .build())
            channel.enableVibration(true)
            channel.vibrationPattern = longArrayOf(0, 1000, 500, 1000)
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(0, notificationBuilder.build())
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
