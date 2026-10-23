package cl.duoc.pagoflex.util

import android.content.Context
import cl.duoc.pagoflex.domain.TipoAviso
import java.time.LocalDate
import java.time.LocalDateTime

class Preferencias(contexto: Context) {

    private val sp = contexto.getSharedPreferences("pagoflex_preferencias", Context.MODE_PRIVATE)

    var sesionUsuarioId: Int?
        get() = sp.getInt(CLAVE_SESION, -1).takeIf { it > 0 }
        set(valor) {
            sp.edit().apply { if (valor == null) remove(CLAVE_SESION) else putInt(CLAVE_SESION, valor) }.apply()
        }

    var permisoNotificacionesSolicitado: Boolean
        get() = sp.getBoolean(CLAVE_PERMISO, false)
        set(valor) = sp.edit().putBoolean(CLAVE_PERMISO, valor).apply()

    fun ultimaActualizacion(idUsuario: Int): LocalDateTime? =
        sp.getString("ultima_actualizacion_$idUsuario", null)?.let { runCatching { LocalDateTime.parse(it) }.getOrNull() }

    fun guardarUltimaActualizacion(idUsuario: Int, momento: LocalDateTime) {
        sp.edit().putString("ultima_actualizacion_$idUsuario", momento.toString()).apply()
    }

    fun yaAvisado(idCompromiso: Int, tipo: TipoAviso): Boolean =
        sp.getBoolean("aviso_${idCompromiso}_${tipo.name}", false)

    fun fechaUltimoAviso(idCompromiso: Int): LocalDate? =
        sp.getString("aviso_fecha_$idCompromiso", null)?.let { runCatching { LocalDate.parse(it) }.getOrNull() }

    fun marcarAvisado(idCompromiso: Int, tipo: TipoAviso, fecha: LocalDate) {
        sp.edit()
            .putBoolean("aviso_${idCompromiso}_${tipo.name}", true)
            .putString("aviso_fecha_$idCompromiso", fecha.toString())
            .apply()
    }

    private companion object {
        const val CLAVE_SESION = "sesion_usuario_id"
        const val CLAVE_PERMISO = "permiso_notificaciones_solicitado"
    }
}
