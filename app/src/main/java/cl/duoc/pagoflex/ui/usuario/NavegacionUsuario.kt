package cl.duoc.pagoflex.ui.usuario

import androidx.compose.runtime.getValue
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
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

private object Rutas {
    const val INICIO = "uf/inicio"
    const val HISTORIAL = "uf/historial"
    const val CUMPLIMIENTO = "uf/cumplimiento"
    const val REPORTES = "uf/reportes"
    const val AVISOS = "uf/avisos"
    const val COMPROMISO = "uf/compromiso/{id}"
    const val PAGAR = "uf/pagar/{id}"
    const val COMPROBANTE = "uf/comprobante/{id}"
    const val REPORTAR = "uf/reportar/{id}"
    fun compromiso(id: Int) = "uf/compromiso/$id"
    fun pagar(id: Int) = "uf/pagar/$id"
    fun comprobante(id: Int) = "uf/comprobante/$id"
    fun reportar(id: Int) = "uf/reportar/$id"
}

private data class Pestana(val ruta: String, val titulo: String, val icono: ImageVector)

private val pestanas = listOf(
    Pestana(Rutas.INICIO, "Inicio", Icons.Filled.Home),
    Pestana(Rutas.HISTORIAL, "Historial", Icons.AutoMirrored.Filled.List),
    Pestana(Rutas.CUMPLIMIENTO, "Cumplimiento", Icons.Filled.CheckCircle),
    Pestana(Rutas.REPORTES, "Reportes", Icons.Filled.Info)
)

private val argId = listOf(navArgument("id") { type = NavType.IntType })

@Composable
fun NavegacionUsuario(persona: Usuario, sesion: SesionViewModel) {
    val nav = rememberNavController()
    val idUsuario = persona.idUsuario
    val vm = vmDe(UsuarioViewModel::class.java, "usuario_$idUsuario") { UsuarioViewModel(it, idUsuario) }
    val entrada by nav.currentBackStackEntryAsState()
    val ruta = entrada?.destination?.route
    val enLinea by sesion.enLinea.collectAsStateWithLifecycle()
    val ultima by sesion.ultimaActualizacion.collectAsStateWithLifecycle()

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            AnimatedVisibility(
                visible = pestanas.any { it.ruta == ruta },
                enter = expandVertically(), exit = shrinkVertically()
            ) {
                NavigationBar {
                    pestanas.forEach { p ->
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
            PagoFlexNavHost(nav, Rutas.INICIO, Modifier.weight(1f)) {
                composable(Rutas.INICIO) {
                    PantallaInicio(
                        vm,
                        alAbrir = { nav.navigate(Rutas.compromiso(it)) },
                        alAvisos = { nav.navigate(Rutas.AVISOS) },
                        alSalir = sesion::cerrarSesion
                    )
                }
                composable(Rutas.HISTORIAL) {
                    PantallaHistorial(
                        vm,
                        alAbrirComprobante = { nav.navigate(Rutas.comprobante(it)) },
                        alAbrirCompromiso = { nav.navigate(Rutas.compromiso(it)) }
                    )
                }
                composable(Rutas.CUMPLIMIENTO) { PantallaCumplimiento(vm) }
                composable(Rutas.REPORTES) {
                    PantallaMisReportes(vm, alAbrirCompromiso = { nav.navigate(Rutas.compromiso(it)) })
                }
                composable(Rutas.AVISOS) { PantallaAvisos(vm, alVolver = { nav.popBackStack() }) }
                composable(Rutas.COMPROMISO, arguments = argId) { e ->
                    val id = e.arguments?.getInt("id") ?: 0
                    val detalle = vmDe(DetalleCompromisoViewModel::class.java, "detalle_${idUsuario}_$id") {
                        DetalleCompromisoViewModel(it, idUsuario, id)
                    }
                    PantallaDetalle(
                        detalle,
                        alPagar = { nav.navigate(Rutas.pagar(id)) },
                        alReportar = { nav.navigate(Rutas.reportar(id)) },
                        alComprobante = { nav.navigate(Rutas.comprobante(id)) },
                        alVolver = { nav.popBackStack() }
                    )
                }
                composable(Rutas.PAGAR, arguments = argId) { e ->
                    val id = e.arguments?.getInt("id") ?: 0
                    val pago = vmDe(PagoViewModel::class.java, "pago_${idUsuario}_$id") { PagoViewModel(it, idUsuario, id) }
                    PantallaPagar(
                        pago,
                        alPagado = {
                            nav.navigate(Rutas.comprobante(id)) { popUpTo(Rutas.PAGAR) { inclusive = true } }
                        },
                        alVolver = { nav.popBackStack() }
                    )
                }
                composable(Rutas.COMPROBANTE, arguments = argId) { e ->
                    val id = e.arguments?.getInt("id") ?: 0
                    val comprobante = vmDe(ComprobanteViewModel::class.java, "comprobante_${idUsuario}_$id") {
                        ComprobanteViewModel(it, idUsuario, id)
                    }
                    PantallaComprobante(
                        comprobante,
                        alInicio = { nav.popBackStack(Rutas.INICIO, inclusive = false) },
                        alVolver = { nav.popBackStack() }
                    )
                }
                composable(Rutas.REPORTAR, arguments = argId) { e ->
                    val id = e.arguments?.getInt("id") ?: 0
                    val reporte = vmDe(ReporteViewModel::class.java, "reporte_${idUsuario}_$id") {
                        ReporteViewModel(it, idUsuario, id)
                    }
                    PantallaReportar(
                        reporte,
                        alTerminar = { nav.popBackStack(Rutas.INICIO, inclusive = false) },
                        alVolver = { nav.popBackStack() }
                    )
                }
            }
        }
    }
}
