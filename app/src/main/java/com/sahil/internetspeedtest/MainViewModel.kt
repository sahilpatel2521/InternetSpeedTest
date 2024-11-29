package com.sahil.internetspeedtest

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.TrafficStats
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {
    private val _wifiSpeed = MutableLiveData<Float>()
    val wifiSpeed: LiveData<Float> get() = _wifiSpeed

    private val _mobileSpeed = MutableLiveData<Float>()
    val mobileSpeed: LiveData<Float> get() = _mobileSpeed

    private var monitoringJob: Job? = null
    private var previousRxBytes = 0L
    private var previousTxBytes = 0L

    fun startMonitoring(context: Context) {
        monitoringJob = CoroutineScope(Dispatchers.IO).launch {
            while (isActive) {
                val (rxBytes, txBytes) = Pair(TrafficStats.getTotalRxBytes(), TrafficStats.getTotalTxBytes())
                val diffRx = rxBytes - previousRxBytes
                val diffTx = txBytes - previousTxBytes
                previousRxBytes = rxBytes
                previousTxBytes = txBytes

                val speedInMbps = (diffRx + diffTx) * 8 / 1_000_000f / 5 // Convert to Mbps over 5 seconds

                when (getNetworkType(context)) {
                    NetworkType.WIFI -> _wifiSpeed.postValue(speedInMbps)
                    NetworkType.MOBILE -> _mobileSpeed.postValue(speedInMbps)
                    else -> {
                        _wifiSpeed.postValue(0f)
                        _mobileSpeed.postValue(0f)
                    }
                }

                delay(5000) // 5-second interval
            }
        }
    }

    fun stopMonitoring() {
        monitoringJob?.cancel()
    }

    private fun getNetworkType(context: Context): NetworkType {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetwork ?: return NetworkType.NONE
        val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return NetworkType.NONE

        return when {
            networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> NetworkType.WIFI
            networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> NetworkType.MOBILE
            else -> NetworkType.NONE
        }
    }
}

enum class NetworkType {
    WIFI, MOBILE, NONE
}