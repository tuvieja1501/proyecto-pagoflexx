package cl.duoc.pagoflex.util

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

object AutenticacionDispositivo {

    private const val AUTENTICADORES = BIOMETRIC_WEAK or DEVICE_CREDENTIAL

    fun disponible(contexto: Context): Boolean =
        BiometricManager.from(contexto).canAuthenticate(AUTENTICADORES) == BiometricManager.BIOMETRIC_SUCCESS

    fun solicitar(
        actividad: FragmentActivity,
        titulo: String,
        subtitulo: String,
        alExito: () -> Unit,
        alFallar: (String) -> Unit
    ) {
        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                alExito()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                alFallar(errString.toString())
            }
        }
        val prompt = BiometricPrompt(actividad, ContextCompat.getMainExecutor(actividad), callback)
        val info = BiometricPrompt.PromptInfo.Builder()
            .setTitle(titulo)
            .setSubtitle(subtitulo)
            .setAllowedAuthenticators(AUTENTICADORES)
            .build()
        prompt.authenticate(info)
    }
}
