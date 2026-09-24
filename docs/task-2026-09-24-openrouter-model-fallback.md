# Task 2026-09-24 — OpenRouter: rotación de modelos ante 429

## Resumen de la tarea

La valoración IA en Railway caía a fallback 4/4 veces con
`RateLimitException: 429: Provider returned error` (cuota gratis agotada /
proveedor saturado). Además se encontró un bug: el `@Retry(openrouterClient)`
**nunca se disparaba** porque el adapter envolvía todo en
`AIServiceException` antes de que el reintento viera el 429.

Fix: el adapter prueba los modelos en orden y devuelve la primera respuesta
válida. Solo los 429 rotan; otros errores (clave, red, modelo inexistente)
fallan rápido al fallback del caso de uso (sin cambios allí). Límite honesto:
si el cupo diario de la cuenta (50/día compartido) está agotado, fallan todos;
la rotación cubre saturación del proveedor y límites por minuto, no el cap
diario. Secuencial a propósito: en paralelo se quemaría 3× cuota por análisis.

## Código generado

- `.../external/openrouter/OpenrouterAdapter.java`: constructor con
  `openrouter.model` (sin cambios, Railway sigue igual) +
  `openrouter.fallback-models` (lista, 2 gratis por defecto); bucle
  secuencial con `catch (RateLimitException)` → siguiente modelo,
  `catch (Exception)` → `AIServiceException` inmediata.
- `config/application.properties`: `openrouter.fallback-models` con default
  `meta-llama/llama-3.3-70b-instruct:free,openai/gpt-oss-20b:free` (cambiable
  por `OPENROUTER_FALLBACK_MODELS` sin código).
- `.env.example` + `README.md`: documentada la nueva variable.
- `OpenrouterAdapterTest`: +4 tests (rota al 2º ante 429, 429 en todos →
  `AIServiceException` tras 3 intentos, error genérico sin reintento,
  funciona sin reservas).

## Decisiones técnicas tomadas

- 7 parámetros en constructor (límite S107): no se añade más; si crece, pasar
  a `@ConfigurationProperties`.
- Se mantiene `@Retry`: con la rotación interna no ve 429s, pero no molesta;
  quitarlo sería ruido en el diff.
- Nada en paralelo: hilos + cancelación para ahorrar 0s no compensa, y
  gastaría cuota triple.

## Cobertura de tests y pruebas añadidas

- `mvn test`: `Tests run: 1075, Failures: 0, Errors: 0` (1071 + 4 nuevos).
- Sin `lenient` nuevo.

## Advertencias de SonarQube o arquitectura

- Cambio aislado en el adapter de infraestructura; el puerto `ApiIAPort` y el
  fallback del caso de uso no cambian. Arquitectura hexagonal intacta.

## Próximos pasos sugeridos

- En Railway (tú, opcional): `OPENROUTER_FALLBACK_MODELS` si quieres otros
  modelos; el default ya vale.
- Si los 429 diarios persisten: top-up único $10 → 1000/día (decisión tuya,
  fuera del código).
- PR #170 (doc deploy) sigue pendiente de tu merge; esta rama sale de `main`
  sin conflictos.

## Verificación runtime

- Requerida por §3.7 (se tocó `src/`): `docker compose down &&
  docker compose up --build -d`, `GET /` → 200 y mysql Healthy (ver abajo).
  La rotación real solo se prueba contra OpenRouter (cuota mediante).

## Checklist de pruebas web

### A. Probar ahora

1. Con el contenedor local: `http://localhost:8080/health` → JSON
   `"status":"UP"` (arranque con la nueva propiedad).
2. En Railway tras el merge (con cuota disponible): genera un análisis →
   la valoración IA llega sin flash de fallback. Si sale el flash con 429 en
   los 3 modelos, es cupo diario agotado (esperar a medianoche UTC o top-up).
   Cubierto por tests lo demás (`OpenrouterAdapterTest`).

### B. Ideas futuras

- Top-up $10 o modelo de pago si el TFM necesita volumen de demos.
