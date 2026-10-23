package cl.duoc.pagoflex.ui

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost

@Composable
fun PagoFlexNavHost(
    navController: NavHostController,
    inicio: String,
    modifier: Modifier = Modifier,
    grafo: NavGraphBuilder.() -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = inicio,
        modifier = modifier,
        enterTransition = { fadeIn(tween(250)) + slideInHorizontally(tween(250)) { it / 8 } },
        exitTransition = { fadeOut(tween(200)) },
        popEnterTransition = { fadeIn(tween(250)) },
        popExitTransition = { fadeOut(tween(200)) + slideOutHorizontally(tween(250)) { it / 8 } },
        builder = grafo
    )
}

fun NavHostController.irAPestana(ruta: String) {
    navigate(ruta) {
        popUpTo(graph.startDestinationId) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
