package cl.duoc.pagoflex.util

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import cl.duoc.pagoflex.PagoFlexApp
import cl.duoc.pagoflex.data.RepositorioBase
import cl.duoc.pagoflex.data.RepositorioUsuarioFinal
import cl.duoc.pagoflex.domain.AppClock
import cl.duoc.pagoflex.domain.Avisos
import cl.duoc.pagoflex.domain.RolIds
import java.util.concurrent.TimeUnit

class ServicioAvisos(
    private val base: RepositorioBase,
    private val usuarioFinal: RepositorioUsuarioFinal,
    private val preferencias: Preferencias,
    private val notificador: Notificador
) {

    suspend fun revisar(): Int {
        val idUsuario = preferencias.sesionUsuarioId ?: return 0
        val persona = base.usuario(idUsuario) ?: return 0
        if (persona.idRol != RolIds.USUARIO_FINAL || persona.avisosActivos != true) return 0

        if (Avisos.enHorarioSilencioso(AppClock.ahora().toLocalTime())) return 0

        val hoy = AppClock.hoy()
        val anticipacion = persona.diasAnticipacion ?: 3
        var enviados = 0
        for (c in usuarioFinal.listarCompromisos(idUsuario)) {
            val tipo = Avisos.tipoDeAviso(c.idEstado, c.vencimiento, hoy, anticipacion) ?: continue
            if (preferencias.yaAvisado(c.id, tipo)) continue
            if (preferencias.fechaUltimoAviso(c.id) == hoy) continue
            val texto = Avisos.mensaje(
                tipo, persona.nombres.substringBefore(' '), c.concepto, c.empresa,
                Avisos.diasRestantes(c.vencimiento, hoy), c.totalAPagar
            )
            if (notificador.mostrar(c.id * 10 + tipo.ordinal, Avisos.titulo(tipo), texto)) {
                preferencias.marcarAvisado(c.id, tipo, hoy)
                enviados++
            }
        }
        return enviados
    }
}

class AvisosWorker(contexto: Context, parametros: WorkerParameters) : CoroutineWorker(contexto, parametros) {
    override suspend fun doWork(): Result {
        val contenedor = (applicationContext as PagoFlexApp).contenedor
        contenedor.asegurarInicializado()
        contenedor.base.actualizarVencimientos()
        contenedor.servicioAvisos.revisar()
        return Result.success()
    }
}

object ProgramadorAvisos {
    private const val TRABAJO_PERIODICO = "avisos_periodicos"
    private const val TRABAJO_INMEDIATO = "avisos_ahora"

    fun programar(contexto: Context) {
        WorkManager.getInstance(contexto).enqueueUniquePeriodicWork(
            TRABAJO_PERIODICO, ExistingPeriodicWorkPolicy.KEEP,
            PeriodicWorkRequestBuilder<AvisosWorker>(1, TimeUnit.HOURS).build()
        )
    }

    fun revisarAhora(contexto: Context) {
        WorkManager.getInstance(contexto).enqueueUniqueWork(
            TRABAJO_INMEDIATO, ExistingWorkPolicy.REPLACE, OneTimeWorkRequestBuilder<AvisosWorker>().build()
        )
    }
}
