package com.example.rescyou.utils

import android.content.ContentValues.TAG
import android.util.Log
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Socket

private const val TAG = "DoesNetworkHaveInternet"
object DoesNetworkHaveInternet {
    fun execute(): Boolean {
        return try {
            Log.d(TAG, "PINGING google.")
            val socket = Socket()
            socket.connect(InetSocketAddress("8.8.8.8", 53), 1500)
            socket.close()
            Log.d(TAG, "PING success.")
            true
        } catch (e: IOException) {
            Log.d(TAG, "No internet connection. $e")
            false
        }

    }
}