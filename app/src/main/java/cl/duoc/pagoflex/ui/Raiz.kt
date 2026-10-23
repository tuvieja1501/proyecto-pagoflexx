package cl.duoc.pagoflex.ui

import androidx.compose.runtime.getValue
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.duoc.pagoflex.domain.RolIds
import cl.duoc.pagoflex.ui.agente.NavegacionAgente
import cl.duoc.pagoflex.ui.componentes.PantallaCarga
import cl.duoc.pagoflex.ui.ejecutivo.NavegacionEjecutivo
import cl.duoc.pagoflex.ui.sesion.PantallaBloqueo
import cl.duoc.pagoflex.ui.sesion.PantallaSesion
import cl.duoc.pagoflex.ui.sesion.SesionViewModel
import cl.duoc.pagoflex.ui.usuario.NavegacionUsuario
import cl.duoc.pagoflex.util.AutenticacionDispositivo

private enum class Fase { CARGANDO, SESION, BLOQUEO, APP }

@Composable
fun RaizApp(actividad: FragmentActivity, vm: SesionViewModel) {
    val ui by vm.ui.collectAsStateWithLifecycle()
    val usuarios by vm.usuarios.collectAsStateWithLifecycle()
    val persona = ui.usuario
    val fase = when {
        ui.cargando -> Fase.CARGANDO
        persona == null -> Fase.SESION
        ui.bloqueado -> Fase.BLOQUEO
        else -> Fase.APP
    }
    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        AnimatedContent(
            targetState = fase,
            transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(200)) },
            label = "fase"
        ) { actual ->
            when (actual) {
                Fase.CARGANDO -> PantallaCarga()
                Fase.SESION -> PantallaSesion(usuarios, vm::iniciar)
                Fase.BLOQUEO -> PantallaBloqueo(
                    alIntentar = {
                        AutenticacionDispositivo.solicitar(
                            actividad, "Desbloquea PagoFlex", "Así nadie más ve ni paga tus compromisos",
                            alExito = vm::desbloquear, alFallar = { }
                        )
                    },
                    alCerrarSesion = vm::cerrarSesion
                )
                Fase.APP -> if (persona != null) {
                    key(persona.idUsuario) {
                        when (persona.idRol) {
                            RolIds.USUARIO_FINAL -> NavegacionUsuario(persona, vm)
                            RolIds.AGENTE -> NavegacionAgente(persona, vm)
                            else -> NavegacionEjecutivo(persona, vm)
                        }
                    }
                } else Box(Modifier.fillMaxSize())
            }
        }
    }
}
