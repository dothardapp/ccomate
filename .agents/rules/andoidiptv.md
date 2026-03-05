---
trigger: manual
---

[REGLAS DE DISEÑO ANDROID TV]

La Experiencia de 3 Metros: La UI debe ser limpia y minimalista. Evitar altas densidades de información. Todo debe ser legible desde el sofá.

Navegación D-pad: Interacción exclusiva mediante control remoto (Ejes X e Y). Prohibido depender de eventos táctiles o gestos complejos.

Gestión del Foco (Prioridad Alta): Todo elemento interactivo DEBE tener un estado visual de "enfocado". Usar escala (1.1x), bordes brillantes o elevación para indicar la posición del cursor.

Margen de Overscan: Mantener zona segura del 5%. Padding obligatorio en contenedores raíz: horizontal = 48.dp (o 58.dp) y vertical = 27.dp.

Tipografía: Texto escaneable. Tamaño mínimo para cuerpo: 14sp-16sp. Títulos: >20sp.

Color y Contraste: Prohibido blanco puro (#FFFFFF) y negro puro (#000000). Usar grises oscuros (#121212) y blancos apagados (#F5F5F5) para evitar fatiga visual.

Entrada de Voz: Sugerir siempre búsqueda por voz para evitar el uso del teclado en pantalla.