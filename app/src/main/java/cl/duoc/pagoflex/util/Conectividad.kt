package cl.duoc.pagoflex.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class Conectividad(contexto: Context) {

    private val administrador = contexto.applicationContext.getSystemService(ConnectivityManager::class.java)

    private val _enLinea = MutableStateFlow(consultar())
    val enLinea: StateFlow<Boolean> = _enLinea.asStateFlow()

    init {
        runCatching {
            administrador?.registerDefaultNetworkCallback(object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    _enLinea.value = consultar()
                }

                override fun onLost(network: Network) {
                    _enLinea.value = consultar()
                }

                override fun onCapabilitiesChanged(network: Network, capacidades: NetworkCapabilities) {
                    _enLinea.value = capacidades.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                }
            })
        }
    }

    fun hayConexion(): Boolean = consultar().also { _enLinea.value = it }

    private fun consultar(): Boolean {
        val admin = administrador ?: return false
        val red = admin.activeNetwork ?: return false
        val capacidades = admin.getNetworkCapabilities(red) ?: return false
        return capacidades.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}
