package com.example.rescyou.utils

import android.app.Application
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConnectivityManager
@Inject
constructor(
    application: Application,
) {
    private val connectionLiveData = ConnectionLiveData(application)

    // observe this in ui
    private val isNetworkAvailable = MutableLiveData<Boolean>().apply { value = false }

    fun registerConnectionObserver(lifecycleOwner: LifecycleOwner){
        connectionLiveData.observe(lifecycleOwner, Observer { isConnected ->
            isConnected?.let { isNetworkAvailable.value = it }
        })
    }
    fun unregisterConnectionObserver(lifecycleOwner: LifecycleOwner){
        connectionLiveData.removeObservers(lifecycleOwner)
    }
}


