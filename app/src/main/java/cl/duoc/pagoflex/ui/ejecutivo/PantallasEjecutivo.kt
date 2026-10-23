package cl.duoc.pagoflex.ui.ejecutivo

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.duoc.pagoflex.domain.Formatos
import cl.duoc.pagoflex.domain.MotivoAnulacionIds
import cl.duoc.pagoflex.domain.Rules
import cl.duoc.pagoflex.ui.componentes.AccionSalir
import cl.duoc.pagoflex.ui.componentes.AnilloPorcentaje
import cl.duoc.pagoflex.ui.componentes.BarraSuperior
import cl.duoc.pagoflex.ui.componentes.BotonGrande
import cl.duoc.pagoflex.ui.componentes.BotonSecundario
import cl.duoc.pagoflex.ui.componentes.CampoFecha
import cl.duoc.pagoflex.ui.componentes.CampoValidado
import cl.duoc.pagoflex.ui.componentes.EstadoChip
import cl.duoc.pagoflex.ui.componentes.FilaDato
import cl.duoc.pagoflex.ui.componentes.MensajeAnimado
import cl.duoc.pagoflex.ui.componentes.MensajeVacio
import cl.duoc.pagoflex.ui.componentes.PantallaCarga
import cl.duoc.pagoflex.ui.componentes.SelectorDesplegable
import cl.duoc.pagoflex.ui.componentes.Tarjeta
import cl.duoc.pagoflex.ui.componentes.TituloSeccion
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.ui.text.font.FontWeight

@Composable
fun PantallaCompromisosEjecutivo(vm: EjecutivoViewModel, alAbrir: (Int) -> Unit, alSalir: () -> Unit) {
    val ui by vm.lista.collectAsStateWithLifecycle()
    Scaffold(topBar = { BarraSuperior(if (ui.empresa.isBlank()) "Compromisos" else ui.empresa, acciones = { AccionSalir(alSalir) }) }) { relleno ->
        LazyColumn(
            Modifier.fillMaxSize().padding(relleno),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item(key = "filtros") {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    SelectorDesplegable("Estado", listOf(0 to "Todos") + ui.estados, ui.filtroEstado, vm::filtrarEstado, null)
                    SelectorDesplegable("Mes de vencimiento", listOf(0 to "Todos") + ui.meses, ui.filtroMes, vm::filtrarMes, null)
                }
            }
            if (ui.cargando) item(key = "carga") { PantallaCarga() }
            else if (ui.filas.isEmpty()) item(key = "vacio") { MensajeVacio("No hay compromisos con estos filtros.") }
            items(ui.filas, key = { it.compromiso.id }) { f ->
                val c = f.compromiso
                Card(
                    onClick = { alAbrir(c.id) }, modifier = Modifier.fillMaxWidth().animateItem(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                            Text(f.nombrePersona, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                            EstadoChip(c.idEstado, c.estado, Modifier.padding(start = 8.dp))
                        }
                        Text("RUT ${f.rutPersona}", style = MaterialTheme.typography.bodyMedium)
                        Text(c.concepto + (c.cuotaTexto?.let { " · cuota $it" } ?: ""), style = MaterialTheme.typography.bodyLarge)
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Vence ${Formatos.fecha(c.vencimiento)}", style = MaterialTheme.typography.bodyMedium)
                            Text(Formatos.pesos(c.totalAPagar), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PantallaDetalleEjecutivo(vm: DetalleEjecutivoViewModel, alVolver: () -> Unit) {
    val ui by vm.ui.collectAsStateWithLifecycle()
    var anulando by remember { mutableStateOf(false) }
    Scaffold(topBar = { BarraSuperior("Detalle del compromiso", alVolver) }) { relleno ->
        val d = ui.detalle
        if (!ui.cargado) {
            PantallaCarga()
        } else if (d == null) {
            MensajeVacio("No encontramos este compromiso en tu empresa.", Modifier.padding(relleno))
        } else {
            val c = d.compromiso
            Column(
                Modifier.fillMaxSize().padding(relleno).verticalScroll(rememberScrollState()).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Tarjeta {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(c.folio, style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                        EstadoChip(c.idEstado, c.estado)
                    }
                    FilaDato("Concepto", c.concepto)
                    c.cuotaTexto?.let { FilaDato("Cuota", it) }
                    FilaDato("Vencimiento", Formatos.fecha(c.vencimiento))
                    FilaDato("Monto", Formatos.pesos(c.monto))
                    if (c.recargo > 0) FilaDato("Recargo", Formatos.pesos(c.recargo))
                    FilaDato("Total", Formatos.pesos(c.totalAPagar), destacado = true)
                    FilaDato("Registrado por", d.registradoPor)
                }
                Tarjeta {
                    TituloSeccion("Persona")
                    FilaDato("Nombre", d.nombrePersona)
                    FilaDato("RUT", d.rutPersona)
                    d.correoPersona?.let { FilaDato("Correo", it) }
                    d.telefonoPersona?.let { FilaDato("Teléfono", it) }
                }
                d.comprobante?.let { p ->
                    Tarjeta {
                        TituloSeccion("Pago")
                        FilaDato("Comprobante", p.numero)
                        FilaDato("Fecha", Formatos.fechaHora(p.fechaHora))
                        FilaDato("Total pagado", Formatos.pesos(p.totalPagado))
                        FilaDato("Medio de pago", p.medioPago)
                        FilaDato("Canal", p.canal)
                    }
                }
                d.reporte?.let { r ->
                    Tarjeta {
                        TituloSeccion("Reporte de la persona")
                        FilaDato("N.º", r.numero)
                        FilaDato("Estado", r.estado)
                        FilaDato("Motivo", r.motivo)
                    }
                }
                d.anulacion?.let { a ->
                    Tarjeta {
                        TituloSeccion("Anulación")
                        FilaDato("Motivo", a.motivo)
                        a.comentario?.let { FilaDato("Comentario", it) }
                        FilaDato("Anulado por", a.ejecutivo)
                        FilaDato("Fecha", Formatos.fechaHora(a.fecha))
                    }
                }
                MensajeAnimado(ui.mensaje, esError = false)
                MensajeAnimado(ui.error)
                if (Rules.puedeAnular(c.idEstado)) {
                    BotonSecundario("Anular compromiso", { vm.limpiarErrores(); anulando = true })
                }
            }
        }
    }
    if (anulando) {
        DialogoAnular(
            motivos = ui.motivos, errorMotivo = ui.errorMotivo, errorComentario = ui.errorComentario,
            error = ui.error, trabajando = ui.trabajando, maxComentario = vm.maxComentario,
            alConfirmar = { motivo, comentario -> vm.anular(motivo, comentario) { ok -> if (ok) anulando = false } },
            alCancelar = { anulando = false }
        )
    }
}

@Composable
private fun DialogoAnular(
    motivos: List<Pair<Int, String>>,
    errorMotivo: String?,
    errorComentario: String?,
    error: String?,
    trabajando: Boolean,
    maxComentario: Int,
    alConfirmar: (Int?, String) -> Unit,
    alCancelar: () -> Unit
) {
    var motivo by remember { mutableStateOf<Int?>(null) }
    var comentario by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = alCancelar,
        title = { Text("Anular compromiso") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("La persona dejará de ver este compromiso como pendiente de pago.", style = MaterialTheme.typography.bodyMedium)
                SelectorDesplegable("Motivo", motivos, motivo, { motivo = it }, errorMotivo)
                CampoValidado(
                    valor = comentario, alCambiar = { comentario = it },
                    etiqueta = if (motivo == MotivoAnulacionIds.OTRO) "Comentario (obligatorio)" else "Comentario (opcional)",
                    error = errorComentario, varias = true, maxCaracteres = maxComentario,
                    ayuda = "${comentario.length}/$maxComentario"
                )
                MensajeAnimado(error)
            }
        },
        confirmButton = { TextButton(onClick = { alConfirmar(motivo, comentario) }, enabled = !trabajando) { Text("Anular") } },
        dismissButton = { TextButton(onClick = alCancelar) { Text("Cancelar") } }
    )
}

@Composable
fun PantallaRegistrar(vm: RegistroViewModel) {
    val ui by vm.ui.collectAsStateWithLifecycle()
    Scaffold(topBar = { BarraSuperior("Registrar compromiso") }) { relleno ->
        Column(
            Modifier.fillMaxSize().padding(relleno).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            TituloSeccion("Persona")
            CampoValidado(
                ui.rut, vm::cambiarRut, "RUT de la persona", ui.errores["rut"],
                ayuda = "Por ejemplo 12.345.678-5", icono = Icons.Filled.Person
            )
            ui.persona?.let { Text("Persona encontrada: ${it.nombreCompleto}", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.secondary) }
            if (ui.pedirDatosPersona) {
                Text("No tenemos a esta persona registrada. Completa sus datos.", style = MaterialTheme.typography.bodyMedium)
                CampoValidado(ui.nombres, vm::cambiarNombres, "Nombres", ui.errores["nombres"], icono = Icons.Filled.Person)
                CampoValidado(ui.apellidos, vm::cambiarApellidos, "Apellidos", ui.errores["apellidos"], icono = Icons.Filled.Person)
                CampoValidado(ui.correo, vm::cambiarCorreo, "Correo", ui.errores["correo"], teclado = KeyboardType.Email, icono = Icons.Filled.Email)
                CampoValidado(ui.telefono, vm::cambiarTelefono, "Teléfono", ui.errores["telefono"], teclado = KeyboardType.Phone, icono = Icons.Filled.Phone)
            }
            TituloSeccion("Compromiso")
            CampoValidado(
                ui.concepto, vm::cambiarConcepto, "Concepto", ui.errores["concepto"],
                maxCaracteres = vm.maxConcepto, icono = Icons.Filled.Create
            )
            CampoValidado(ui.monto, vm::cambiarMonto, "Monto en pesos", ui.errores["monto"], teclado = KeyboardType.Number, icono = Icons.Filled.Add)
            CampoFecha(ui.vencimiento, vm::cambiarVencimiento, "Fecha de vencimiento", ui.errores["vencimiento"])
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                CampoValidado(
                    ui.cuota, vm::cambiarCuota, "N.º de cuota", ui.errores["cuota"],
                    modifier = Modifier.weight(1f), teclado = KeyboardType.Number, ayuda = "Opcional"
                )
                CampoValidado(
                    ui.totalCuotas, vm::cambiarTotalCuotas, "Total de cuotas", ui.errores["total"],
                    modifier = Modifier.weight(1f), teclado = KeyboardType.Number, ayuda = "Opcional"
                )
            }
            MensajeAnimado(ui.error)
            MensajeAnimado(ui.exito, esError = false)
            BotonGrande("Registrar compromiso", vm::registrar, habilitado = !ui.guardando)
        }
    }
}

@Composable
fun PantallaCobranza(vm: EjecutivoViewModel) {
    val mes by vm.mesCobranza.collectAsStateWithLifecycle()
    val cobranza by vm.cobranza.collectAsStateWithLifecycle()
    Scaffold(topBar = { BarraSuperior("Resumen de cobranza") }) { relleno ->
        Column(
            Modifier.fillMaxSize().padding(relleno).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = vm::mesAnterior) { Text("‹ Anterior") }
                Text(Formatos.mesAnio(mes.year, mes.monthValue), style = MaterialTheme.typography.titleLarge)
                TextButton(onClick = vm::mesSiguiente) { Text("Siguiente ›") }
            }
            val c = cobranza
            if (c == null) {
                PantallaCarga("Calculando…")
            } else if (c.tasa == null) {
                MensajeVacio("No hay compromisos que venzan en este mes.")
            } else {
                AnilloPorcentaje(c.tasa.toFloat(), Formatos.porcentaje(c.tasa))
                Text("Tasa de cobranza", style = MaterialTheme.typography.titleMedium)
                Tarjeta {
                    FilaDato("Compromisos del mes", c.total.toString())
                    FilaDato("Pagados", c.pagados.toString())
                    FilaDato("Pendientes", c.pendientes.toString())
                    FilaDato("Vencidos", c.vencidos.toString())
                    FilaDato("En revisión", c.enRevision.toString())
                    FilaDato("Anulados (no cuentan)", c.anulados.toString())
                    FilaDato("Monto recaudado", Formatos.pesos(c.montoRecaudado), destacado = true)
                }
            }
        }
    }
}
