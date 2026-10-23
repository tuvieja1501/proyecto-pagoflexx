package cl.duoc.pagoflex.ui.usuario

import androidx.compose.runtime.getValue
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.duoc.pagoflex.domain.Formatos
import cl.duoc.pagoflex.ui.componentes.BarraSuperior
import cl.duoc.pagoflex.ui.componentes.BotonGrande
import cl.duoc.pagoflex.ui.componentes.FilaDato
import cl.duoc.pagoflex.ui.componentes.MensajeAnimado
import cl.duoc.pagoflex.ui.componentes.MensajeVacio
import cl.duoc.pagoflex.ui.componentes.PantallaCarga
import cl.duoc.pagoflex.ui.componentes.SelectorDesplegable
import cl.duoc.pagoflex.ui.componentes.Tarjeta
import cl.duoc.pagoflex.ui.componentes.TarjetaReporte

@Composable
fun PantallaReportar(vm: ReporteViewModel, alTerminar: () -> Unit, alVolver: () -> Unit) {
    val ui by vm.ui.collectAsStateWithLifecycle()
    Scaffold(topBar = { BarraSuperior("Reportar un problema", alVolver) }) { relleno ->
        val enviado = ui.enviado
        val c = ui.compromiso
        if (enviado != null) {
            Column(
                Modifier.fillMaxSize().padding(relleno).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Recibimos tu reporte", style = MaterialTheme.typography.headlineSmall)
                Tarjeta {
                    FilaDato("N.º de reporte", enviado.numero)
                    FilaDato("Motivo", enviado.motivo)
                    FilaDato("Estado", enviado.estado)
                }
                Text(
                    "Un agente lo revisará y te responderá aquí. Mientras tanto, este compromiso queda en revisión " +
                        "y no necesitas pagarlo.",
                    style = MaterialTheme.typography.bodyLarge
                )
                BotonGrande("Listo", alTerminar)
            }
        } else if (c == null) {
            PantallaCarga()
        } else {
            Column(
                Modifier.fillMaxSize().padding(relleno).verticalScroll(rememberScrollState()).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Tarjeta {
                    Text(c.empresa, style = MaterialTheme.typography.titleMedium)
                    FilaDato("Concepto", c.concepto + (c.cuotaTexto?.let { " · cuota $it" } ?: ""))
                    FilaDato("Total", Formatos.pesos(c.totalAPagar))
                }
                SelectorDesplegable(
                    etiqueta = "¿Qué pasó?", opciones = ui.motivos, seleccion = ui.motivo,
                    alSeleccionar = vm::elegirMotivo, error = ui.errorMotivo
                )
                CampoComentario(ui.comentario, vm::cambiarComentario, ui.errorComentario, vm.maxComentario)
                MensajeAnimado(ui.error)
                BotonGrande("Enviar reporte", vm::enviar, habilitado = !ui.enviando)
            }
        }
    }
}

@Composable
fun PantallaMisReportes(vm: UsuarioViewModel, alAbrirCompromiso: (Int) -> Unit) {
    val reportes by vm.reportes.collectAsStateWithLifecycle()
    Scaffold(topBar = { BarraSuperior("Mis reportes") }) { relleno ->
        if (reportes.isEmpty()) {
            MensajeVacio("Todavía no has reportado ningún problema.", Modifier.padding(relleno))
        } else {
            LazyColumn(
                Modifier.fillMaxSize().padding(relleno),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(reportes, key = { it.id }) { r ->
                    TarjetaReporte(r, alClick = { alAbrirCompromiso(r.idCompromiso) }, modifier = Modifier.animateItem())
                }
            }
        }
    }
}
