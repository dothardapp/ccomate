Eres un desarrollador Android/Kotlin experto en Jetpack Compose para Android TV
(androidx.tv.material3). Debes implementar mejoras visuales y de comportamiento
en el sistema de navegación de una app IPTV llamada CCOMate.

================================================================================
CONTEXTO DEL PROYECTO
================================================================================

Repositorio: c:\Users\josel\AndroidStudioProjects\CCOMate
Branch actual: v1.1-estable
Módulo objetivo: app-tv (app-mobile NO se toca)

Stack: Kotlin + Jetpack Compose + androidx.tv.material3 + androidx.navigation.compose

El sistema de navegación vive en:
  app-tv/src/main/java/com/iptv/ccomate/navigation/
    - CcoNavigationDrawer.kt    (contenedor principal del drawer + NavHost wrapper)
    - DrawerItemRenderer.kt     (render de los items del drawer)
    - DrawerItemsList.kt        (lista estática de items: HOME, TDA, PLUTO, CONFIG)
    - DrawerLogo.kt             (logo animado arriba del drawer)
    - NavGraph.kt               (NavHost con 4 rutas)
    - Route.kt                  (sealed class con paths: home, tda, plutotv, settings)

Theme centralizado en:
  app-tv/src/main/java/com/iptv/ccomate/ui/theme/AppTheme.kt
  Contiene: AppColors, AppGradients, AppTypography, AppDimensions
  NO crear colores/dimensiones nuevos hardcoded — AGREGARLOS a AppTheme.kt.

Modelo DrawerItem:
  core/src/main/java/com/iptv/ccomate/model/DrawerItem.kt
  DrawerIcon es sealed: Vector(ImageVector) | Resource(Int)

Fullscreen state:
  com.iptv.ccomate.util.LocalFullscreenState  (CompositionLocal<MutableState<Boolean>>)
  Cuando es true, el drawer debe estar oculto y no interferir con el reproductor.

================================================================================
TAREAS A IMPLEMENTAR (en orden)
================================================================================

--------------------------------------------------------------------------------
TAREA 1 — Sincronizar selectedIndex con la ruta actual del NavController
Archivo: CcoNavigationDrawer.kt
--------------------------------------------------------------------------------

Problema: `selectedIndex` solo se actualiza en onItemClick. Si el usuario navega
por back-stack o deep-link, el drawer queda desincronizado.

Acción:
  Reemplazar la declaración:
    var selectedIndex by remember { mutableIntStateOf(0) }
  Por una lectura derivada de currentBackStackEntryAsState:

    import androidx.navigation.compose.currentBackStackEntryAsState
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val selectedIndex = remember(currentRoute) {
        drawerItems.indexOfFirst { it.route == currentRoute }.coerceAtLeast(0)
    }

  Eliminar `selectedIndex = index` dentro de onItemClick (ya no es necesario).

--------------------------------------------------------------------------------
TAREA 2 — Mover CONFIG al fondo del drawer (patrón Android TV estándar)
Archivos: DrawerItemRenderer.kt + CcoNavigationDrawer.kt
--------------------------------------------------------------------------------

DrawerItemRenderer.kt:
  - Aceptar un parámetro nuevo: `bottomItemsCount: Int = 1` (por defecto el
    último item se renderiza abajo).
  - Cambiar la Column interna por una Column que llene altura disponible
    (fillMaxHeight) y separe los primeros N items del último con
    Spacer(Modifier.weight(1f)).
  - Firma final:
      @Composable
      fun NavigationDrawerScope.DrawerItemRenderer(
          items: List<DrawerItem>,
          selectedIndex: Int,
          bottomItemsCount: Int = 1,
          onItemClick: (Int, String) -> Unit
      )

  Implementación:
    val topItems = items.dropLast(bottomItemsCount)
    val bottomItems = items.takeLast(bottomItemsCount)

    Column(Modifier.fillMaxHeight()) {
        topItems.forEachIndexed { index, item -> renderItem(index, item) }
        Spacer(Modifier.weight(1f))
        bottomItems.forEachIndexed { i, item ->
            val realIndex = topItems.size + i
            renderItem(realIndex, item)
        }
    }

  Extraer la lógica de cada NavigationDrawerItem a una función local
  `@Composable fun renderItem(index: Int, item: DrawerItem)` para evitar duplicación.

CcoNavigationDrawer.kt:
  - El Spacer(drawerLogoSpacing) tras el logo se mantiene.
  - Eliminar el Arrangement.spacedBy en la Column exterior (ahora el
    DrawerItemRenderer gestiona su propio espaciado interno).

--------------------------------------------------------------------------------
TAREA 3 — Indicador de selección persistente (barra vertical lateral izquierda)
Archivo: DrawerItemRenderer.kt
--------------------------------------------------------------------------------

Objetivo: Mostrar una barra vertical de 3dp a la izquierda del item activo
(coincide con la ruta actual), visible INDEPENDIENTEMENTE del foco.

Agregar al inicio de AppDimensions (en AppTheme.kt):
    val drawerSelectionIndicatorWidth: Dp = 3.dp
    val drawerSelectionIndicatorHeight: Dp = 24.dp

Agregar a AppColors (en AppTheme.kt):
    val drawerSelectionIndicator = Color(0xFF42A5F5)  // accentBlueFocused

En DrawerItemRenderer, envolver cada NavigationDrawerItem en un Row:
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .width(AppDimensions.drawerSelectionIndicatorWidth)
                .height(AppDimensions.drawerSelectionIndicatorHeight)
                .background(
                    if (selectedIndex == index) AppColors.drawerSelectionIndicator
                    else Color.Transparent,
                    shape = RoundedCornerShape(topEnd = 2.dp, bottomEnd = 2.dp)
                )
        )
        Spacer(Modifier.width(8.dp))
        NavigationDrawerItem( ... )  // el actual
    }

--------------------------------------------------------------------------------
TAREA 4 — Estado de foco diferenciado (TV-first)
Archivo: DrawerItemRenderer.kt
--------------------------------------------------------------------------------

Objetivo: Distinguir visualmente foco (focused) de selección (selected).

Agregar a AppColors (en AppTheme.kt):
    val drawerItemFocused = gray5.copy(alpha = 0.4f)

En cada NavigationDrawerItem, actualizar el `colors =`:
    colors = NavigationDrawerItemDefaults.colors(
        selectedContainerColor = Color.Transparent,
        selectedContentColor = AppColors.selected,
        focusedContainerColor = AppColors.drawerItemFocused,
        focusedContentColor = AppColors.selected,
        focusedSelectedContainerColor = AppColors.drawerItemFocused,
        focusedSelectedContentColor = AppColors.selected
    )

Para el tinting del icono, modificar DrawerIconContent
(app-tv/src/main/java/com/iptv/ccomate/ui/screens/DrawerIconContent.kt):
  - Aceptar parámetro `tint: Color = Color.Unspecified`.
  - En la rama Vector: pasar `tint = tint` al Icon.
  - En la rama Resource: aplicar `colorFilter = if (tint != Color.Unspecified)
    ColorFilter.tint(tint) else null` al Image.

En DrawerItemRenderer, pasar tint dinámico según selectedIndex:
    DrawerIconContent(
        icon = item.icon,
        label = item.label,
        tint = if (selectedIndex == index) AppColors.selected
               else AppColors.textSecondary
    )

--------------------------------------------------------------------------------
TAREA 5 — Animación mejorada del logo
Archivo: DrawerLogo.kt
--------------------------------------------------------------------------------

Agregar al lado de las animaciones de tamaño existentes:
    val satelliteAlpha by animateFloatAsState(
        targetValue = if (isOpen) 1f else 0.6f,
        animationSpec = tween(300),
        label = "satelliteAlpha"
    )
    val logoAlpha by animateFloatAsState(
        targetValue = if (isOpen) 1f else 0.8f,
        animationSpec = tween(300),
        label = "logoAlpha"
    )

Aplicar con .graphicsLayer { alpha = satelliteAlpha } y análogo al logo.
NO usar Modifier.alpha (se combina peor con otros modifiers).

--------------------------------------------------------------------------------
TAREA 6 — Manejo de BACK cuando el drawer está abierto
Archivo: CcoNavigationDrawer.kt
--------------------------------------------------------------------------------

Agregar import:
    import androidx.activity.compose.BackHandler

Dentro del Composable, ANTES del NavigationDrawer(...):
    BackHandler(enabled = drawerState.currentValue == DrawerValue.Open) {
        scope.launch { drawerState.setValue(DrawerValue.Closed) }
    }

--------------------------------------------------------------------------------
TAREA 7 — Ocultar drawer limpiamente en fullscreen
Archivo: CcoNavigationDrawer.kt
--------------------------------------------------------------------------------

Reemplazar el Spacer(Modifier.width(0.dp)) en el else por:
    // drawerContent vacío intencional en fullscreen
    Box(Modifier.width(0.dp))

(Box con width 0 es más explícito y no intenta ocupar slot que Spacer sí puede
reservar con ciertos arrangements.)

--------------------------------------------------------------------------------
TAREA 8 — Transiciones entre pantallas en NavGraph
Archivo: NavGraph.kt
--------------------------------------------------------------------------------

Agregar imports:
    import androidx.compose.animation.core.tween
    import androidx.compose.animation.fadeIn
    import androidx.compose.animation.fadeOut
    import androidx.compose.animation.slideInHorizontally
    import androidx.compose.animation.slideOutHorizontally

Añadir enterTransition/exitTransition a nivel de NavHost (aplica a todas las rutas):

    NavHost(
        navController = navController,
        startDestination = Route.Home.path,
        enterTransition = {
            slideInHorizontally(tween(300)) { it / 8 } + fadeIn(tween(300))
        },
        exitTransition = {
            fadeOut(tween(200))
        },
        popEnterTransition = {
            slideInHorizontally(tween(300)) { -it / 8 } + fadeIn(tween(300))
        },
        popExitTransition = {
            fadeOut(tween(200))
        }
    ) {
        composable(Route.Home.path) { HomeScreen() }
        composable(Route.TDA.path) { TDAScreen() }
        composable(Route.PlutoTV.path) { PlutoTvScreen() }
        composable(Route.Settings.path) { SettingsScreen() }
    }

================================================================================
REGLAS ESTRICTAS
================================================================================

1. NO tocar archivos fuera de:
     - app-tv/src/main/java/com/iptv/ccomate/navigation/*.kt
     - app-tv/src/main/java/com/iptv/ccomate/ui/theme/AppTheme.kt
     - app-tv/src/main/java/com/iptv/ccomate/ui/screens/DrawerIconContent.kt

2. NO modificar app-mobile, core, ni ningún otro módulo.

3. NO agregar dependencias nuevas al build.gradle.

4. NO hacer git commit, git push, ni crear ramas. Solo edita archivos.

5. Todos los colores y dimensiones nuevos DEBEN ir en AppTheme.kt — prohibido
   hardcodear Color(0xFF...) o .dp directo en los archivos de navigation.

6. Preservar toda la lógica actual de fullscreenState y el contentFocusRequester.

7. NO eliminar comentarios existentes del código.

8. Después de editar, ejecuta:
     ./gradlew :app-tv:compileDebugKotlin
   y reporta si compila. Si hay errores, arréglalos antes de reportar "done".

9. Al terminar, entrega un resumen en este formato exacto:
     - Archivos modificados: [lista con rutas relativas]
     - Compila: SI / NO
     - Advertencias encontradas: [lista o "ninguna"]
     - Tareas completadas: [1,2,3,4,5,6,7,8]

================================================================================
VERIFICACIÓN VISUAL ESPERADA (qué debería ver el usuario al probar)
================================================================================

- Al abrir la app, el drawer muestra HOME/TDA/PLUTO arriba y CONFIG al fondo.
- El item de la pantalla activa tiene una barra azul vertical a su izquierda.
- Navegar con D-pad destaca el item enfocado con fondo gris translúcido
  (distinto del indicador azul de selección).
- El icono del item activo o enfocado se ve blanco puro; los demás, grisáceos.
- Al abrir el drawer, el satélite y el logo hacen fade-in suave además del scale.
- Pulsar BACK con drawer abierto lo cierra (no sale de la app).
- Cambiar de pantalla muestra un slide horizontal + fade de 300ms.
- En fullscreen de reproductor, el drawer no aparece ni interfiere con el foco.
