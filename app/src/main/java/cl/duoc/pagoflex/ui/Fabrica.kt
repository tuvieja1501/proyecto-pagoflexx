package cl.duoc.pagoflex.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.pagoflex.AppContainer
import cl.duoc.pagoflex.PagoFlexApp

@Composable
fun contenedorActual(): AppContainer =
    (LocalContext.current.applicationContext as PagoFlexApp).contenedor

@Composable
fun <T : ViewModel> rememberFabrica(clave: String, crear: (AppContainer) -> T): FabricaVm<T> {
    val contenedor = contenedorActual()
    return remember(clave) { FabricaVm(clave, contenedor, crear) }
}

class FabricaVm<T : ViewModel>(
    val clave: String,
    private val contenedor: AppContainer,
    private val crear: (AppContainer) -> T
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <V : ViewModel> create(modelClass: Class<V>): V = crear(contenedor) as V
}

@Composable
fun <T : ViewModel> vmDe(clase: Class<T>, clave: String, crear: (AppContainer) -> T): T {
    val fabrica = rememberFabrica(clave, crear)
    return viewModel(modelClass = clase, key = fabrica.clave, factory = fabrica)
}
