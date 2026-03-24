package com.iptv.ccomate.navigation

/**
 * Sealed class que centraliza todas las rutas de navegación de la aplicación.
 * Esto previene errores de tipeo y facilita cambios globales de rutas.
 *
 * Uso:
 * - navController.navigate(Route.Home.path)
 * - Cambios en rutas se hacen en un solo lugar
 */
sealed class Route(val path: String) {
    object Home : Route("home")
    object TDA : Route("tda")
    object PlutoTV : Route("plutotv")
    object Settings : Route("settings")
}
