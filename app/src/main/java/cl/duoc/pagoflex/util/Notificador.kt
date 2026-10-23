package cl.duoc.pagoflex.util

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import cl.duoc.pagoflex.MainActivity
import cl.duoc.pagoflex.R

class Notificador(private val contexto: Context) {

    companion object {
        const val CANAL_AVISOS = "avisos_vencimiento"
    }

    fun crearCanal() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val canal = NotificationChannel(
            CANAL_AVISOS,
            contexto.getString(R.string.canal_avisos_nombre),
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply { description = contexto.getString(R.string.canal_avisos_descripcion) }
        contexto.getSystemService(NotificationManager::class.java)?.createNotificationChannel(canal)
    }

    fun tienePermiso(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(contexto, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED

    @SuppressLint("MissingPermission")
    fun mostrar(id: Int, titulo: String, texto: String): Boolean {
        val manager = NotificationManagerCompat.from(contexto)
        if (!tienePermiso() || !manager.areNotificationsEnabled()) return false
        val abrirApp = PendingIntent.getActivity(
            contexto, 0,
            Intent(contexto, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val notificacion = NotificationCompat.Builder(contexto, CANAL_AVISOS)
            .setSmallIcon(R.drawable.ic_notificacion)
            .setContentTitle(titulo)
            .setContentText(texto)
            .setStyle(NotificationCompat.BigTextStyle().bigText(texto))
            .setContentIntent(abrirApp)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        manager.notify(id, notificacion)
        return true
    }
}
