package cl.duoc.pagoflex.ui.componentes

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Warning
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import cl.duoc.pagoflex.domain.Formatos
import java.time.Instant
import java.time.ZoneOffset

private fun iconoDeError(error: String?): (@Composable () -> Unit)? =
    if (error != null) {
        {
            Icon(Icons.Filled.Warning, contentDescription = "Error en el campo", tint = MaterialTheme.colorScheme.error)
        }
    } else null

@Composable
fun CampoValidado(
    valor: String,
    alCambiar: (String) -> Unit,
    etiqueta: String,
    error: String?,
    modifier: Modifier = Modifier,
    teclado: KeyboardType = KeyboardType.Text,
    varias: Boolean = false,
    ayuda: String? = null,
    maxCaracteres: Int? = null,
    icono: ImageVector? = null
) {
    OutlinedTextField(
        value = valor,
        onValueChange = { nuevo -> if (maxCaracteres == null || nuevo.length <= maxCaracteres) alCambiar(nuevo) },
        label = { Text(etiqueta) },
        isError = error != null,
        supportingText = {
            val texto = error ?: ayuda
            if (texto != null) Text(texto)
        },
        leadingIcon = icono?.let { ic -> { Icon(ic, contentDescription = null) } },
        trailingIcon = iconoDeError(error),
        singleLine = !varias,
        minLines = if (varias) 3 else 1,
        keyboardOptions = KeyboardOptions(keyboardType = teclado),
        modifier = modifier.fillMaxWidth()
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectorDesplegable(
    etiqueta: String,
    opciones: List<Pair<Int, String>>,
    seleccion: Int?,
    alSeleccionar: (Int) -> Unit,
    error: String?,
    modifier: Modifier = Modifier
) {
    var abierto by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = abierto, onExpandedChange = { abierto = it }, modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = opciones.firstOrNull { it.first == seleccion }?.second ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text(etiqueta) },
            isError = error != null,
            supportingText = { if (error != null) Text(error) },
            leadingIcon = iconoDeError(error),
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = abierto) },
            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth()
        )
        ExposedDropdownMenu(expanded = abierto, onDismissRequest = { abierto = false }) {
            opciones.forEach { (id, nombre) ->
                DropdownMenuItem(
                    text = { Text(nombre, style = androidx.compose.material3.MaterialTheme.typography.bodyLarge) },
                    onClick = { alSeleccionar(id); abierto = false }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CampoFecha(
    valor: String,
    alCambiar: (String) -> Unit,
    etiqueta: String,
    error: String?,
    modifier: Modifier = Modifier
) {
    var calendario by remember { mutableStateOf(false) }
    OutlinedTextField(
        value = valor,
        onValueChange = { if (it.length <= 10) alCambiar(it) },
        label = { Text(etiqueta) },
        placeholder = { Text("DD-MM-AAAA") },
        isError = error != null,
        supportingText = { if (error != null) Text(error) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        leadingIcon = iconoDeError(error),
        trailingIcon = {
            IconButton(onClick = { calendario = true }) {
                Icon(Icons.Filled.DateRange, contentDescription = "Elegir fecha en el calendario")
            }
        },
        modifier = modifier.fillMaxWidth()
    )
    if (calendario) {
        val estado = rememberDatePickerState(
            initialSelectedDateMillis = Formatos.parseFecha(valor)?.atStartOfDay(ZoneOffset.UTC)?.toInstant()?.toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { calendario = false },
            confirmButton = {
                TextButton(onClick = {
                    estado.selectedDateMillis?.let { ms ->
                        alCambiar(Formatos.fecha(Instant.ofEpochMilli(ms).atZone(ZoneOffset.UTC).toLocalDate()))
                    }
                    calendario = false
                }) { Text("Aceptar") }
            },
            dismissButton = { TextButton(onClick = { calendario = false }) { Text("Cancelar") } }
        ) { DatePicker(state = estado) }
    }
}
