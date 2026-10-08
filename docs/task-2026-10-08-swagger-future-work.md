# Task 2026-10-08 — Swagger a Future Work (revisión plan de cierre)

## Título
Mover OpenAPI/Swagger del alcance a Future Work en `docs/tfm-closure-plan.md`.

## Resumen
- El plan pedía añadir `springdoc-openapi-starter-webmvc-ui` al `pom.xml` + documentar
  endpoints clave (Día 4 AM / bloque 8–9 Oct) y lo listaba en Scope Locked y en el DoD
  ("Swagger carga").
- Tras revisar la app se confirma que es Thymeleaf MVC (`@Controller` → vistas /
  `redirect:` + flash), no REST. Solo 4 `GET` JSON son documentables:
  `GET /health`, `GET /analysis/ticker/{id}/candles`,
  `GET /analysis/ia-jobs/{jobId}`, `GET /strategies/suggest-jobs/{jobId}`.
- El "Try it out" de Swagger sobre los formularios `POST /edit|/delete|/getTickerData`
  devuelve `302` / `403 CSRF` / redirect a `/login`: no aporta prueba práctica y confunde
  al tribunal.
- Decisión del usuario (8 Oct): sacar Swagger del alcance → Future Work, sustituir por
  tabla de endpoints JSON en README Final (sin dependencia nueva).
- Rama: `feature/swagger-to-future-work` (desde `main` sincronizado, estado limpio,
  0 PRs abiertas). Solo se toca `docs/tfm-closure-plan.md`.

## Código generado
No aplica (cambio solo-docs, sin código). Diff en `docs/tfm-closure-plan.md` (18+/14-):
1. Nueva nota `Revisión 8 Oct (2)`: motivo del descarte + sustituto (tabla en README).
2. Tabla Semana 1 / Día 4 AM: `OpenAPI/Swagger descartado — movido a Future Work;
   no se añade springdoc`. Día 4 PM (README Final) suma la tabla endpoints JSON.
3. Tabla Recalibración 8→16 Oct / 8–9 Oct: solo `README Final` (+ tabla endpoints JSON;
   `springdoc` descartado → Future Work).
4. Scope Locked: fila `OpenAPI + Swagger UI` pasa de Incluido a Excluido
   (de paso elimina el duplicado `Portfolio tracking` en la columna Excluido);
   Incluido pasa a `Tabla endpoints JSON en README`.
5. DoD Funcional: se elimina `Swagger carga (pendiente: springdoc aún no en pom.xml)`;
   queda `endpoints JSON documentados en README (Swagger a Future Work 8 Oct)`.
6. Notas de partida: `springdoc` descartado (no se añade); SonarQube sigue pendiente.
7. Decisiones Confirmadas: nuevo punto 6 con justificación (MVC vs REST, 4 endpoints,
   future work cuando exista API pública p. ej. servicio ML Python separado).
8. Próximo Paso Inmediato: cola renumerada 1–6; el nº 1 pasa a ser README Final
   (Swagger eliminado de la cola).

## Decisiones técnicas tomadas
- Descarte total de `springdoc` (ni siquiera alcance mínimo): el alcance mínimo
  (solo 4 GET JSON) tampoco compensa como dependencia + apertura en `SecurityConfig`
  (`/swagger-ui/**`, `/v3/api-docs/**`) + ruido Sonar por un checkbox cosmético.
- Alternativa de coste ~20 min y 0 dependencias: tabla manual en README
  (método, ruta, auth, ejemplo `curl` de `/health` y `/candles`).
- Justificación lista para slides: *"API pública + OpenAPI cuando se exponga el
  servicio ML Python separado (FastAPI) consumiendo datos persistidos vía API
  interna, desacoplado del motor determinista"* — coherente con la nota de modelo
  predictivo ya existente en el plan.
- Una rama = un tema = un doc: este cambio no mezcla README Final ni SonarQube;
  esos van en sus propias ramas/PRs tras el merge de esta (regla 10 AGENTS.md).

## Cobertura de tests y pruebas añadidas
- Sin cambios de código: no se añaden ni modifican tests.
- No se ejecuta `mvn test` (cambio solo `/docs`; sin comportamiento que cubrir).
- Verificación runtime Docker omitida: solo se tocó `/docs` (aplica excepción
  AGENTS.md §3.7 — se indica aquí explícitamente).

## Advertencias de SonarQube o arquitectura
- Ninguna: no hay código nuevo, no cambia la arquitectura hexagonal ni el dominio.
- Efecto positivo indirecto: se evita nueva superficie de seguridad (`swagger-ui`
  público) y deuda de mantener anotaciones `@Tag/@Operation` sobre POSTs MVC con
  redirect, que Swagger documenta mal.

## Próximos pasos sugeridos
1. Validar esta PR con menú (esta tarea se detiene aquí según AGENTS.md §3.8).
2. Tras MERGED: atacar **README Final** en rama propia (badges, URL deploy
   `https://tfm.rubentouceda.es`, sección "Desarrollo con IA", troubleshooting
   + tabla endpoints JSON) → doc → menú → PR.
3. Después (12 Oct): **SonarQube Local** + Quality Gate A + fix critical.
4. No reabrir Swagger salvo que aparezca API pública real (servicio ML separado);
   en tal caso, nueva tarea/rama con menú previo (no expandir alcance sin menú).
