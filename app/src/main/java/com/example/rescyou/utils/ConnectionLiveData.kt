package com.example.rescyou.utils

import android.content.ContentValues.TAG
import android.content.Context
import android.content.Context.CONNECTIVITY_SERVICE
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET
import android.net.NetworkRequest
import android.util.Log
import androidx.lifecycle.LiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val TAG = "ConnectionLiveData"
class ConnectionLiveData(context: Context) : LiveData<Boolean>() {

    private lateinit var networkCallback: ConnectivityManager.NetworkCallback
    private val connectivityManager =
        context.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
    private val validNetworks: MutableSet<Network> = HashSet()

    private fun checkValidNetworks() {
        postValue(validNetworks.size > 0)
    }

    override fun onActive() {
        networkCallback = createNetworkCallback()
        val networkRequest = NetworkRequest.Builder().addCapability(NET_CAPABILITY_INTERNET).build()
        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
    }

    override fun onInactive() {
        connectivityManager.unregisterNetworkCallback(networkCallback)
    }

    private fun createNetworkCallback() = object : ConnectivityManager.NetworkCallback() {

        override fun onAvailable(network: Network) {
            Log.d(TAG, "onAvailable: $network")
            val networkCapabilities = connectivityManager.getNetworkCapabilities(network)
            val hasInternetCapabilities =
                networkCapabilities?.hasCapability(NET_CAPABILITY_INTERNET)
            Log.d(TAG, "onAvailable: $network, $hasInternetCapabilities")
            if (hasInternetCapabilities == true) {
                CoroutineScope(IO).launch {
                    val hasInternet = DoesNetworkHaveInternet.execute()
                    if (hasInternet) {
                        withContext(Main) {
                            Log.d(TAG, "onAvailable: adding network. $network")
                            validNetworks.add(network)
                            checkValidNetworks()
                        }
                    }
                }
                validNetworks.add(network)
            }
            checkValidNetworks()
        }

        override fun onLost(network: Network) {
            Log.d(TAG, "onLost: $network")
            validNetworks.remove(network)
            checkValidNetworks()
        }
    }


}