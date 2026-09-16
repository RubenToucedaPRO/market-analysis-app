# Texto neutro en la página de error genérica

- Fecha: 2026-09-16
- Slug: `error-page-copy`
- Alcance: `messages.properties` (1 línea; sin cambios de código ni de tests)
- Origen: observación manual sobre `/analysis/ticker/abc` (tarea `analysis-ticker-numeric-id`)

## Resumen

La página de error genérica (`error.html`) es compartida: sale tanto para URLs que
no existen como para fallos transitorios (base de datos o API caídas). Su caja
principal mostraba `error.unexpected` = "Ocurrió un error inesperado. Por favor,
inténtalo de nuevo más tarde.", que para una URL inexistente engaña (reintentar
no sirve de nada).

Ahora dice: "Ocurrió un error inesperado. Revisa las sugerencias de abajo para saber
cómo actuar." Vale para los dos casos, porque la lista de sugerencias ya cubre ambos
(verifica que el recurso existe / servicios externos / refrescar / soporte).

## Código generado

### 1. `messages.properties`

- `error.unexpected`: antes "…inténtalo de nuevo más tarde." → ahora "…Revisa las
  sugerencias de abajo para saber cómo actuar."
- El mensaje lo pone el `GlobalExceptionHandler.handleGenericException` como
  `errorMessage`; `error.html` lo pinta sin cambios (sin tocar vistas ni Java).

## Decisiones técnicas

- Opción A (texto global neutro) en vez de mensajes por tipo de error: cambio de
  1 línea, reversible, sin tocar el handler ni los tests. Los mensajes por tipo
  quedan como idea futura.
- No se toca `error.page.default_message` (ya era neutro) ni la lista de sugerencias.

## Cobertura de tests

- `GlobalExceptionHandlerTest` (20/20 en verde): mockea el `MessageSource`, así que
  el cambio de texto no le afecta y no exige tests nuevos (se verifica a mano).
- Suite completa tras fusionar `main` con la PR #154: `mvn test` → `Tests run: 1030, Failures: 0, Errors: 0, BUILD SUCCESS`.
- Verificado en vivo con Docker reconstruido: `/analysis/ticker/abc` muestra el texto nuevo.

## Advertencias de SonarQube o arquitectura

- Sin código cambiado: nada que advertir (i18n ya existía, sin hardcode).

## Próximos pasos sugeridos

### A. Probar ahora (login con `APP_SECURITY_*`, en `http://localhost:8080`)

1. Escribe a mano `/analysis/ticker/abc` en la barra de direcciones → la caja
  principal dice "Ocurrió un error inesperado. Revisa las sugerencias de abajo
  para saber cómo actuar." (antes: "…inténtalo de nuevo más tarde."). El resto
  de la página (detalle técnico, botones, sugerencias) igual que antes.

### B. Ideas futuras (opcionales, NO hacer ahora)

- Mensajes distintos según el tipo de error (URL inexistente vs fallo transitorio),
  si se quiere hilar más fino.
