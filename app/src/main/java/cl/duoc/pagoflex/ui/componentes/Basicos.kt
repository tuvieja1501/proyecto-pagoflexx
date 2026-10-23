package cl.duoc.pagoflex.ui.componentes

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cl.duoc.pagoflex.domain.Formatos
import cl.duoc.pagoflex.ui.theme.coloresDeEstado
import java.time.LocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BarraSuperior(
    titulo: String,
    alVolver: (() -> Unit)? = null,
    acciones: @Composable () -> Unit = {}
) {
    TopAppBar(
        title = { Text(titulo, style = MaterialTheme.typography.titleLarge) },
        navigationIcon = {
            if (alVolver != null) {
                IconButton(onClick = alVolver, modifier = Modifier.size(48.dp)) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                }
            }
        },
        actions = { acciones() },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            titleContentColor = MaterialTheme.colorScheme.onPrimary,
            navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
            actionIconContentColor = MaterialTheme.colorScheme.onPrimary
        )
    )
}

@Composable
fun PantallaCarga(mensaje: String = "Cargando tus datos…") {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
            CircularProgressIndicator()
            Text(mensaje, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
fun MensajeVacio(texto: String, modifier: Modifier = Modifier) {
    Box(modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
        Text(
            texto, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun EstadoChip(idEstado: Int, nombre: String, modifier: Modifier = Modifier) {
    val colores = coloresDeEstado(idEstado)
    Surface(modifier = modifier, shape = RoundedCornerShape(50), color = colores.fondo, contentColor = colores.texto) {
        Text(
            nombre, style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
        )
    }
}

@Composable
fun FilaDato(etiqueta: String, valor: String, destacado: Boolean = false) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            etiqueta, style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(0.45f)
        )
        Text(
            valor,
            style = if (destacado) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyLarge,
            fontWeight = if (destacado) FontWeight.Bold else FontWeight.Normal,
            textAlign = TextAlign.End, modifier = Modifier.weight(0.55f)
        )
    }
}

@Composable
fun Tarjeta(modifier: Modifier = Modifier, contenido: @Composable () -> Unit) {
    Card(
        modifier = modifier.fillMaxWidth().animateContentSize(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.padding(16.dp)) { contenido() }
    }
}

@Composable
fun TituloSeccion(texto: String) {
    Text(
        texto, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
    )
}

@Composable
fun BotonGrande(texto: String, alClick: () -> Unit, modifier: Modifier = Modifier, habilitado: Boolean = true) {
    Button(
        onClick = alClick, enabled = habilitado,
        modifier = modifier.fillMaxWidth().heightIn(min = 56.dp),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp)
    ) { Text(texto, style = MaterialTheme.typography.labelLarge) }
}

@Composable
fun BotonSecundario(texto: String, alClick: () -> Unit, modifier: Modifier = Modifier, habilitado: Boolean = true) {
    OutlinedButton(
        onClick = alClick, enabled = habilitado,
        modifier = modifier.fillMaxWidth().heightIn(min = 56.dp),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp)
    ) { Text(texto, style = MaterialTheme.typography.labelLarge) }
}

@Composable
fun MensajeAnimado(texto: String?, esError: Boolean = true, modifier: Modifier = Modifier) {
    AnimatedVisibility(
        visible = texto != null,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically(),
        modifier = modifier
    ) {
        val fondo = if (esError) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.secondaryContainer
        val color = if (esError) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onSecondaryContainer
        Surface(shape = MaterialTheme.shapes.small, color = fondo, contentColor = color) {
            Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(if (esError) Icons.Filled.Warning else Icons.Filled.Info, contentDescription = null)
                Text(texto ?: "", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(start = 12.dp))
            }
        }
    }
}

@Composable
fun BannerSinConexion(sinConexion: Boolean, ultimaActualizacion: LocalDateTime?) {
    val desde = ultimaActualizacion?.let { "Última actualización: ${Formatos.fechaHora(it)}." } ?: ""
    MensajeAnimado(
        texto = if (sinConexion) "Sin conexión. Te mostramos la última información que tenemos. $desde".trim() else null,
        esError = false,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@Composable
fun DialogoConfirmar(
    titulo: String,
    texto: String,
    textoConfirmar: String,
    alConfirmar: () -> Unit,
    alCancelar: () -> Unit
) {
    AlertDialog(
        onDismissRequest = alCancelar,
        title = { Text(titulo) },
        text = { Text(texto, style = MaterialTheme.typography.bodyLarge) },
        confirmButton = { TextButton(onClick = alConfirmar) { Text(textoConfirmar) } },
        dismissButton = { TextButton(onClick = alCancelar) { Text("Cancelar") } }
    )
}

@Composable
fun AccionSalir(alClick: () -> Unit) {
    IconButton(onClick = alClick, modifier = Modifier.size(48.dp)) {
        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Cambiar de cuenta")
    }
}
