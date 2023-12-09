package com.example.rescyou

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.RemoteMessage

class SendMessageActivity : AppCompatActivity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_send_message)

        // Replace "your_device_token" with the actual FCM token of the target device/user
        val targetDeviceToken = "AAAA6jJJnJg:APA91bG-D1uEV29YYYCsxUtGQPoNpMUVWTt9V1Nq8q5mGibbF45F7ukPYkKpqgZ34zbW5wcav3GtXN_9zLwydF7U6-i956Sz9aWyBU5MAQYLaYe4MP6TYsvWXcjMKa2T1pqmeOgEtfiD"

        // Send a test FCM message to the specified device token
        sendTestMessage(targetDeviceToken)
    }

    private fun sendTestMessage(targetDeviceToken: String) {
        // Create a data payload for your message
        val data = hashMapOf(
            "message" to "This is a test message"
        )

        // Create a message using the FCM API
        val message = RemoteMessage.Builder(targetDeviceToken)
            .setMessageId("1")
            .setData(data)
            .build()

        // Simulate receiving the message by calling onMessageReceived directly
        onMessageReceived(message)
    }

    fun onMessageReceived(remoteMessage: RemoteMessage) {
        // Handle the incoming message here
        Log.d(TAG, "Received message: ${remoteMessage.data["message"]}")
    }


    companion object {
        private const val TAG = "SendMessageActivity"
    }
}