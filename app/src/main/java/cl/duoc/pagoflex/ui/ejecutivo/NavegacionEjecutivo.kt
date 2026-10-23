package cl.duoc.pagoflex.ui.ejecutivo

import androidx.compose.runtime.getValue
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import cl.duoc.pagoflex.data.db.Usuario
import cl.duoc.pagoflex.ui.PagoFlexNavHost
import cl.duoc.pagoflex.ui.componentes.BannerSinConexion
import cl.duoc.pagoflex.ui.irAPestana
import cl.duoc.pagoflex.ui.sesion.SesionViewModel
import cl.duoc.pagoflex.ui.vmDe

private data class PestanaEj(val ruta: String, val titulo: String, val icono: ImageVector)

private val pestanasEj = listOf(
    PestanaEj("ej/compromisos", "Compromisos", Icons.AutoMirrored.Filled.List),
    PestanaEj("ej/registrar", "Registrar", Icons.Filled.Add),
    PestanaEj("ej/cobranza", "Cobranza", Icons.Filled.DateRange)
)

@Composable
fun NavegacionEjecutivo(persona: Usuario, sesion: SesionViewModel) {
    val nav = rememberNavController()
    val idEjecutivo = persona.idUsuario
    val idEmpresa = persona.idEmpresa ?: 0
    val vm = vmDe(EjecutivoViewModel::class.java, "ejecutivo_$idEjecutivo") { EjecutivoViewModel(it, idEmpresa) }
    val registro = vmDe(RegistroViewModel::class.java, "registro_$idEjecutivo") { RegistroViewModel(it, idEjecutivo) }
    val entrada by nav.currentBackStackEntryAsState()
    val ruta = entrada?.destination?.route
    val enLinea by sesion.enLinea.collectAsStateWithLifecycle()
    val ultima by sesion.ultimaActualizacion.collectAsStateWithLifecycle()

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            AnimatedVisibility(
                visible = pestanasEj.any { it.ruta == ruta },
                enter = expandVertically(), exit = shrinkVertically()
            ) {
                NavigationBar {
                    pestanasEj.forEach { p ->
                        NavigationBarItem(
                            selected = ruta == p.ruta,
                            onClick = { nav.irAPestana(p.ruta) },
                            icon = { Icon(p.icono, contentDescription = null) },
                            label = { Text(p.titulo) }
                        )
                    }
                }
            }
        }
    ) { relleno ->
        Column(Modifier.padding(relleno)) {
            BannerSinConexion(!enLinea, ultima)
            PagoFlexNavHost(nav, "ej/compromisos", Modifier.weight(1f)) {
                composable("ej/compromisos") {
                    PantallaCompromisosEjecutivo(vm, alAbrir = { nav.navigate("ej/compromiso/$it") }, alSalir = sesion::cerrarSesion)
                }
                composable("ej/registrar") { PantallaRegistrar(registro) }
                composable("ej/cobranza") { PantallaCobranza(vm) }
                composable("ej/compromiso/{id}", arguments = listOf(navArgument("id") { type = NavType.IntType })) { e ->
                    val id = e.arguments?.getInt("id") ?: 0
                    val detalle = vmDe(DetalleEjecutivoViewModel::class.java, "detalle_ej_${idEjecutivo}_$id") {
                        DetalleEjecutivoViewModel(it, idEjecutivo, idEmpresa, id)
                    }
                    PantallaDetalleEjecutivo(detalle, alVolver = { nav.popBackStack() })
                }
            }
        }
    }
}
