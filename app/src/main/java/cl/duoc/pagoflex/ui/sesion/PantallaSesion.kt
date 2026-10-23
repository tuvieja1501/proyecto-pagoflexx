package cl.duoc.pagoflex.ui.sesion

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cl.duoc.pagoflex.data.db.Usuario
import cl.duoc.pagoflex.domain.RolIds
import cl.duoc.pagoflex.domain.Rut
import cl.duoc.pagoflex.ui.componentes.BarraSuperior
import cl.duoc.pagoflex.ui.componentes.BotonGrande
import cl.duoc.pagoflex.ui.componentes.TituloSeccion
import cl.duoc.pagoflex.ui.contenedorActual

@Composable
fun PantallaSesion(usuarios: List<Usuario>, alElegir: (Usuario) -> Unit) {
    val empresas = contenedorActual().nucleo.cat.empresas
    Scaffold(topBar = { BarraSuperior("Elige tu cuenta") }) { relleno ->
        LazyColumn(
            Modifier.fillMaxSize().padding(relleno),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Text(
                    "Todos los datos de esta app son ficticios. Toca una cuenta para entrar; no necesitas contraseña.",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            val secciones = listOf(
                RolIds.USUARIO_FINAL to "Personas que pagan",
                RolIds.AGENTE to "Agentes PagoFlex",
                RolIds.EJECUTIVO to "Ejecutivos de empresas"
            )
            secciones.forEach { (rol, titulo) ->
                item(key = "titulo_$rol") { TituloSeccion(titulo) }
                items(usuarios.filter { it.idRol == rol }, key = { it.idUsuario }) { u ->
                    val detalle = when (rol) {
                        RolIds.USUARIO_FINAL -> "RUT ${Rut.enmascarar(u.rut)}"
                        RolIds.AGENTE -> "Agente de atención"
                        else -> listOfNotNull(u.cargo, u.idEmpresa?.let { empresas[it]?.nombreFantasia }).joinToString(" · ")
                    }
                    FilaUsuario(u, detalle, alElegir)
                }
            }
            item { Spacer(Modifier.height(8.dp)) }
        }
    }
}

@Composable
private fun FilaUsuario(u: Usuario, detalle: String, alElegir: (Usuario) -> Unit) {
    Card(
        onClick = { alElegir(u) },
        enabled = u.activo,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Person, contentDescription = null, modifier = Modifier.size(32.dp), tint = MaterialTheme.colorScheme.primary)
            Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                Text(u.nombreCompleto, style = MaterialTheme.typography.titleMedium)
                Text(
                    if (u.activo) detalle else "Cuenta inactiva · no puede entrar",
                    style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
        }
    }
}

@Composable
fun PantallaBloqueo(alIntentar: () -> Unit, alCerrarSesion: () -> Unit) {
    androidx.compose.runtime.LaunchedEffect(Unit) { alIntentar() }
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Filled.Lock, contentDescription = null, modifier = Modifier.size(72.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(16.dp))
        Text("Tu información está protegida", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(8.dp))
        Text(
            "Confirma que eres tú con tu huella, tu rostro o el PIN del teléfono.",
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(Modifier.height(24.dp))
        BotonGrande("Desbloquear", alIntentar)
        Spacer(Modifier.height(12.dp))
        androidx.compose.material3.TextButton(onClick = alCerrarSesion) { Text("Cambiar de cuenta") }
    }
}
