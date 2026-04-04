# CCOMate

Aplicacion IPTV multi-plataforma para Android TV y dispositivos moviles. Reproduce canales de TDA (Television Digital Abierta argentina) y Pluto TV con guia de programacion EPG integrada.

## Arquitectura

Proyecto multi-modulo con logica de negocio compartida:

```
CCOMate/
├── :core          → Modelos, repositorios, ViewModels, Room DB, DI (Hilt), networking
├── :app-tv        → UI optimizada para Android TV (D-Pad, NavigationDrawer, tv-material3)
└── :app-mobile    → UI para moviles/tablets (Scaffold, BottomNavigation, Material3)
```

## Funcionalidades

- Reproduccion de streams HLS via Media3 ExoPlayer con bitrate adaptativo
- Guia de programacion EPG para Pluto TV con actualizacion automatica
- Base de datos local Room con queries optimizadas e indices
- Fullscreen inmersivo en mobile al rotar a landscape (sin interrupcion de video)
- Navegacion D-Pad completa en Android TV con restauracion de foco
- Certificados SSL personalizados para streams que lo requieran
- Carga de logos con Coil (PNG, SVG, WebP)
- Tema oscuro nativo en ambas plataformas

## Stack Tecnologico

| Componente | Tecnologia |
|---|---|
| UI TV | Jetpack Compose + tv-material3 |
| UI Mobile | Jetpack Compose + Material3 |
| Video | Media3 ExoPlayer 1.6.1 (HLS, AdaptiveTrackSelection) |
| Networking | Ktor CIO + OkHttp (datasource) |
| Base de datos | Room 2.7.2 con KSP |
| DI | Hilt 2.55 |
| Imagenes | Coil 2.x (compose, SVG) |
| Navegacion | Navigation Compose |
| Lenguaje | Kotlin 2.0.21 |
| Build | AGP 9.0.1 + Version Catalog |

## Requisitos

- Android Studio Meerkat o superior
- JDK 11
- Android SDK 35 (compileSdk)
- Dispositivo o emulador con minSdk 24 (TV) / 26 (Mobile)

## Instalacion

```bash
git clone https://github.com/tu-usuario/CCOMate.git
cd CCOMate
```

Abrir en Android Studio y sincronizar Gradle. Seleccionar la configuracion de ejecucion deseada:

- **app-tv** para Android TV / dispositivos con leanback
- **app-mobile** para telefonos y tablets

## Estructura de Modulos

### :core

Contiene toda la logica compartida entre ambas apps:

- `model/` — Channel, EPGProgram, DrawerItem
- `data/` — ChannelRepository, EPGRepository, Room (AppDatabase, DAOs, Entities), M3UParser, EPGParser, NetworkClient
- `viewmodel/` — VideoPlayerViewModel, SubscriptionViewModel, ChannelListViewModel, TdaViewModel, PlutoTvViewModel
- `di/` — AppModule (Hilt, provee Room DB y DAOs)
- `util/` — AppConfig, DeviceIdentifier, SubscriptionManager, TimeUtils, SvgUtils

### :app-tv

UI especifica para Android TV:

- NavigationDrawer lateral con logo animado
- ChannelScreen con layout split (video + info arriba, grupos + canales abajo)
- Fullscreen con zapping via D-Pad (izquierda/derecha cambia canal)
- Skeleton loading con shimmer compartido
- movableContentOf para preservar ExoPlayer entre layouts

### :app-mobile

UI especifica para moviles y tablets:

- Scaffold con BottomNavigationBar (Inicio, TDA, Pluto TV)
- Video player 16:9 en portrait, fullscreen inmersivo en landscape
- FilterChips horizontales para grupos de canales
- Lista scrolleable de canales con logo y nombre
- movableContentOf para video sin interrupcion al rotar

## Documentación Técnica

Para análisis profundos de cada capa arquitectónica, consultar:

- **[etapa1_infraestructura.md](etapa1_infraestructura.md)** — Grafo de módulos, Product Flavors, stack tecnológico completo, configuración de Gradle
- **[etapa2_datos_y_dominio.md](etapa2_datos_y_dominio.md)** — Room database, parsers (M3U/EPG), repositorios, networking, modelos de dominio
- **[etapa3_logica_negocio.md](etapa3_logica_negocio.md)** — ViewModels, máquinas de estado, integración de EPG, reproducción de video (ExoPlayer/VLC)
- **[etapa4_interfaces_ui.md](etapa4_interfaces_ui.md)** — Arquitectura de UI para TV (D-Pad, Drawer, 3-capas) y Mobile (responsive, BottomNav)

**Status:** ✅ 100% Implementado (4 etapas, 95 componentes verificados)

## Notas

- Los certificados SSL personalizados estan en `res/raw/` y configurados en `network_security_config.xml` de cada modulo app
- El proyecto usa `api()` en `:core` para exponer dependencias transitivas a los modulos app
- Los ViewModels de canal son `Activity-scoped` para sobrevivir a la navegacion entre pantallas
- El proyecto soporta 2 players multimedia mediante Product Flavors: ExoPlayer (Media3) y LibVLC, compilables para ambas plataformas (TV y Mobile)

## Capturas

<!-- Agrega capturas aqui: -->
<!-- ![TDA en TV](screenshots/tda_tv.png) -->
<!-- ![Pluto en Mobile](screenshots/pluto_mobile.png) -->

## Contribuciones

Las contribuciones son bienvenidas. Haz un fork, crea tu branch y envia un pull request.
