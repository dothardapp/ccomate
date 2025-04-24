CCOMate
Una aplicación de IPTV para reproducir canales como Canal 26, Telefe, TV Pública, Crónica TV y Pluto TV.
Características

Reproduce streams HLS y FLV.
Manejo de certificados SSL personalizados para conexiones seguras.
Interfaz en Jetpack Compose con navegación optimizada para Android TV.
Carga de logotipos de canales con soporte para dominios como images.pluto.tv y mtvi.com.

Instalación

Clona el repositorio:git clone https://github.com/tu-usuario/CCOMate.git


Abre el proyecto en Android Studio.
Compila y ejecuta en un dispositivo Android (probado en Android 7.1 y 8.1).

Requisitos

Android 7.0 o superior.
Dependencias:
ExoPlayer 2.19.1 (para reproducción de video).
Jetpack Compose (para la interfaz).
Coil (para carga de imágenes).



Notas

Algunos servidores requieren certificados personalizados debido a configuraciones SSL incompletas. Estos están incluidos en res/raw y configurados en network-security-config.xml.
Usa con precaución: Asegúrate de tener permisos para reproducir los streams.

Capturas
(Agrega capturas de pantalla si lo deseas, por ejemplo, de PlutoTvScreen03 o ChannelList02.)
Contribuciones
¡Siéntete libre de hacer un fork y contribuir al proyecto!