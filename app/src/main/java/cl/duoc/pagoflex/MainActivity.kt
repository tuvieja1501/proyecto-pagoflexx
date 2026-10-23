package cl.duoc.pagoflex

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import cl.duoc.pagoflex.ui.RaizApp
import cl.duoc.pagoflex.ui.sesion.SesionViewModel
import cl.duoc.pagoflex.ui.theme.PagoFlexTema

class MainActivity : FragmentActivity() {

    private val sesion: SesionViewModel by viewModels {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                SesionViewModel((application as PagoFlexApp).contenedor, applicationContext) as T
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PagoFlexTema { RaizApp(this, sesion) }
        }
    }

    override fun onStop() {
        super.onStop()
        sesion.alIrASegundoPlano()
    }

    override fun onStart() {
        super.onStart()
        sesion.alVolverAPrimerPlano()
    }
}
