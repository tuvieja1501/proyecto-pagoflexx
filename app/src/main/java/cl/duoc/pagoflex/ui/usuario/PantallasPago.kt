package cl.duoc.pagoflex.ui.usuario

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Create
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.duoc.pagoflex.domain.Formatos
import cl.duoc.pagoflex.ui.componentes.BarraSuperior
import cl.duoc.pagoflex.ui.componentes.BotonGrande
import cl.duoc.pagoflex.ui.componentes.CampoValidado
import cl.duoc.pagoflex.ui.componentes.DialogoConfirmar
import cl.duoc.pagoflex.ui.componentes.FilaDato
import cl.duoc.pagoflex.ui.componentes.MensajeAnimado
import cl.duoc.pagoflex.ui.componentes.MensajeVacio
import cl.duoc.pagoflex.ui.componentes.PantallaCarga
import cl.duoc.pagoflex.ui.componentes.SelectorDesplegable
import cl.duoc.pagoflex.ui.componentes.Tarjeta

@Composable
fun PantallaPagar(vm: PagoViewModel, alPagado: () -> Unit, alVolver: () -> Unit) {
    val ui by vm.ui.collectAsStateWithLifecycle()
    val vibracion = LocalHapticFeedback.current
    var confirmando by remember { mutableStateOf(false) }

    LaunchedEffect(ui.comprobante) {
        if (ui.comprobante != null) {
            vibracion.performHapticFeedback(HapticFeedbackType.LongPress)
            alPagado()
        }
    }

    Scaffold(topBar = { BarraSuperior("Pagar", alVolver) }) { relleno ->
        val c = ui.compromiso
        if (c == null) {
            PantallaCarga()
        } else {
            Column(
                Modifier.fillMaxSize().padding(relleno).verticalScroll(rememberScrollState()).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Tarjeta {
                    Text(c.empresa, style = MaterialTheme.typography.titleLarge)
                    FilaDato("Concepto", c.concepto + (c.cuotaTexto?.let { " · cuota $it" } ?: ""))
                    FilaDato("Vencimiento", Formatos.fecha(c.vencimiento))
                    FilaDato("Monto", Formatos.pesos(c.monto))
                    if (c.recargo > 0) FilaDato("Recargo por atraso", Formatos.pesos(c.recargo))
                    FilaDato("Total a pagar", Formatos.pesos(c.totalAPagar), destacado = true)
                }
                if (ui.motivoNoPagable != null) {
                    MensajeAnimado(ui.motivoNoPagable, esError = false)
                } else {
                    SelectorDesplegable(
                        etiqueta = "¿Cómo quieres pagar?",
                        opciones = ui.medios,
                        seleccion = ui.medioElegido,
                        alSeleccionar = vm::elegirMedio,
                        error = ui.errorMedio
                    )
                    Text(
                        "Este es un pago de práctica: no se cobra dinero real ni se piden datos de tu tarjeta.",
                        style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    MensajeAnimado(ui.error)
                    if (ui.procesando) {
                        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                    } else {
                        BotonGrande("Pagar ${Formatos.pesos(c.totalAPagar)}", alClick = {
                            if (ui.medioElegido == null) vm.confirmar() else confirmando = true
                        })
                    }
                }
            }
        }
    }

    if (confirmando) {
        DialogoConfirmar(
            titulo = "¿Confirmas el pago?",
            texto = "Vas a pagar ${Formatos.pesos(ui.compromiso?.totalAPagar ?: 0)} a ${ui.compromiso?.empresa ?: ""}.",
            textoConfirmar = "Sí, pagar",
            alConfirmar = { confirmando = false; vm.confirmar() },
            alCancelar = { confirmando = false }
        )
    }
}

@Composable
fun PantallaComprobante(vm: ComprobanteViewModel, alInicio: () -> Unit, alVolver: () -> Unit) {
    val comprobante by vm.comprobante.collectAsStateWithLifecycle()
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }
    val escala by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "escalaCheck"
    )
    Scaffold(topBar = { BarraSuperior("Comprobante de pago", alVolver) }) { relleno ->
        val c = comprobante
        if (c == null) {
            MensajeVacio("Cargando tu comprobante…", Modifier.padding(relleno))
        } else {
            Column(
                Modifier.fillMaxSize().padding(relleno).verticalScroll(rememberScrollState()).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Filled.CheckCircle, contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(88.dp).scale(escala)
                )
                Text("¡Pago realizado!", style = MaterialTheme.typography.headlineSmall)
                Text(
                    if (c.aTiempo) "Pagaste a tiempo. ¡Muy bien!"
                    else "Pagaste con ${c.diasDeAtraso} día(s) de atraso. Ya quedó al día.",
                    style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center
                )
                Tarjeta {
                    FilaDato("N.º de comprobante", c.numero)
                    FilaDato("Fecha y hora", Formatos.fechaHora(c.fechaHora))
                    FilaDato("Pagado por", c.pagadoPor)
                    FilaDato("RUT", c.rutEnmascarado)
                    FilaDato("Empresa", c.empresa)
                    FilaDato("Razón social", c.razonSocial)
                    FilaDato("Concepto", c.concepto + (c.cuotaTexto?.let { " · cuota $it" } ?: ""))
                    FilaDato("Vencimiento", Formatos.fecha(c.vencimiento))
                    FilaDato("Monto", Formatos.pesos(c.montoCuota))
                    if (c.recargo > 0) FilaDato("Recargo", Formatos.pesos(c.recargo))
                    FilaDato("Total pagado", Formatos.pesos(c.totalPagado), destacado = true)
                    FilaDato("Medio de pago", c.medioPago)
                    FilaDato("Canal", c.canal)
                }
                BotonGrande("Volver al inicio", alInicio)
            }
        }
    }
}

@Composable
fun CampoComentario(valor: String, alCambiar: (String) -> Unit, error: String?, maximo: Int) {
    CampoValidado(
        valor = valor, alCambiar = alCambiar, etiqueta = "Cuéntanos qué pasó", error = error,
        varias = true, maxCaracteres = maximo, ayuda = "${valor.length}/$maximo", icono = Icons.Filled.Create
    )
}
