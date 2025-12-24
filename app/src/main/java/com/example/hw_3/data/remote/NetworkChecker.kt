package com.example.hw_3.data.remote

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build

// Утилита для проверки наличия интернета
class NetworkChecker(private val context: Context) {
    fun isInternetAvailable(): Boolean {
        return try {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val network = connectivityManager.activeNetwork
                if (network == null) {
                    android.util.Log.d("NetworkChecker", "No active network")
                    return false
                }
                val capabilities = connectivityManager.getNetworkCapabilities(network)
                if (capabilities == null) {
                    android.util.Log.d("NetworkChecker", "No network capabilities")
                    return false
                }
                // Проверяем, что есть интернет и он валидирован
                val hasInternet = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                        capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
                android.util.Log.d("NetworkChecker", "Internet available: $hasInternet (hasInternet=${capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)}, validated=${capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)})")
                hasInternet
            } else {
                @Suppress("DEPRECATION")
                val networkInfo = connectivityManager.activeNetworkInfo
                val isConnected = networkInfo?.isConnected == true && networkInfo.isAvailable
                android.util.Log.d("NetworkChecker", "Internet available (deprecated): $isConnected")
                isConnected
            }
        } catch (e: Exception) {
            android.util.Log.e("NetworkChecker", "Error checking internet: ${e.message}", e)
            false
        }
    }
}

