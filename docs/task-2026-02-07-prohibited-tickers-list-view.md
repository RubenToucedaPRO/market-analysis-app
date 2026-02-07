# Prohibited tickers list view

## Resumen de la tarea
- Se creo la vista para listar tickers prohibidos con columnas de ticker, reason, createdAt y accion de borrado.
- Se anadio estado vacio para listas sin elementos.

## Codigo generado
- [src/main/resources/templates/prohibited-tickers/list.html](src/main/resources/templates/prohibited-tickers/list.html)

## Decisiones tecnicas tomadas
- Se reutilizo el estilo y la estructura de las vistas existentes (Bootstrap 5 + estrategia de layout).
- Se uso `#temporals.format` para representar `createdAt` con un formato legible.
- La accion de borrado se implementa como POST con confirmacion del navegador.

## Cobertura de tests
- No se anadieron tests nuevos (cambio de plantilla HTML).
- No se ejecuto la bateria de tests en esta tarea.

## Advertencias de SonarQube o arquitectura
- Sin advertencias identificadas para este cambio de vista.

## Proximos pasos sugeridos
- Verificar la vista en el navegador con datos reales y confirmar el formato de fechas.
- Si se requiere i18n, mover los textos a `messages.properties`.
