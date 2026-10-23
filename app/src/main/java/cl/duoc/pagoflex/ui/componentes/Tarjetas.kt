package cl.duoc.pagoflex.ui.componentes

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import cl.duoc.pagoflex.data.CompromisoVista
import cl.duoc.pagoflex.data.ReporteVista
import cl.duoc.pagoflex.domain.Formatos
import cl.duoc.pagoflex.domain.Rules
import cl.duoc.pagoflex.domain.AppClock

@Composable
fun TarjetaCompromiso(c: CompromisoVista, alClick: () -> Unit, modifier: Modifier = Modifier, extra: String? = null) {
    Card(
        onClick = alClick,
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Text(c.empresa, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                EstadoChip(c.idEstado, c.estado, Modifier.padding(start = 8.dp))
            }
            Text(
                c.concepto + (c.cuotaTexto?.let { " · cuota $it" } ?: ""),
                style = MaterialTheme.typography.bodyLarge
            )
            if (extra != null) Text(extra, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(Modifier.fillMaxWidth().padding(top = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Vence ${Formatos.fecha(c.vencimiento)}", style = MaterialTheme.typography.bodyMedium)
                Text(Formatos.pesos(c.totalAPagar), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            if (c.recargo > 0) {
                Text(
                    "Incluye recargo de ${Formatos.pesos(c.recargo)}",
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun TarjetaReporte(r: ReporteVista, alClick: () -> Unit, modifier: Modifier = Modifier, mostrarPersona: Boolean = false) {
    Card(
        onClick = alClick,
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(r.numero, style = MaterialTheme.typography.titleMedium)
                Text(r.estado + if (r.atrasado) " · atrasado" else "", style = MaterialTheme.typography.labelLarge,
                    color = if (r.atrasado) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary)
            }
            Text("${r.empresa} · ${r.concepto}" + (r.cuotaTexto?.let { " (cuota $it)" } ?: ""), style = MaterialTheme.typography.bodyLarge)
            Text("Motivo: ${r.motivo}", style = MaterialTheme.typography.bodyMedium)
            if (mostrarPersona) Text("${r.nombrePersona} · ${r.rutPersona}", style = MaterialTheme.typography.bodyMedium)
            Text("Enviado el ${Formatos.fechaHora(r.fechaCreacion)}", style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (r.respuesta != null) {
                Text("Respuesta: ${r.respuesta}", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 4.dp))
            }
        }
    }
}

@Composable
fun AnilloPorcentaje(porcentaje: Float, textoCentro: String, tamano: Dp = 160.dp, modifier: Modifier = Modifier) {
    var meta by remember { mutableStateOf(0f) }
    LaunchedEffect(porcentaje) { meta = porcentaje.coerceIn(0f, 100f) }
    val animado by animateFloatAsState(targetValue = meta, animationSpec = tween(durationMillis = 1000), label = "anillo")
    val colorFondo = MaterialTheme.colorScheme.surfaceVariant
    val colorProgreso = MaterialTheme.colorScheme.primary
    Box(modifier.size(tamano), contentAlignment = Alignment.Center) {
        Canvas(Modifier.size(tamano)) {
            val grosor = 16.dp.toPx()
            val lado = size.minDimension - grosor
            val arriba = Offset(grosor / 2, grosor / 2)
            drawArc(colorFondo, 0f, 360f, false, arriba, Size(lado, lado), style = Stroke(grosor))
            drawArc(
                colorProgreso, -90f, 360f * animado / 100f, false, arriba, Size(lado, lado),
                style = Stroke(grosor, cap = StrokeCap.Round)
            )
        }
        Text(textoCentro, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
    }
}

fun textoHoy(): String = Formatos.fecha(AppClock.hoy())

fun diasParaVencer(c: CompromisoVista): String {
    val dias = java.time.temporal.ChronoUnit.DAYS.between(AppClock.hoy(), c.vencimiento).toInt()
    return when {
        c.idEstado == cl.duoc.pagoflex.domain.EstadoCompromiso.VENCIDO ->
            "Venció hace ${Rules.diasAtraso(c.vencimiento, AppClock.hoy())} día(s)"
        dias == 0 -> "Vence hoy"
        dias == 1 -> "Vence mañana"
        dias > 1 -> "Vence en $dias días"
        else -> ""
    }
}
