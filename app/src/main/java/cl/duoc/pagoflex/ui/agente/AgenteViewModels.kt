package cl.duoc.pagoflex.ui.agente

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.pagoflex.AppContainer
import cl.duoc.pagoflex.data.ReporteVista
import cl.duoc.pagoflex.data.RepositorioAgente
import cl.duoc.pagoflex.data.Resultado
import cl.duoc.pagoflex.domain.EstadoReporte
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class BandejaUi(
    val cargando: Boolean = true,
    val items: List<ReporteVista> = emptyList(),

    val filtro: Int = 0,
    val atrasados: Int = 0
)

class BandejaViewModel(contenedor: AppContainer) : ViewModel() {

    private val filtro = MutableStateFlow(0)

    val ui: StateFlow<BandejaUi> = combine(contenedor.agente.observarReportes(), filtro) { todos, f ->
        val orden = compareBy<ReporteVista>(
            { if (it.atrasado) 0 else 1 },
            { when (it.idEstado) { EstadoReporte.ABIERTO -> 0; EstadoReporte.EN_GESTION -> 1; else -> 2 } },
            { it.fechaCreacion }
        )
        BandejaUi(
            cargando = false,
            items = todos.filter { f == 0 || it.idEstado == f }.sortedWith(orden),
            filtro = f,
            atrasados = todos.count { it.atrasado }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), BandejaUi())

    fun filtrar(idEstado: Int) { filtro.value = idEstado }
}

data class ReporteAgenteUi(
    val cargado: Boolean = false,
    val reporte: ReporteVista? = null,
    val respuesta: String = "",
    val errorRespuesta: String? = null,
    val error: String? = null,
    val mensaje: String? = null,
    val trabajando: Boolean = false
)

class ReporteAgenteViewModel(
    contenedor: AppContainer, private val idAgente: Int, private val idReporte: Int
) : ViewModel() {

    private val repo = contenedor.agente
    private val _ui = MutableStateFlow(ReporteAgenteUi())
    val ui: StateFlow<ReporteAgenteUi> = _ui.asStateFlow()

    init {
        viewModelScope.launch {
            repo.observarReporte(idReporte).collect { r ->
                _ui.value = _ui.value.copy(cargado = true, reporte = r)
            }
        }
    }

    fun cambiarRespuesta(texto: String) {
        _ui.value = _ui.value.copy(respuesta = texto, errorRespuesta = null, error = null)
    }

    fun tomar() {
        if (_ui.value.trabajando) return
        _ui.value = _ui.value.copy(trabajando = true, error = null, mensaje = null)
        viewModelScope.launch {
            when (val r = repo.tomar(idAgente, idReporte)) {
                is Resultado.Exito -> _ui.value = _ui.value.copy(trabajando = false, mensaje = "Tomaste este reporte. Ahora puedes resolverlo.")
                is Resultado.Fallo -> _ui.value = _ui.value.copy(trabajando = false, error = r.mensaje)
            }
        }
    }

    fun resolver() {
        val actual = _ui.value
        if (actual.trabajando) return
        if (actual.respuesta.isBlank()) {
            _ui.value = actual.copy(errorRespuesta = "Escribe la respuesta para la persona antes de resolver.")
            return
        }
        _ui.value = actual.copy(trabajando = true, error = null, mensaje = null)
        viewModelScope.launch {
            when (val r = repo.resolver(idAgente, idReporte, actual.respuesta)) {
                is Resultado.Exito -> _ui.value = _ui.value.copy(
                    trabajando = false, respuesta = "",
                    mensaje = "Reporte resuelto. El compromiso quedó ${r.valor.lowercase()}."
                )
                is Resultado.Fallo -> _ui.value = _ui.value.copy(trabajando = false, error = r.mensaje)
            }
        }
    }

    val maxRespuesta: Int get() = RepositorioAgente.MAX_RESPUESTA
}
