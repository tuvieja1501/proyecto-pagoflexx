package cl.duoc.pagoflex.ui.usuario

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.pagoflex.AppContainer
import cl.duoc.pagoflex.data.CompromisoVista
import cl.duoc.pagoflex.data.ComprobanteVista
import cl.duoc.pagoflex.data.ReporteVista
import cl.duoc.pagoflex.data.Resultado
import cl.duoc.pagoflex.domain.MedioPagoIds
import cl.duoc.pagoflex.domain.MotivoReporteIds
import cl.duoc.pagoflex.data.RepositorioUsuarioFinal
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class DetalleUi(
    val cargado: Boolean = false,
    val compromiso: CompromisoVista? = null,
    val reporte: ReporteVista? = null,

    val motivoNoPagable: String? = null
)

class DetalleCompromisoViewModel(
    contenedor: AppContainer, private val idUsuario: Int, private val idCompromiso: Int
) : ViewModel() {
    private val repo = contenedor.usuarioFinal

    val ui: StateFlow<DetalleUi> = kotlinx.coroutines.flow.combine(
        repo.observarCompromiso(idCompromiso, idUsuario),
        repo.observarReporteDeCompromiso(idCompromiso, idUsuario)
    ) { c, r ->
        DetalleUi(
            cargado = true, compromiso = c, reporte = r,
            motivoNoPagable = if (c == null) null else repo.motivoNoPagable(idUsuario, idCompromiso)
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DetalleUi())
}

data class PagoUi(
    val compromiso: CompromisoVista? = null,
    val motivoNoPagable: String? = null,
    val medios: List<Pair<Int, String>> = emptyList(),
    val medioElegido: Int? = null,
    val errorMedio: String? = null,
    val error: String? = null,
    val procesando: Boolean = false,
    val comprobante: ComprobanteVista? = null
)

class PagoViewModel(
    private val contenedor: AppContainer, private val idUsuario: Int, private val idCompromiso: Int
) : ViewModel() {
    private val repo = contenedor.usuarioFinal

    private val _ui = MutableStateFlow(PagoUi())
    val ui: StateFlow<PagoUi> = _ui.asStateFlow()

    init {
        viewModelScope.launch {
            val medios = contenedor.nucleo.cat.medios
                .filter { it.idMedioPago != MedioPagoIds.PAGO_SIMULADO }
                .map { it.idMedioPago to it.nombre }
            repo.observarCompromiso(idCompromiso, idUsuario).collect { c ->
                val motivo = if (c == null) "No encontramos este compromiso." else repo.motivoNoPagable(idUsuario, idCompromiso)

                _ui.value = _ui.value.copy(
                    compromiso = c, medios = medios,
                    motivoNoPagable = if (_ui.value.comprobante != null) null else motivo
                )
            }
        }
    }

    fun elegirMedio(id: Int) {
        _ui.value = _ui.value.copy(medioElegido = id, errorMedio = null, error = null)
    }

    fun confirmar() {
        val actual = _ui.value
        if (actual.procesando || actual.comprobante != null) return
        val medio = actual.medioElegido
        if (medio == null) {
            _ui.value = actual.copy(errorMedio = "Elige cómo quieres pagar.")
            return
        }
        _ui.value = actual.copy(procesando = true, error = null)
        viewModelScope.launch {
            when (val r = repo.pagar(idUsuario, idCompromiso, medio)) {
                is Resultado.Exito -> _ui.value = _ui.value.copy(procesando = false, comprobante = r.valor, motivoNoPagable = null)
                is Resultado.Fallo -> _ui.value = _ui.value.copy(procesando = false, error = r.mensaje)
            }
        }
    }
}

data class ReporteFormUi(
    val compromiso: CompromisoVista? = null,
    val motivos: List<Pair<Int, String>> = emptyList(),
    val motivo: Int? = null,
    val comentario: String = "",
    val errorMotivo: String? = null,
    val errorComentario: String? = null,
    val error: String? = null,
    val enviando: Boolean = false,
    val enviado: ReporteVista? = null
)

class ReporteViewModel(
    contenedor: AppContainer, private val idUsuario: Int, private val idCompromiso: Int
) : ViewModel() {
    private val repo = contenedor.usuarioFinal

    private val _ui = MutableStateFlow(
        ReporteFormUi(motivos = contenedor.nucleo.cat.motivosReporte.map { it.idMotivoReporte to it.nombre })
    )
    val ui: StateFlow<ReporteFormUi> = _ui.asStateFlow()

    init {
        viewModelScope.launch {
            repo.observarCompromiso(idCompromiso, idUsuario).collect { c ->
                _ui.value = _ui.value.copy(compromiso = c)
            }
        }
    }

    fun elegirMotivo(id: Int) {
        _ui.value = _ui.value.copy(motivo = id, errorMotivo = null, errorComentario = null, error = null)
    }

    fun cambiarComentario(texto: String) {
        _ui.value = _ui.value.copy(comentario = texto, errorComentario = null, error = null)
    }

    fun enviar() {
        val actual = _ui.value
        if (actual.enviando || actual.enviado != null) return
        val motivo = actual.motivo
        var errorMotivo: String? = null
        var errorComentario: String? = null
        if (motivo == null) errorMotivo = "Elige el motivo de tu reporte."
        else if (motivo == MotivoReporteIds.OTRO && actual.comentario.isBlank()) {
            errorComentario = "Cuéntanos qué pasó para poder ayudarte."
        }
        if (motivo == null || errorComentario != null) {
            _ui.value = actual.copy(errorMotivo = errorMotivo, errorComentario = errorComentario)
            return
        }
        _ui.value = actual.copy(enviando = true, error = null)
        viewModelScope.launch {
            when (val r = repo.reportarProblema(idUsuario, idCompromiso, motivo, actual.comentario)) {
                is Resultado.Exito -> _ui.value = _ui.value.copy(enviando = false, enviado = r.valor)
                is Resultado.Fallo -> _ui.value = _ui.value.copy(enviando = false, error = r.mensaje)
            }
        }
    }

    val maxComentario: Int get() = RepositorioUsuarioFinal.MAX_COMENTARIO_REPORTE
}

class ComprobanteViewModel(
    contenedor: AppContainer, idUsuario: Int, idCompromiso: Int
) : ViewModel() {
    val comprobante: StateFlow<ComprobanteVista?> = contenedor.usuarioFinal
        .observarComprobante(idCompromiso, idUsuario)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
}
