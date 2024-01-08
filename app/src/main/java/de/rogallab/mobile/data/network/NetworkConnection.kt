package de.rogallab.mobile.data.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import javax.inject.Inject

class NetworkConnection @Inject constructor(
   context: Context
) {
   private val connectivityManager =
      context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

   fun isWiFiOnline(): Boolean {
      var result = false
      val network = connectivityManager.activeNetwork
      connectivityManager.getNetworkCapabilities(network)?.let {
         result = it.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
      }
      return result
   }

   fun isCellularOnline(): Boolean {
      var result = false
      val network = connectivityManager.activeNetwork
      connectivityManager.getNetworkCapabilities(network)?.let {
         result = it.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
      }
      return result
   }
}