package cl.duoc.pagoflex.ui.agente

import androidx.compose.runtime.getValue
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import cl.duoc.pagoflex.data.db.Usuario
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.duoc.pagoflex.ui.PagoFlexNavHost
import cl.duoc.pagoflex.ui.componentes.BannerSinConexion
import cl.duoc.pagoflex.ui.sesion.SesionViewModel
import cl.duoc.pagoflex.ui.vmDe

@Composable
fun NavegacionAgente(persona: Usuario, sesion: SesionViewModel) {
    val nav = rememberNavController()
    val idAgente = persona.idUsuario
    val bandeja = vmDe(BandejaViewModel::class.java, "bandeja_$idAgente") { BandejaViewModel(it) }
    val enLinea by sesion.enLinea.collectAsStateWithLifecycle()
    val ultima by sesion.ultimaActualizacion.collectAsStateWithLifecycle()
    Column(Modifier.fillMaxSize()) {
        BannerSinConexion(!enLinea, ultima)
        PagoFlexNavHost(nav, "ag/reportes", Modifier.weight(1f)) {
            composable("ag/reportes") {
                PantallaBandeja(bandeja, alAbrir = { nav.navigate("ag/reporte/$it") }, alSalir = sesion::cerrarSesion)
            }
            composable("ag/reporte/{id}", arguments = listOf(navArgument("id") { type = NavType.IntType })) { e ->
                val id = e.arguments?.getInt("id") ?: 0
                val vm = vmDe(ReporteAgenteViewModel::class.java, "reporte_ag_${idAgente}_$id") {
                    ReporteAgenteViewModel(it, idAgente, id)
                }
                PantallaReporteAgente(vm, idAgente, alVolver = { nav.popBackStack() })
            }
        }
    }
}
