package com.example.rescyou.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.lifecycle.LiveData

// LiveData is used to observe changes in network connectivity
class NetworkManager(context: Context) : LiveData<Boolean>() {
    override fun onActive() {
        super.onActive()
        checkNetworkConnectivity()
    }
    override fun onInactive() {
        super.onInactive()
        connectivityManager.unregisterNetworkCallback(networkCallback)
    }

    // Reference to the ConnectivityManager service
    private var connectivityManager =
        context.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    // Network callback that will listen to network state changes
    private val networkCallback = object : ConnectivityManager.NetworkCallback() {

        // Will be called when a network connection becomes available
        override fun onAvailable(network: Network) {
            super.onAvailable(network)
            postValue(true)   // LiveData value indicating that network connectivity is available
        }

        // Will be called when a network connection becomes available
        override fun onUnavailable() {
            super.onUnavailable()
            postValue(false)   // LiveData value indicating that network connectivity is available
        }

        // Will be called when a network connection is lost
        override fun onLost(network: Network) {
            super.onLost(network)
            postValue(false)   // LiveData value indicating that network connectivity is lost
        }
    }

    // Will check network connectivity and register a network callback
    private fun checkNetworkConnectivity() {
        val network = connectivityManager.activeNetwork

        // If there is no active network, this will post a LiveData value indicating no connectivity
        if (network == null) postValue(false)

        // This defines a network request with desired capabilities and transport types
        val requestBuilder = NetworkRequest.Builder().apply {
            addCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
            addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
            addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            addTransportType(NetworkCapabilities.TRANSPORT_ETHERNET)
        }.build()

        // Will register a network callback to listen for network state changes
        connectivityManager.registerNetworkCallback(requestBuilder, networkCallback)
    }
}


