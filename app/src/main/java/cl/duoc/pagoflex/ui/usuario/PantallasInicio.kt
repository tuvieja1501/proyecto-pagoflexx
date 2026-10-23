package cl.duoc.pagoflex.ui.usuario

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.duoc.pagoflex.data.RepositorioUsuarioFinal
import cl.duoc.pagoflex.domain.AppClock
import cl.duoc.pagoflex.domain.EstadoCompromiso
import cl.duoc.pagoflex.domain.Formatos
import cl.duoc.pagoflex.domain.Rules
import cl.duoc.pagoflex.ui.componentes.AccionSalir
import cl.duoc.pagoflex.ui.componentes.BarraSuperior
import cl.duoc.pagoflex.ui.componentes.BotonGrande
import cl.duoc.pagoflex.ui.componentes.BotonSecundario
import cl.duoc.pagoflex.ui.componentes.EstadoChip
import cl.duoc.pagoflex.ui.componentes.FilaDato
import cl.duoc.pagoflex.ui.componentes.MensajeAnimado
import cl.duoc.pagoflex.ui.componentes.MensajeVacio
import cl.duoc.pagoflex.ui.componentes.PantallaCarga
import cl.duoc.pagoflex.ui.componentes.Tarjeta
import cl.duoc.pagoflex.ui.componentes.TarjetaCompromiso
import cl.duoc.pagoflex.ui.componentes.TituloSeccion
import cl.duoc.pagoflex.ui.componentes.diasParaVencer
import cl.duoc.pagoflex.ui.contenedorActual

@Composable
fun PantallaInicio(vm: UsuarioViewModel, alAbrir: (Int) -> Unit, alAvisos: () -> Unit, alSalir: () -> Unit) {
    val ui by vm.inicio.collectAsStateWithLifecycle()
    Scaffold(
        topBar = {
            BarraSuperior("Mis pagos", acciones = {
                IconButton(onClick = alAvisos, modifier = Modifier.size(48.dp)) {
                    Icon(Icons.Filled.Notifications, contentDescription = "Configurar avisos")
                }
                AccionSalir(alSalir)
            })
        }
    ) { relleno ->
        LazyColumn(
            Modifier.fillMaxSize().padding(relleno),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item(key = "saludo") {
                Text("Hola, ${ui.nombre}", style = MaterialTheme.typography.headlineSmall)
            }
            item(key = "resumen") { TarjetaResumen(ui) }
            item(key = "titulo") { TituloSeccion("Tus compromisos vigentes") }
            if (ui.vigentes.isEmpty()) {
                item(key = "vacio") { MensajeVacio("No tienes compromisos por pagar. ¡Vas al día!") }
            }
            items(ui.vigentes, key = { it.id }) { c ->
                TarjetaCompromiso(c, alClick = { alAbrir(c.id) }, modifier = Modifier.animateItem(), extra = diasParaVencer(c))
            }
        }
    }
}

@Composable
private fun TarjetaResumen(ui: InicioUi) {
    var meta by remember { mutableIntStateOf(0) }
    LaunchedEffect(ui.totalMes) { meta = ui.totalMes }
    val total by animateIntAsState(targetValue = meta, animationSpec = tween(900), label = "totalMes")
    Card(
        Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Total a pagar este mes", style = MaterialTheme.typography.titleMedium)
            Text(Formatos.pesos(total), style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.Bold)
            val p = ui.proximo
            Text(
                if (p != null) "Próximo vencimiento: ${Formatos.fecha(p.vencimiento)} · ${p.empresa}"
                else "No tienes vencimientos próximos.",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
fun PantallaDetalle(
    vm: DetalleCompromisoViewModel,
    alPagar: () -> Unit,
    alReportar: () -> Unit,
    alComprobante: () -> Unit,
    alVolver: () -> Unit
) {
    val ui by vm.ui.collectAsStateWithLifecycle()
    Scaffold(topBar = { BarraSuperior("Detalle del compromiso", alVolver) }) { relleno ->
        val c = ui.compromiso
        if (!ui.cargado) {
            PantallaCarga()
        } else if (c == null) {
            MensajeVacio("No encontramos este compromiso.", Modifier.padding(relleno))
        } else {
            Column(
                Modifier.fillMaxSize().padding(relleno).verticalScroll(rememberScrollState()).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Tarjeta {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(c.empresa, style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                        EstadoChip(c.idEstado, c.estado)
                    }
                    FilaDato("Concepto", c.concepto)
                    c.cuotaTexto?.let { FilaDato("Cuota", it) }
                    FilaDato("Vencimiento", Formatos.fecha(c.vencimiento))
                    FilaDato("Monto", Formatos.pesos(c.monto))
                    if (c.recargo > 0) {
                        FilaDato("Recargo por atraso", Formatos.pesos(c.recargo))
                        FilaDato("Días de atraso", Rules.diasAtraso(c.vencimiento, AppClock.hoy()).toString())
                    }
                    FilaDato("Total a pagar", Formatos.pesos(c.totalAPagar), destacado = true)
                    FilaDato("Folio", c.folio)
                }
                ui.reporte?.let { r ->
                    Tarjeta {
                        TituloSeccion("Tu reporte ${r.numero}")
                        FilaDato("Estado", r.estado)
                        FilaDato("Motivo", r.motivo)
                        r.comentario?.let { FilaDato("Tu comentario", it) }
                        r.respuesta?.let { FilaDato("Respuesta", it) }
                    }
                }
                MensajeAnimado(ui.motivoNoPagable, esError = false)
                when {
                    c.idEstado == EstadoCompromiso.PAGADO -> BotonGrande("Ver comprobante", alComprobante)
                    Rules.puedePagar(c.idEstado) -> {
                        BotonGrande("Pagar ahora", alPagar, habilitado = ui.motivoNoPagable == null)
                    }
                }
                if (Rules.puedeReportar(c.idEstado)) {
                    BotonSecundario("Reportar un problema", alReportar)
                }
            }
        }
    }
}

@Composable
fun PantallaAvisos(vm: UsuarioViewModel, alVolver: () -> Unit) {
    val contexto = LocalContext.current
    val contenedor = contenedorActual()
    val persona by vm.persona.collectAsStateWithLifecycle()
    val mensaje by vm.mensajeAvisos.collectAsStateWithLifecycle()
    val error by vm.avisosError.collectAsStateWithLifecycle()
    var activos by remember(persona?.idUsuario) { mutableStateOf(persona?.avisosActivos ?: false) }
    var dias by remember(persona?.idUsuario) { mutableIntStateOf(persona?.diasAnticipacion ?: 3) }
    var permisoDenegado by remember { mutableStateOf(false) }

    val lanzador = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { concedido ->
        permisoDenegado = !concedido
        vm.guardarAvisos(activos, dias)
        vm.revisarAvisosAhora(contexto.applicationContext)
    }

    Scaffold(topBar = { BarraSuperior("Avisos de vencimiento", alVolver) }) { relleno ->
        Column(
            Modifier.fillMaxSize().padding(relleno).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Te enviamos una notificación antes de cada vencimiento para que no se te pase. " +
                    "Nunca te avisamos entre las 22:00 y las 08:00.",
                style = MaterialTheme.typography.bodyLarge
            )
            Tarjeta {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("Recibir avisos", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                    Switch(checked = activos, onCheckedChange = { activos = it; vm.limpiarMensajesAvisos() })
                }
                if (activos) {
                    TituloSeccion("¿Con cuántos días de anticipación?")
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        RepositorioUsuarioFinal.OPCIONES_ANTICIPACION.forEach { opcion ->
                            FilterChip(
                                selected = dias == opcion,
                                onClick = { dias = opcion; vm.limpiarMensajesAvisos() },
                                label = { Text(if (opcion == 1) "1 día" else "$opcion días", style = MaterialTheme.typography.bodyLarge) }
                            )
                        }
                    }
                }
            }
            MensajeAnimado(error)
            MensajeAnimado(mensaje, esError = false)
            MensajeAnimado(
                if (permisoDenegado) "El teléfono tiene bloqueadas las notificaciones de PagoFlex. Puedes activarlas en Ajustes del teléfono." else null
            )
            BotonGrande("Guardar", alClick = {
                if (activos && !contenedor.notificador.tienePermiso()) {
                    lanzador.launch(Manifest.permission.POST_NOTIFICATIONS)
                } else {
                    vm.guardarAvisos(activos, dias)
                    vm.revisarAvisosAhora(contexto.applicationContext)
                }
            })
        }
    }
}
