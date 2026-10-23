package cl.duoc.pagoflex.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape
import cl.duoc.pagoflex.domain.EstadoCompromiso

private val AzulProfundo = Color(0xFF0B4F6C)
private val AzulClaro = Color(0xFFCFE6F2)
private val AzulMuyOscuro = Color(0xFF00232F)
private val Turquesa = Color(0xFF14706F)
private val TurquesaClaro = Color(0xFFC3ECE9)
private val Ambar = Color(0xFF7A4F00)
private val AmbarClaro = Color(0xFFFFDEAA)

private val EsquemaClaro = lightColorScheme(
    primary = AzulProfundo,
    onPrimary = Color.White,
    primaryContainer = AzulClaro,
    onPrimaryContainer = AzulMuyOscuro,
    secondary = Turquesa,
    onSecondary = Color.White,
    secondaryContainer = TurquesaClaro,
    onSecondaryContainer = Color(0xFF00201F),
    tertiary = Ambar,
    onTertiary = Color.White,
    tertiaryContainer = AmbarClaro,
    onTertiaryContainer = Color(0xFF291800),
    error = Color(0xFFB3261E),
    onError = Color.White,
    errorContainer = Color(0xFFF9DEDC),
    onErrorContainer = Color(0xFF410E0B),
    background = Color(0xFFF6F9FB),
    onBackground = Color(0xFF14191C),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF14191C),
    surfaceVariant = Color(0xFFE3EAEE),
    onSurfaceVariant = Color(0xFF3F484D),
    outline = Color(0xFF70797E),
    outlineVariant = Color(0xFFBFC8CD)
)

private val EsquemaOscuro = darkColorScheme(
    primary = Color(0xFF8CCDEB),
    onPrimary = Color(0xFF00344A),
    primaryContainer = Color(0xFF004B69),
    onPrimaryContainer = AzulClaro,
    secondary = Color(0xFF7FD6D3),
    onSecondary = Color(0xFF003736),
    secondaryContainer = Color(0xFF00504E),
    onSecondaryContainer = TurquesaClaro,
    tertiary = Color(0xFFFFBA42),
    onTertiary = Color(0xFF442B00),
    tertiaryContainer = Color(0xFF5F3D00),
    onTertiaryContainer = AmbarClaro,
    error = Color(0xFFF2B8B5),
    onError = Color(0xFF601410),
    errorContainer = Color(0xFF8C1D18),
    onErrorContainer = Color(0xFFF9DEDC),
    background = Color(0xFF0F1417),
    onBackground = Color(0xFFDFE3E6),
    surface = Color(0xFF161C1F),
    onSurface = Color(0xFFDFE3E6),
    surfaceVariant = Color(0xFF3F484D),
    onSurfaceVariant = Color(0xFFBFC8CD),
    outline = Color(0xFF8A9398),
    outlineVariant = Color(0xFF3F484D)
)

private val Tipografia = Typography(
    displayMedium = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Bold, fontSize = 40.sp, lineHeight = 46.sp),
    headlineLarge = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Bold, fontSize = 32.sp, lineHeight = 38.sp),
    headlineMedium = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.SemiBold, fontSize = 28.sp, lineHeight = 34.sp),
    headlineSmall = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.SemiBold, fontSize = 24.sp, lineHeight = 30.sp),
    titleLarge = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.SemiBold, fontSize = 22.sp, lineHeight = 28.sp),
    titleMedium = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.SemiBold, fontSize = 18.sp, lineHeight = 24.sp),
    titleSmall = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Medium, fontSize = 16.sp, lineHeight = 22.sp),
    bodyLarge = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Normal, fontSize = 18.sp, lineHeight = 26.sp),
    bodyMedium = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 24.sp),
    bodySmall = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp),
    labelLarge = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 22.sp),
    labelMedium = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Medium, fontSize = 14.sp, lineHeight = 20.sp),
    labelSmall = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Medium, fontSize = 13.sp, lineHeight = 18.sp)
)

private val Formas = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

@Composable
fun PagoFlexTema(modoOscuro: Boolean = isSystemInDarkTheme(), contenido: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (modoOscuro) EsquemaOscuro else EsquemaClaro,
        typography = Tipografia,
        shapes = Formas,
        content = contenido
    )
}

data class ColoresEstado(val fondo: Color, val texto: Color)

@Composable
fun coloresDeEstado(idEstado: Int): ColoresEstado {
    val oscuro = isSystemInDarkTheme()
    return when (idEstado) {
        EstadoCompromiso.PENDIENTE ->
            if (oscuro) ColoresEstado(Color(0xFF004B69), Color(0xFFCFE6F2)) else ColoresEstado(Color(0xFFD7EAF5), Color(0xFF00364D))
        EstadoCompromiso.VENCIDO ->
            if (oscuro) ColoresEstado(Color(0xFF5F3D00), Color(0xFFFFDEAA)) else ColoresEstado(Color(0xFFFFE3B3), Color(0xFF5A3800))
        EstadoCompromiso.PAGADO ->
            if (oscuro) ColoresEstado(Color(0xFF14532D), Color(0xFFBBF7D0)) else ColoresEstado(Color(0xFFD3F2DC), Color(0xFF0B3D1F))
        EstadoCompromiso.EN_REVISION ->
            if (oscuro) ColoresEstado(Color(0xFF4A3B7A), Color(0xFFE6DEFF)) else ColoresEstado(Color(0xFFE8E0FF), Color(0xFF2E2260))
        else ->
            if (oscuro) ColoresEstado(Color(0xFF3F484D), Color(0xFFDFE3E6)) else ColoresEstado(Color(0xFFE3E6E8), Color(0xFF30373B))
    }
}
