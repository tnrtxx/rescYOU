package com.example.rescyou.utils

import android.content.Context
import android.content.IntentSender
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.Priority

// This class ensures that the device's GPS (location services) is enabled and configured for high accuracy when needed.
// It prompts the user to enable GPS if it's not already enabled.

class TurnOnGps(private val context: Context) {

    // This function starts the process to enable GPS and prompts the user to turn on GPS if necessary
    fun startGps(resultLauncher: ActivityResultLauncher<IntentSenderRequest>) {

        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 3000)
            .setMinUpdateIntervalMillis(3000)
            .setMinUpdateDistanceMeters(5f)
            .build()

        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)

        // Will check if the device's location settings meet the requirements in the LocationSettingsRequest
        val task = LocationServices.getSettingsClient(context).checkLocationSettings(builder.build())

        // This handles the result of checking the location settings
        task.addOnFailureListener {
            // If the settings do not meet the requirements and are fixable...
            if (it is ResolvableApiException) {
                try {
                    // ... create an IntentSenderRequest for the user to confirm the settings change
                    val intentSenderRequest = IntentSenderRequest.Builder(it.resolution).build()
                    // ... launch the IntentSenderRequest using the provided ActivityResultLauncher
                    resultLauncher.launch(intentSenderRequest)
                } catch (exception: IntentSender.SendIntentException) {
                    // This will handle any exceptions that may occur while launching the request
                    Log.e("TurnOnGps", "startGps: ${exception.message}")
                }
            }
        }
    }
}