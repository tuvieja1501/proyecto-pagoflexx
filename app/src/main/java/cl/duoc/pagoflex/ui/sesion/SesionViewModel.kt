package cl.duoc.pagoflex.ui.sesion

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.pagoflex.AppContainer
import cl.duoc.pagoflex.data.db.Usuario
import cl.duoc.pagoflex.domain.AppClock
import cl.duoc.pagoflex.domain.RolIds
import cl.duoc.pagoflex.util.AutenticacionDispositivo
import cl.duoc.pagoflex.util.ProgramadorAvisos
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDateTime

data class SesionUi(
    val cargando: Boolean = true,
    val usuario: Usuario? = null,
    val bloqueado: Boolean = false
)

class SesionViewModel(
    private val contenedor: AppContainer,
    private val contexto: Context
) : ViewModel() {

    companion object {

        const val SEGUNDOS_PARA_BLOQUEAR = 60L
    }

    private val _ui = MutableStateFlow(SesionUi())
    val ui: StateFlow<SesionUi> = _ui.asStateFlow()

    val usuarios: StateFlow<List<Usuario>> = contenedor.base.usuarios
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val enLinea: StateFlow<Boolean> = contenedor.conectividad.enLinea

    private val _ultimaActualizacion = MutableStateFlow<LocalDateTime?>(null)
    val ultimaActualizacion: StateFlow<LocalDateTime?> = _ultimaActualizacion.asStateFlow()

    private var momentoSegundoPlano: Long? = null

    init {
        viewModelScope.launch {
            contenedor.asegurarInicializado()
            val id = contenedor.preferencias.sesionUsuarioId
            val persona = id?.let { contenedor.base.usuario(it) }?.takeIf { it.activo }
            if (persona == null) contenedor.preferencias.sesionUsuarioId = null
            _ui.value = SesionUi(
                cargando = false,
                usuario = persona,
                bloqueado = persona != null && debeBloquear(persona)
            )
            if (persona != null) refrescar()
        }
        viewModelScope.launch {
            contenedor.conectividad.enLinea.collect { conectado ->
                if (conectado) _ui.value.usuario?.let { registrarActualizacion(it.idUsuario) }
            }
        }
    }

    private fun debeBloquear(persona: Usuario): Boolean =
        persona.idRol == RolIds.USUARIO_FINAL && AutenticacionDispositivo.disponible(contexto)

    private fun registrarActualizacion(idUsuario: Int) {
        val ahora = AppClock.ahora()
        contenedor.preferencias.guardarUltimaActualizacion(idUsuario, ahora)
        _ultimaActualizacion.value = ahora
    }

    fun refrescar() {
        val persona = _ui.value.usuario ?: return
        viewModelScope.launch {
            contenedor.base.actualizarVencimientos()
            if (contenedor.conectividad.hayConexion()) registrarActualizacion(persona.idUsuario)
            else _ultimaActualizacion.value = contenedor.preferencias.ultimaActualizacion(persona.idUsuario)
            ProgramadorAvisos.revisarAhora(contexto)
        }
    }

    fun iniciar(persona: Usuario) {
        if (!persona.activo) return
        contenedor.preferencias.sesionUsuarioId = persona.idUsuario
        _ui.value = SesionUi(cargando = false, usuario = persona, bloqueado = false)
        refrescar()
    }

    fun cerrarSesion() {
        contenedor.preferencias.sesionUsuarioId = null
        _ultimaActualizacion.value = null
        _ui.value = SesionUi(cargando = false)
    }

    fun desbloquear() {
        _ui.value = _ui.value.copy(bloqueado = false)
        refrescar()
    }

    fun alIrASegundoPlano() {
        momentoSegundoPlano = System.currentTimeMillis()
    }

    fun alVolverAPrimerPlano() {
        val desde = momentoSegundoPlano ?: return
        momentoSegundoPlano = null
        val persona = _ui.value.usuario ?: return
        val segundos = (System.currentTimeMillis() - desde) / 1000
        if (segundos >= SEGUNDOS_PARA_BLOQUEAR && debeBloquear(persona)) {
            _ui.value = _ui.value.copy(bloqueado = true)
        } else {
            refrescar()
        }
    }
}
