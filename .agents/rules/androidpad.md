---
trigger: manual
---

Basado en la guía oficial de diseño de Android TV sobre navegación, aquí tienes un conjunto de reglas clave a tener en cuenta al diseñar, evaluar o generar interfaces para aplicaciones de TV:

Principios Fundamentales
Regla de Eficiencia: Minimiza la cantidad de pasos o "clics" necesarios. El contenido debe ser accesible de la forma más rápida y directa posible, organizando la información para usar la menor cantidad de pantallas.

Regla de Predictibilidad: Utiliza patrones de navegación estándar. No reinventes la rueda; la navegación debe comportarse de la forma que el usuario ya espera en un entorno de TV para evitar confusiones.

Regla de Intuición: Mantén la simplicidad. Evita agregar capas de navegación o submenús complejos e innecesarios que sobrecarguen la experiencia.

Reglas de Control (Mando a distancia / D-pad)
Enfoque Direccional (Arriba, Abajo, Izquierda, Derecha): Diseña siempre asumiendo que el usuario usará un pad direccional (D-pad) de 4 direcciones.

Accesibilidad de Elementos: Todos los elementos interactivos en pantalla deben poder recibir el "foco" y tener una ruta de acceso clara y directa desde los demás elementos. Si un control es difícil de alcanzar con el D-pad, debe reubicarse.

Uso de Ejes (Vertical y Horizontal): Asigna funciones claras a los ejes. Por ejemplo: usa el eje vertical para desplazarte por las diferentes categorías y el eje horizontal para navegar por los elementos dentro de cada categoría. Evita anidamientos complejos.

Reglas del Botón "Atrás"
Regresión Lógica: Presionar el botón "Atrás" siempre debe llevar al usuario al destino anterior lógico y, en última instancia, a la pantalla de inicio del sistema (Home).

Prohibición de Botón Atrás Virtual: Nunca diseñes ni muestres un botón "Atrás" en la pantalla de la interfaz. Los usuarios de TV ya tienen un botón físico en su mando para esto.

Cero Secuestros de Salida: No bloquees el botón "Atrás" con ventanas de confirmación para salir de la aplicación ni crees bucles infinitos. El usuario debe poder salir libremente.

Botón de Cancelar (Solo cuando sea necesario): Si el usuario está en una pantalla con acciones destructivas, de compra o de confirmación exclusiva, incluye un botón visible de "Cancelar" para regresar de forma segura.

Reglas de Arquitectura de Navegación
Destino de Inicio Fijo: La primera pantalla que ve el usuario al abrir la app debe ser exactamente la última pantalla que vea al presionar repetidamente el botón "Atrás" antes de salir de la misma. No incluyas pantallas de carga (splash screens) en el historial de navegación hacia atrás.

Simulación en Vínculos Directos (Deep Links): Si el usuario ingresa a una pantalla específica de tu app desde un enlace externo (como la pantalla principal de Google TV), al presionar "Atrás", no debe salir inmediatamente de la app. Debe simularse una navegación natural que lo lleve a la página de inicio de tu aplicación primero.