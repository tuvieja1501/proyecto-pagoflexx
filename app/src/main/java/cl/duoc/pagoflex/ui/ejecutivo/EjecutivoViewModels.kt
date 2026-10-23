package cl.duoc.pagoflex.ui.ejecutivo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.pagoflex.AppContainer
import cl.duoc.pagoflex.data.DatosCompromiso
import cl.duoc.pagoflex.data.DetalleEjecutivo
import cl.duoc.pagoflex.data.FilaEjecutivo
import cl.duoc.pagoflex.data.RepositorioEjecutivo
import cl.duoc.pagoflex.data.Resultado
import cl.duoc.pagoflex.data.db.Usuario
import cl.duoc.pagoflex.domain.AppClock
import cl.duoc.pagoflex.domain.Formatos
import cl.duoc.pagoflex.domain.MotivoAnulacionIds
import cl.duoc.pagoflex.domain.Rules
import cl.duoc.pagoflex.domain.Rut
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.YearMonth

data class ListaEjecutivoUi(
    val cargando: Boolean = true,
    val empresa: String = "",
    val filas: List<FilaEjecutivo> = emptyList(),
    val estados: List<Pair<Int, String>> = emptyList(),
    val meses: List<Pair<Int, String>> = emptyList(),
    val filtroEstado: Int = 0,

    val filtroMes: Int = 0
)

class EjecutivoViewModel(private val contenedor: AppContainer, private val idEmpresa: Int) : ViewModel() {

    private val repo = contenedor.ejecutivo
    private val estado = MutableStateFlow(0)
    private val mes = MutableStateFlow(0)

    val lista: StateFlow<ListaEjecutivoUi> = combine(repo.observarFilas(idEmpresa), estado, mes) { todas, e, m ->
        val ordenadas = todas.sortedWith(compareBy({ it.compromiso.vencimiento }, { it.compromiso.id }))
        ListaEjecutivoUi(
            cargando = false,
            empresa = contenedor.nucleo.cat.empresas[idEmpresa]?.nombreFantasia ?: "",
            filas = ordenadas.filter { f ->
                (e == 0 || f.compromiso.idEstado == e) &&
                    (m == 0 || f.compromiso.vencimiento.year * 100 + f.compromiso.vencimiento.monthValue == m)
            },
            estados = contenedor.nucleo.cat.estadosCompromiso.map { it.key to it.value },
            meses = todas.map { it.compromiso.vencimiento }
                .map { it.year * 100 + it.monthValue }.distinct().sortedDescending()
                .map { it to Formatos.mesAnio(it / 100, it % 100) },
            filtroEstado = e, filtroMes = m
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ListaEjecutivoUi())

    fun filtrarEstado(id: Int) { estado.value = id }
    fun filtrarMes(valor: Int) { mes.value = valor }

    private val _mesCobranza = MutableStateFlow(YearMonth.from(AppClock.hoy()))
    val mesCobranza: StateFlow<YearMonth> = _mesCobranza.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val cobranza: StateFlow<Rules.Cobranza?> = _mesCobranza
        .flatMapLatest { repo.observarCobranza(idEmpresa, it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun mesAnterior() { _mesCobranza.value = _mesCobranza.value.minusMonths(1) }
    fun mesSiguiente() { _mesCobranza.value = _mesCobranza.value.plusMonths(1) }
}

data class DetalleEjecutivoUi(
    val cargado: Boolean = false,
    val detalle: DetalleEjecutivo? = null,
    val motivos: List<Pair<Int, String>> = emptyList(),
    val mensaje: String? = null,
    val error: String? = null,
    val errorMotivo: String? = null,
    val errorComentario: String? = null,
    val trabajando: Boolean = false
)

class DetalleEjecutivoViewModel(
    contenedor: AppContainer, private val idEjecutivo: Int, idEmpresa: Int, private val idCompromiso: Int
) : ViewModel() {

    private val repo = contenedor.ejecutivo
    private val _ui = MutableStateFlow(
        DetalleEjecutivoUi(motivos = contenedor.nucleo.cat.motivosAnulacion.map { it.idMotivoAnulacion to it.nombre })
    )
    val ui: StateFlow<DetalleEjecutivoUi> = _ui.asStateFlow()

    init {
        viewModelScope.launch {
            repo.observarDetalle(idCompromiso, idEmpresa).collect { d ->
                _ui.value = _ui.value.copy(cargado = true, detalle = d)
            }
        }
    }

    val maxComentario: Int get() = RepositorioEjecutivo.MAX_COMENTARIO_ANULACION

    fun limpiarErrores() {
        _ui.value = _ui.value.copy(error = null, errorMotivo = null, errorComentario = null)
    }

    fun anular(idMotivo: Int?, comentario: String, alTerminar: (Boolean) -> Unit) {
        if (idMotivo == null) {
            _ui.value = _ui.value.copy(errorMotivo = "Elige el motivo de la anulación.")
            alTerminar(false); return
        }
        if (idMotivo == MotivoAnulacionIds.OTRO && comentario.isBlank()) {
            _ui.value = _ui.value.copy(errorComentario = "Cuando el motivo es «Otro», escribe un comentario.")
            alTerminar(false); return
        }
        _ui.value = _ui.value.copy(trabajando = true, error = null, errorMotivo = null, errorComentario = null)
        viewModelScope.launch {
            when (val r = repo.anularCompromiso(idEjecutivo, idCompromiso, idMotivo, comentario)) {
                is Resultado.Exito -> {
                    _ui.value = _ui.value.copy(trabajando = false, mensaje = "El compromiso quedó anulado.")
                    alTerminar(true)
                }
                is Resultado.Fallo -> {
                    _ui.value = _ui.value.copy(trabajando = false, error = r.mensaje)
                    alTerminar(false)
                }
            }
        }
    }
}

data class RegistroUi(
    val rut: String = "",
    val nombres: String = "",
    val apellidos: String = "",
    val correo: String = "",
    val telefono: String = "",
    val concepto: String = "",
    val monto: String = "",
    val vencimiento: String = "",
    val cuota: String = "",
    val totalCuotas: String = "",

    val persona: Usuario? = null,
    val rutConsultado: Boolean = false,
    val errores: Map<String, String> = emptyMap(),
    val error: String? = null,
    val guardando: Boolean = false,
    val exito: String? = null
) {

    val pedirDatosPersona: Boolean get() = rutConsultado && persona == null
}

class RegistroViewModel(contenedor: AppContainer, private val idEjecutivo: Int) : ViewModel() {

    private val repo = contenedor.ejecutivo
    private val _ui = MutableStateFlow(RegistroUi())
    val ui: StateFlow<RegistroUi> = _ui.asStateFlow()

    val maxConcepto: Int get() = RepositorioEjecutivo.MAX_CONCEPTO

    private fun editar(campo: String, cambio: (RegistroUi) -> RegistroUi) {
        val actual = cambio(_ui.value)
        _ui.value = actual.copy(errores = actual.errores - campo, error = null, exito = null)
    }

    fun cambiarRut(texto: String) {
        editar("rut") { it.copy(rut = texto, persona = null, rutConsultado = false) }
        if (Rut.esValido(texto)) {
            viewModelScope.launch {
                val persona = repo.buscarPersona(texto)
                if (_ui.value.rut == texto) _ui.value = _ui.value.copy(persona = persona, rutConsultado = true)
            }
        }
    }

    fun cambiarNombres(t: String) = editar("nombres") { it.copy(nombres = t) }
    fun cambiarApellidos(t: String) = editar("apellidos") { it.copy(apellidos = t) }
    fun cambiarCorreo(t: String) = editar("correo") { it.copy(correo = t) }
    fun cambiarTelefono(t: String) = editar("telefono") { it.copy(telefono = t) }
    fun cambiarConcepto(t: String) = editar("concepto") { it.copy(concepto = t) }
    fun cambiarMonto(t: String) = editar("monto") { it.copy(monto = t.filter { c -> c.isDigit() }) }
    fun cambiarVencimiento(t: String) = editar("vencimiento") { it.copy(vencimiento = t) }
    fun cambiarCuota(t: String) = editar("cuota") { it.copy(cuota = t.filter { c -> c.isDigit() }) }
    fun cambiarTotalCuotas(t: String) = editar("total") { it.copy(totalCuotas = t.filter { c -> c.isDigit() }) }

    private fun validar(u: RegistroUi): Map<String, String> {
        val e = mutableMapOf<String, String>()
        if (u.rut.isBlank()) e["rut"] = "Escribe el RUT de la persona."
        else if (!Rut.esValido(u.rut)) e["rut"] = "El RUT no es válido. Revisa el número y el dígito verificador."
        if (u.pedirDatosPersona) {
            if (u.nombres.isBlank()) e["nombres"] = "Escribe los nombres."
            if (u.apellidos.isBlank()) e["apellidos"] = "Escribe los apellidos."
            if (!RepositorioEjecutivo.correoValido(u.correo)) e["correo"] = "Escribe un correo válido, por ejemplo nombre@correo.cl."
            if (!RepositorioEjecutivo.telefonoValido(u.telefono)) e["telefono"] = "Escribe un teléfono válido, por ejemplo +56 9 1234 5678."
        }
        val concepto = u.concepto.trim()
        if (concepto.isEmpty()) e["concepto"] = "Escribe el concepto del pago."
        else if (concepto.length > RepositorioEjecutivo.MAX_CONCEPTO) e["concepto"] = "Máximo ${RepositorioEjecutivo.MAX_CONCEPTO} letras."
        val monto = u.monto.toIntOrNull()
        if (monto == null || monto <= 0) e["monto"] = "El monto debe ser mayor que cero."
        val fecha = Formatos.parseFecha(u.vencimiento)
        if (fecha == null) e["vencimiento"] = "Escribe la fecha como DD-MM-AAAA."
        else if (fecha.isBefore(AppClock.hoy())) e["vencimiento"] = "La fecha no puede ser anterior a hoy."
        val cuota = u.cuota.toIntOrNull()
        val total = u.totalCuotas.toIntOrNull()
        if (u.cuota.isNotEmpty() != u.totalCuotas.isNotEmpty()) {
            if (u.cuota.isEmpty()) e["cuota"] = "Indica el número de cuota o deja ambos campos vacíos."
            else e["total"] = "Indica el total de cuotas o deja ambos campos vacíos."
        } else if (cuota != null && total != null) {
            if (cuota < 1) e["cuota"] = "La cuota debe ser 1 o más."
            if (total < 1) e["total"] = "El total debe ser 1 o más."
            else if (cuota > total) e["cuota"] = "La cuota no puede ser mayor que el total."
        }
        return e
    }

    fun registrar() {
        val u = _ui.value
        if (u.guardando) return
        val errores = validar(u)
        if (errores.isNotEmpty()) {
            _ui.value = u.copy(errores = errores, error = "Revisa los campos marcados.", exito = null)
            return
        }
        _ui.value = u.copy(guardando = true, error = null, exito = null)
        viewModelScope.launch {
            val datos = DatosCompromiso(
                rut = u.rut, nombres = u.nombres, apellidos = u.apellidos, correo = u.correo, telefono = u.telefono,
                concepto = u.concepto, monto = u.monto.toInt(), vencimiento = Formatos.parseFecha(u.vencimiento)!!,
                cuota = u.cuota.toIntOrNull(), totalCuotas = u.totalCuotas.toIntOrNull()
            )
            when (val r = repo.registrarCompromiso(idEjecutivo, datos)) {
                is Resultado.Exito -> _ui.value = RegistroUi(
                    exito = "Registrado: ${r.valor.folio} · ${r.valor.concepto} por ${Formatos.pesos(r.valor.monto)}."
                )
                is Resultado.Fallo -> _ui.value = _ui.value.copy(guardando = false, error = r.mensaje)
            }
        }
    }
}
