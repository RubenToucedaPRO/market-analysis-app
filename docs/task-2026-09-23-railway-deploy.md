# Task 2026-09-23 — Deploy Railway (Día 3 PM del plan TFM)

## Resumen de la tarea

Aplicación desplegada y verificada en
`https://market-analysis-app-production.up.railway.app`:
`/health` → UP, login OK, CRUD de estrategias/reglas OK, scoring visible.

Pasos previos en el repo (PRs #167 y #168):
- `server.port=${PORT:8080}` (Railway inyecta su puerto en `$PORT`).
- `GET /health` público (era 302 a `/login`; sin esto no sirve de health check).
- `.env.example` + README normalizados (notas Railway).

## Decisiones técnicas tomadas (deploy)

1. **BD: plugin MySQL de Railway** (no MariaDB). Válido: el driver MariaDB
   habla el protocolo MySQL y `script-bd.sql` es sintaxis estándar
   (InnoDB, AUTO_INCREMENT). Esquema creado ejecutando el script vía
   `railway connect MySQL` + `source <script>` (tablas vacías, prod empieza
   de cero).
2. **`DB_URL` con esquema `jdbc:mariadb://`**: el driver 3.5.8 **rechaza**
   `jdbc:mysql://` (`Driver claims to not accept jdbcUrl`). Error/
/fix documentado en esta tarea.
3. **`DB_URL` con `?allowPublicKeyRetrieval=true`**: el MySQL 8 usa
   `caching_sha2_password`; sin SSL el cliente necesita pedir la clave RSA.
   Red interna de Railway → aceptable.
4. **`DB_URL` con `&rewriteBatchedStatements=true`**: ver tarea batch velas.
5. **Referencias `${{MySQL.*}}`** en `DB_URL/USER/PASSWORD`: el servicio debe
   llamarse exactamente `MySQL`; usan la red interna
   (`mysql.railway.internal`, sin proxy público salvo para la carga inicial
   del esquema, luego retirado).
6. **Health check** del servicio en `/health`; dominio público generado.

## Problemas encontrados y solución

| Síntoma | Causa | Fix |
|---|---|---|
| `Driver ... claims to not accept jdbcUrl, jdbc:mysql://...` | Driver MariaDB no acepta esquema `jdbc:mysql:` | `jdbc:mariadb://` |
| `RSA public key is not available client side` | MySQL 8 + `caching_sha2_password` sin SSL | `?allowPublicKeyRetrieval=true` |
| Añadir ticker 41.6s (36s en persistir 240 velas) | `saveAll` = 240 INSERTs × 145ms | Batch nativo (PR #169) → 5.9s |
| `/health` 302 a `/login` | Endpoint tras autenticación | `permitAll` (PR #167) |
| Puerto fijo 8080 | Railway asigna `$PORT` | `server.port=${PORT:8080}` (PR #167) |

## Verificación en producción (medida por el usuario)

- `GET /health` → 200 `{"status":"UP","database_healthy":true,...}`.
- Login OK, `/strategies` vacía (BD nueva), estrategia + reglas creadas OK.
- Añadir ticker `U`: 41.6s → **5.9s** tras PR #169 + flag driver.
- Métricas Railway: CPU ~0, RAM ~400MB plana, 177 requests sin 5xx →
  el tiempo se iba en esperas externas, no en falta de recursos.
- Limitación conocida (documentada, fuera de alcance): APIs gratuitas
  (turno Polygon 5/min, cola OpenRouter) + latencia BD 145ms; flujos
  multi-ticker pueden tardar minutos. Uso recomendado: 1-2 tickers.

## Advertencias de SonarQube o arquitectura

- Ninguna: esta tarea no toca código (solo este doc + plan). Los cambios de
  código previos pasaron por sus PRs con tests en verde.

## Próximos pasos sugeridos

- Siguiente del plan TFM (Día 4 AM): **OpenAPI/Swagger**
  (`springdoc-openapi-starter-webmvc-ui`, hoy ausente en `pom.xml`).
- Día 4 PM: README final + SonarQube local + Quality Gate A.

## Verificación runtime

- Producción verificada a mano (checklist en el doc de la tarea previa y
  sección anterior). Sin cambios de ejecución en esta rama (solo docs).

## Checklist de pruebas web

### A. Probar ahora

1. Abre `https://market-analysis-app-production.up.railway.app/health` →
   JSON con `"status":"UP"` (sin login).
2. Abre `.../login`, entra con tus credenciales de Railway → ves `/analysis`.
3. Todo lo automatizable está cubierto por tests (1071 en `main`).

### B. Ideas futuras

- OpenAPI/Swagger (siguiente tarea del plan).
