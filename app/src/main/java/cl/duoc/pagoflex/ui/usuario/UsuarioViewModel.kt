package cl.duoc.pagoflex.ui.usuario

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.pagoflex.AppContainer
import cl.duoc.pagoflex.data.CompromisoVista
import cl.duoc.pagoflex.data.ItemHistorial
import cl.duoc.pagoflex.data.ReporteVista
import cl.duoc.pagoflex.data.Resultado
import cl.duoc.pagoflex.data.db.Usuario
import cl.duoc.pagoflex.domain.AppClock
import cl.duoc.pagoflex.domain.EstadoCompromiso
import cl.duoc.pagoflex.domain.Rules
import cl.duoc.pagoflex.util.ProgramadorAvisos
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.YearMonth

data class InicioUi(
    val cargando: Boolean = true,
    val nombre: String = "",
    val vigentes: List<CompromisoVista> = emptyList(),
    val totalMes: Int = 0,
    val proximo: CompromisoVista? = null
)

data class FiltrosHistorial(val idEmpresa: Int = 0, val anio: Int = 0, val mes: Int = 0)

data class HistorialUi(
    val items: List<ItemHistorial> = emptyList(),
    val empresas: List<Pair<Int, String>> = emptyList(),
    val anios: List<Int> = emptyList(),
    val filtros: FiltrosHistorial = FiltrosHistorial()
)

class UsuarioViewModel(contenedor: AppContainer, private val idUsuario: Int) : ViewModel() {

    private val repo = contenedor.usuarioFinal
    private val base = contenedor.base
    private val appContext = contenedor

    val persona: StateFlow<Usuario?> = base.observarUsuario(idUsuario)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val inicio: StateFlow<InicioUi> = combine(repo.observarCompromisos(idUsuario), base.observarUsuario(idUsuario)) { lista, u ->
        val hoy = AppClock.hoy()
        val vigentes = lista
            .filter { it.idEstado == EstadoCompromiso.PENDIENTE || it.idEstado == EstadoCompromiso.VENCIDO || it.idEstado == EstadoCompromiso.EN_REVISION }
            .sortedBy { it.vencimiento }
        val finMes = YearMonth.from(hoy).atEndOfMonth()
        val totalMes = vigentes
            .filter { (it.idEstado == EstadoCompromiso.PENDIENTE || it.idEstado == EstadoCompromiso.VENCIDO) && !it.vencimiento.isAfter(finMes) }
            .sumOf { it.totalAPagar }
        val proximo = vigentes
            .filter { it.idEstado == EstadoCompromiso.PENDIENTE && !it.vencimiento.isBefore(hoy) }
            .minByOrNull { it.vencimiento }
        InicioUi(false, u?.nombres?.substringBefore(' ') ?: "", vigentes, totalMes, proximo)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), InicioUi())

    private val filtros = MutableStateFlow(FiltrosHistorial())

    val historial: StateFlow<HistorialUi> = combine(repo.observarHistorial(idUsuario), filtros) { todos, f ->
        val filtrados = todos.filter { item ->
            (f.idEmpresa == 0 || item.idEmpresa == f.idEmpresa) &&
                (f.anio == 0 || item.fecha.year == f.anio) &&
                (f.mes == 0 || item.fecha.monthValue == f.mes)
        }
        HistorialUi(
            items = filtrados,
            empresas = todos.map { it.idEmpresa to it.empresa }.distinct().sortedBy { it.second },
            anios = todos.map { it.fecha.year }.distinct().sortedDescending(),
            filtros = f
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HistorialUi())

    fun filtrarEmpresa(id: Int) { filtros.value = filtros.value.copy(idEmpresa = id) }
    fun filtrarAnio(anio: Int) { filtros.value = filtros.value.copy(anio = anio) }
    fun filtrarMes(mes: Int) { filtros.value = filtros.value.copy(mes = mes) }
    fun limpiarFiltros() { filtros.value = FiltrosHistorial() }

    val cumplimiento: StateFlow<Rules.Cumplimiento?> = repo.observarCumplimiento(idUsuario)
        .map<Rules.Cumplimiento, Rules.Cumplimiento?> { it }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val reportes: StateFlow<List<ReporteVista>> = repo.observarReportes(idUsuario)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _mensajeAvisos = MutableStateFlow<String?>(null)
    val mensajeAvisos: StateFlow<String?> = _mensajeAvisos.asStateFlow()

    private val _avisosError = MutableStateFlow<String?>(null)
    val avisosError: StateFlow<String?> = _avisosError.asStateFlow()

    fun guardarAvisos(activos: Boolean, dias: Int) {
        viewModelScope.launch {
            when (val r = repo.guardarAvisos(idUsuario, activos, dias)) {
                is Resultado.Exito -> {
                    _avisosError.value = null
                    _mensajeAvisos.value = if (activos) "Listo, te avisaremos $dias día(s) antes de cada vencimiento." else "Avisos desactivados."
                    appContext.preferencias.permisoNotificacionesSolicitado = true
                }
                is Resultado.Fallo -> {
                    _mensajeAvisos.value = null
                    _avisosError.value = r.mensaje
                }
            }
        }
    }

    fun revisarAvisosAhora(contexto: android.content.Context) = ProgramadorAvisos.revisarAhora(contexto)

    fun limpiarMensajesAvisos() {
        _mensajeAvisos.value = null
        _avisosError.value = null
    }
}
