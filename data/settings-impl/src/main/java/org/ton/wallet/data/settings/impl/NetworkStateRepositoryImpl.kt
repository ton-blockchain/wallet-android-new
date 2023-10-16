package org.ton.wallet.data.settings.impl

import android.content.*
import android.net.ConnectivityManager
import android.os.Build
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import org.ton.wallet.data.settings.api.NetworkState
import org.ton.wallet.data.settings.api.NetworkStateRepository
import org.ton.wallet.lib.log.L

class NetworkStateRepositoryImpl(
    context: Context
) : NetworkStateRepository {

    private val connectivityManager = context.getSystemService(ConnectivityManager::class.java)

    private val _networkStateFlow = MutableStateFlow(NetworkState(false))
    override val networkStateFlow: Flow<NetworkState> = _networkStateFlow

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {

        override fun onAvailable(network: android.net.Network) {
            super.onAvailable(network)
            _networkStateFlow.tryEmit(NetworkState(true))
        }

        override fun onLost(network: android.net.Network) {
            super.onLost(network)
            _networkStateFlow.tryEmit(NetworkState(false))
        }
    }

    private val networkStateReceiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            _networkStateFlow.tryEmit(NetworkState(isNetworkConnected()))
        }
    }

    init {
        _networkStateFlow.tryEmit(NetworkState(isNetworkConnected()))

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            connectivityManager.registerDefaultNetworkCallback(networkCallback)
        } else {
            context.registerReceiver(networkStateReceiver, IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION))
        }
    }

    override suspend fun deleteWallet() = Unit

    private fun isNetworkConnected(): Boolean {
        try {
            var networkInfo = connectivityManager.activeNetworkInfo
            if (networkInfo != null && (networkInfo.isConnectedOrConnecting || networkInfo.isAvailable)) {
                return true
            }
            networkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)
            if (networkInfo?.isConnectedOrConnecting == true) {
                return true
            }
            networkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
            if (networkInfo?.isConnectedOrConnecting == true) {
                return true
            }
        } catch (e: Exception) {
            L.e(e)
        }
        return false
    }
}