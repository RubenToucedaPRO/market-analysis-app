# Task 2026-09-30 — Bind local a localhost + orden en .env.example

## Resumen
- Rama: `chore/localhost-bind-env-order` (desde `main` en `7c66903`).
- Cambios pendientes que había en `main` sin subir:
  - `docker-compose.yml`: puertos publicados solo en loopback (`127.0.0.1:8080`, `127.0.0.1:5005`, `127.0.0.1:3306` vía variable). Endurece la exposición local: la app y la BD dejan de escuchar en todas las interfaces.
  - `.env.example`: bloque `DB_URL` movido junto al resto de vars de BD + salto de línea final. Sin cambios de valores.
- Sin cambios en `src/`, `pom.xml`, plantillas ni properties: no hay lógica de negocio afectada.

## Decisiones técnicas
- Bind a `127.0.0.1` en compose (no en `Dockerfile`): es configuración de despliegue local, no de imagen. En VPS se decidirá si se publica o se va tras Nginx en la misma red Docker.
- Reorden de `.env.example` para legibilidad (bloque BD junto). No se tocan valores ni se añaden secretos.
- No se elimina `version:` obsoleto de `docker-compose.yml` (warning de Docker): fuera de alcance, se anota como idea futura.

## Cobertura de tests
- Sin código Java modificado: no se añaden tests. `mvn test` no afectado por estos dos ficheros.
- Verificación runtime (obligatoria al tocar `docker-compose.yml`, AGENTS.md §3.7): `docker compose down && docker compose up --build -d` → OK.
  - `docker compose ps`: `market-analysis-app` Up, `market-analysis-mysql` Up (healthy).
  - `GET /` → 200. `GET /health` → 200 `{"status":"UP","database_healthy":true}`.
  - Puertos efectivos: `127.0.0.1:8080->8080`, `127.0.0.1:5005->5005`, `127.0.0.1:3306->3306`.

## Advertencias SonarQube / arquitectura
- Ninguna: sin código. Hexagonal intacta.

## Conflicto de flujo (Regla 10)
- La PR #170 (`docs/railway-deploy`) sigue OPEN. Según AGENTS.md Regla 10 no debería abrirse otra rama hasta su merge; se abre por petición explícita del usuario para no dejar trabajo sin publicar en `main` sucio.

## Próximos pasos sugeridos
1. Validar esta tarea (menú abajo) → `git add` solo `.env.example`, `docker-compose.yml` + este doc → commit `chore:` → push → `gh pr create --base main`.
2. Decidir PR #170 (merge o cerrar como superada por el giro a VPS) antes de seguir con Login TFM.

## B. Ideas futuras (no hacer ahora)
- Quitar `version:` obsoleto de `docker-compose.yml`.
- Revisar si el bind a `127.0.0.1` conviene también en el compose de VPS o solo local.
