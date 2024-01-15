package de.rogallab.mobile.data.devices.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import de.rogallab.mobile.data.devices.IConnectivityObserver
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class NetworkConnectivityObserver(
   private val context: Context
): IConnectivityObserver {

   private val connectivityManager =
      context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

   override fun observe(): Flow<IConnectivityObserver.Status> {

      return callbackFlow {

         val callback = object : ConnectivityManager.NetworkCallback() {

            override fun onAvailable(network: Network) {
               super.onAvailable(network)
               launch { send(IConnectivityObserver.Status.Available) }
            }

            override fun onLosing(network: Network, maxMsToLive: Int) {
               super.onLosing(network, maxMsToLive)
               launch { send(IConnectivityObserver.Status.Losing) }
            }

            override fun onLost(network: Network) {
               super.onLost(network)
               launch { send(IConnectivityObserver.Status.Lost) }
            }

            override fun onUnavailable() {
               super.onUnavailable()
               launch { send(IConnectivityObserver.Status.Unavailable) }
            }
         }

         connectivityManager.registerDefaultNetworkCallback(callback)

         awaitClose {
            connectivityManager.unregisterNetworkCallback(callback)
         }
      }.distinctUntilChanged()
   }
}

/*

https://www.youtube.com/watch?v=TzV0oCRDNfM

https://github.com/philipplackner/ObserveConnectivity

class MainActivity : ComponentActivity() {

    private lateinit var connectivityObserver: ConnectivityObserver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        connectivityObserver = NetworkConnectivityObserver(applicationContext)
        setContent {
            ObserveConnectivityTheme {
                val status by connectivityObserver.observe().collectAsState(
                    initial = ConnectivityObserver.Status.Unavailable
                )
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "Network status: $status")
                }
            }
        }
    }
}
 */