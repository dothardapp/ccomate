# AppTheme - Centralización de Estilos

Este módulo centraliza todos los estilos, colores, tipografía y dimensiones de la aplicación CCOMate.

## Uso

### 1. Colores (`AppColors`)

```kotlin
import com.iptv.ccomate.ui.theme.AppColors

Text(
    text = "Hello",
    color = AppColors.textPrimary  // En lugar de Color.White
)
```

**Colores disponibles:**
- `lightGray`, `gray1-5`, `darkGray` - Escala de grises
- `background`, `backgroundSecondary` - Fondos
- `textPrimary`, `textSecondary`, `textTertiary` - Textos
- `selected`, `unselected` - Estados

### 2. Gradientes (`AppGradients`)

```kotlin
import com.iptv.ccomate.ui.theme.AppGradients

Box(
    modifier = Modifier.background(brush = AppGradients.verticalGrayGradient)
)
```

**Gradientes disponibles:**
- `verticalGrayGradient` - Gradiente de gris claro a oscuro

### 3. Tipografía (`AppTypography`)

```kotlin
import com.iptv.ccomate.ui.theme.AppTypography

Text(
    text = "Drawer Item",
    style = AppTypography.drawerLabel
)
```

**Estilos disponibles:**
- `drawerLabel` - Texto normal del drawer
- `drawerLabelSelected` - Texto seleccionado del drawer
- `body` - Texto de cuerpo general
- `small` - Texto pequeño

### 4. Dimensiones (`AppDimensions`)

```kotlin
import com.iptv.ccomate.ui.theme.AppDimensions

Spacer(modifier = Modifier.height(AppDimensions.spacing_md))

Box(modifier = Modifier.padding(AppDimensions.containerPaddingLarge))
```

**Espacios disponibles:**
- `spacing_xs` (4.dp) a `spacing_xxl` (32.dp)
- `containerPaddingSmall/Medium/Large`
- `drawerItemSpacing`, `drawerLogoSpacing`
- `iconSmall/Medium/Large`

## ¿Por qué centralizar?

✅ **Consistencia visual** - Todos usan los mismos colores y espacios
✅ **Mantenibilidad** - Cambiar un color/gradiente en un solo lugar
✅ **Refactoring** - El IDE puede rastrear todos los usos
✅ **Escalabilidad** - Fácil agregar nuevos temas o variaciones

## Cómo agregar nuevos estilos

### Agregar un nuevo color:
```kotlin
// En AppColors
val brandPrimary = Color(0xFF...)
```

### Agregar un nuevo gradiente:
```kotlin
// En AppGradients
val horizontalGradient: Brush
    get() = Brush.horizontalGradient(colors = listOf(...))
```

### Agregar una nueva dimensión:
```kotlin
// En AppDimensions
val spacing_custom: Dp = 20.dp
```

## Archivos modificados para usar AppTheme

- ✅ `HomeScreen.kt`
- ✅ `CcoNavigationDrawer.kt`
- ✅ `DrawerItemRenderer.kt`

## Próximas mejoras

- [ ] Tema oscuro/claro (Dark mode support)
- [ ] Animaciones reutilizables
- [ ] Bordes y formas comunes
