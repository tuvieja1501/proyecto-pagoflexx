package cl.duoc.pagoflex.ui.usuario

import androidx.compose.runtime.getValue
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.duoc.pagoflex.data.ItemHistorial
import cl.duoc.pagoflex.domain.Formatos
import cl.duoc.pagoflex.ui.componentes.AnilloPorcentaje
import cl.duoc.pagoflex.ui.componentes.BarraSuperior
import cl.duoc.pagoflex.ui.componentes.FilaDato
import cl.duoc.pagoflex.ui.componentes.MensajeVacio
import cl.duoc.pagoflex.ui.componentes.SelectorDesplegable
import cl.duoc.pagoflex.ui.componentes.Tarjeta

@Composable
fun PantallaHistorial(vm: UsuarioViewModel, alAbrirComprobante: (Int) -> Unit, alAbrirCompromiso: (Int) -> Unit) {
    val ui by vm.historial.collectAsStateWithLifecycle()
    Scaffold(topBar = { BarraSuperior("Historial") }) { relleno ->
        LazyColumn(
            Modifier.fillMaxSize().padding(relleno),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item(key = "filtros") {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    SelectorDesplegable(
                        "Empresa", listOf(0 to "Todas") + ui.empresas, ui.filtros.idEmpresa, vm::filtrarEmpresa, null
                    )
                    SelectorDesplegable(
                        "Año", listOf(0 to "Todos") + ui.anios.map { it to it.toString() }, ui.filtros.anio, vm::filtrarAnio, null
                    )
                    SelectorDesplegable(
                        "Mes", listOf(0 to "Todos") + (1..12).map { it to Formatos.nombreMes(it) }, ui.filtros.mes, vm::filtrarMes, null
                    )
                    TextButton(onClick = vm::limpiarFiltros) { Text("Quitar filtros") }
                }
            }
            if (ui.items.isEmpty()) {
                item(key = "vacio") { MensajeVacio("No hay movimientos con estos filtros.") }
            }
            items(ui.items, key = { item ->
                when (item) {
                    is ItemHistorial.DePago -> "p${item.comprobante.idPago}"
                    is ItemHistorial.DeAnulacion -> "a${item.compromiso.id}"
                }
            }) { item ->
                when (item) {
                    is ItemHistorial.DePago -> {
                        val c = item.comprobante
                        FilaMovimiento(
                            titulo = c.empresa,
                            detalle = c.concepto + (c.cuotaTexto?.let { " · cuota $it" } ?: ""),
                            estado = if (c.aTiempo) "Pagado a tiempo el ${Formatos.fecha(c.fechaHora.toLocalDate())}"
                            else "Pagado el ${Formatos.fecha(c.fechaHora.toLocalDate())} (${c.diasDeAtraso} día(s) después)",
                            monto = Formatos.pesos(c.totalPagado),
                            alClick = { alAbrirComprobante(c.idCompromiso) },
                            modifier = Modifier.animateItem()
                        )
                    }
                    is ItemHistorial.DeAnulacion -> {
                        val c = item.compromiso
                        FilaMovimiento(
                            titulo = c.empresa,
                            detalle = c.concepto + (c.cuotaTexto?.let { " · cuota $it" } ?: ""),
                            estado = "Anulado por la empresa el ${Formatos.fecha(item.fecha.toLocalDate())} · ${item.motivo}",
                            monto = Formatos.pesos(c.monto),
                            alClick = { alAbrirCompromiso(c.id) },
                            modifier = Modifier.animateItem()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FilaMovimiento(
    titulo: String, detalle: String, estado: String, monto: String, alClick: () -> Unit, modifier: Modifier = Modifier
) {
    Card(
        onClick = alClick, modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(titulo, style = MaterialTheme.typography.titleMedium)
            Text(detalle, style = MaterialTheme.typography.bodyLarge)
            Text(estado, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(monto, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun PantallaCumplimiento(vm: UsuarioViewModel) {
    val cumplimiento by vm.cumplimiento.collectAsStateWithLifecycle()
    Scaffold(topBar = { BarraSuperior("Mi cumplimiento") }) { relleno ->
        val c = cumplimiento
        Column(
            Modifier.fillMaxSize().padding(relleno).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (c == null) {
                MensajeVacio("Calculando tu indicador…")
            } else if (c.porcentaje == null) {
                AnilloPorcentaje(0f, "—")
                Text("Todavía estamos conociendo tu historial", style = MaterialTheme.typography.titleLarge)
                Text(
                    "Necesitamos al menos 3 compromisos en los últimos 12 meses para calcular tu indicador. " +
                        "Cada pago que hagas suma.",
                    style = MaterialTheme.typography.bodyLarge
                )
            } else {
                AnilloPorcentaje(c.porcentaje.toFloat(), Formatos.porcentaje(c.porcentaje))
                Text(c.categoria ?: "", style = MaterialTheme.typography.headlineSmall)
                Tarjeta {
                    FilaDato("Pagos a tiempo", "${c.aTiempo} de ${c.considerados}")
                    FilaDato("Período", "Últimos 12 meses")
                }
                Text(
                    "Se cuentan los compromisos pagados antes o el mismo día de su vencimiento. " +
                        "Los reportes en revisión y los anulados no te afectan.",
                    style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
