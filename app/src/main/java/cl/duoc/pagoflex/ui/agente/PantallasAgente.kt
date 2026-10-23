package cl.duoc.pagoflex.ui.agente

import androidx.compose.runtime.getValue
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.duoc.pagoflex.domain.EstadoReporte
import cl.duoc.pagoflex.domain.Formatos
import cl.duoc.pagoflex.ui.componentes.AccionSalir
import cl.duoc.pagoflex.ui.componentes.BarraSuperior
import cl.duoc.pagoflex.ui.componentes.BotonGrande
import cl.duoc.pagoflex.ui.componentes.CampoValidado
import cl.duoc.pagoflex.ui.componentes.FilaDato
import cl.duoc.pagoflex.ui.componentes.MensajeAnimado
import cl.duoc.pagoflex.ui.componentes.MensajeVacio
import cl.duoc.pagoflex.ui.componentes.PantallaCarga
import cl.duoc.pagoflex.ui.componentes.Tarjeta
import cl.duoc.pagoflex.ui.componentes.TarjetaReporte
import cl.duoc.pagoflex.ui.componentes.TituloSeccion

private val filtros = listOf(
    0 to "Todos",
    EstadoReporte.ABIERTO to "Abiertos",
    EstadoReporte.EN_GESTION to "En gestión",
    EstadoReporte.RESUELTO to "Resueltos"
)

@Composable
fun PantallaBandeja(vm: BandejaViewModel, alAbrir: (Int) -> Unit, alSalir: () -> Unit) {
    val ui by vm.ui.collectAsStateWithLifecycle()
    Scaffold(topBar = { BarraSuperior("Reportes de pagos", acciones = { AccionSalir(alSalir) }) }) { relleno ->
        LazyColumn(
            Modifier.fillMaxSize().padding(relleno),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item(key = "filtros") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        filtros.forEach { (id, nombre) ->
                            FilterChip(
                                selected = ui.filtro == id, onClick = { vm.filtrar(id) },
                                label = { Text(nombre, style = MaterialTheme.typography.bodyMedium) }
                            )
                        }
                    }
                    if (ui.atrasados > 0) {
                        Text(
                            "${ui.atrasados} reporte(s) llevan más de 2 días hábiles sin gestionar.",
                            style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                }
            }
            if (ui.cargando) item(key = "carga") { PantallaCarga() }
            else if (ui.items.isEmpty()) item(key = "vacio") { MensajeVacio("No hay reportes con este filtro.") }
            items(ui.items, key = { it.id }) { r ->
                TarjetaReporte(r, alClick = { alAbrir(r.id) }, modifier = Modifier.animateItem(), mostrarPersona = true)
            }
        }
    }
}

@Composable
fun PantallaReporteAgente(vm: ReporteAgenteViewModel, idAgente: Int, alVolver: () -> Unit) {
    val ui by vm.ui.collectAsStateWithLifecycle()
    Scaffold(topBar = { BarraSuperior("Reporte", alVolver) }) { relleno ->
        val r = ui.reporte
        if (!ui.cargado) {
            PantallaCarga()
        } else if (r == null) {
            MensajeVacio("No encontramos este reporte.", Modifier.padding(relleno))
        } else {
            Column(
                Modifier.fillMaxSize().padding(relleno).verticalScroll(rememberScrollState()).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Tarjeta {
                    Text(r.numero, style = MaterialTheme.typography.titleLarge)
                    FilaDato("Estado del reporte", r.estado + if (r.atrasado) " (atrasado)" else "")
                    FilaDato("Enviado", Formatos.fechaHora(r.fechaCreacion))
                    FilaDato("Motivo", r.motivo)
                    r.comentario?.let { FilaDato("Comentario", it) }
                }
                Tarjeta {
                    TituloSeccion("Persona")
                    FilaDato("Nombre", r.nombrePersona)
                    FilaDato("RUT", r.rutPersona)
                }
                Tarjeta {
                    TituloSeccion("Compromiso")
                    FilaDato("Empresa", r.empresa)
                    FilaDato("Concepto", r.concepto + (r.cuotaTexto?.let { " · cuota $it" } ?: ""))
                    FilaDato("Monto", Formatos.pesos(r.monto))
                    FilaDato("Estado", r.estadoCompromiso)
                    FilaDato("Folio", r.folio)
                }
                r.nombreAgente?.let { nombre ->
                    Tarjeta {
                        TituloSeccion("Gestión")
                        FilaDato("Agente", nombre)
                        r.fechaToma?.let { FilaDato("Tomado", Formatos.fechaHora(it)) }
                        r.fechaResolucion?.let { FilaDato("Resuelto", Formatos.fechaHora(it)) }
                        r.respuesta?.let { FilaDato("Respuesta", it) }
                    }
                }
                MensajeAnimado(ui.error)
                MensajeAnimado(ui.mensaje, esError = false)
                when {
                    r.idEstado == EstadoReporte.ABIERTO ->
                        BotonGrande("Tomar este reporte", vm::tomar, habilitado = !ui.trabajando)
                    r.idEstado == EstadoReporte.EN_GESTION && r.idAgente == idAgente -> {
                        CampoValidado(
                            valor = ui.respuesta, alCambiar = vm::cambiarRespuesta,
                            etiqueta = "Respuesta para la persona", error = ui.errorRespuesta,
                            varias = true, maxCaracteres = vm.maxRespuesta, ayuda = "${ui.respuesta.length}/${vm.maxRespuesta}", icono = Icons.Filled.Create
                        )
                        BotonGrande("Resolver reporte", vm::resolver, habilitado = !ui.trabajando)
                    }
                    r.idEstado == EstadoReporte.EN_GESTION ->
                        Text("Este reporte lo está gestionando ${r.nombreAgente ?: "otro agente"}.", style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}
