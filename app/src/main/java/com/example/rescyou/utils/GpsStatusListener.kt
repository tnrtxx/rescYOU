package com.example.rescyou.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.LocationManager
import androidx.lifecycle.LiveData



// LiveData is used to observe changes in GPS status (enabled or disabled)
class GpsStatusListener(private val context: Context) : LiveData<Boolean>() {

    override fun onActive() {
        registerReceiver()   // Register a BroadcastReceiver to listen for GPS status changes
        checkGpsStatus()     // Check the initial GPS status
    }

    override fun onInactive() {
        unRegisterReceiver()  // Unregister the BroadcastReceiver to avoid memory leaks
    }

    // BroadcastReceiver to listen for changes in GPS status
    private val gpsStatusReceiver = object : BroadcastReceiver() {
        override fun onReceive(p0: Context?, p1: Intent?) {
            checkGpsStatus()   // When GPS status changes, re-check and update the LiveData value
        }
    }

    /// Check the current GPS status and update the LiveData accordingly
    private fun checkGpsStatus() {
        if (isLocationEnable()) {
            // If GPS is enabled, set the LiveData value to true
            postValue(true)
        } else {
            // If GPS is disabled, set the LiveData value to false
            postValue(false)
        }
    }

    // Helper function to check if the GPS provider is enabled
    private fun isLocationEnable() = context.getSystemService(LocationManager::class.java)
        .isProviderEnabled(LocationManager.GPS_PROVIDER)

    // Registers the BroadcastReceiver to listen for GPS status changes
    private fun registerReceiver() = context.registerReceiver(gpsStatusReceiver, IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION))

    // Will unregister the BroadcastReceiver when it's no longer needed
    private fun unRegisterReceiver() = context.unregisterReceiver(gpsStatusReceiver)
}