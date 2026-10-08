# Task 2026-10-08 — README Final + eliminar readme_aux.md

## Título
README Final (badges, URL deploy, "Desarrollo con IA", troubleshooting, tabla
endpoints JSON) + eliminación de `readme_aux.md`.

## Resumen
- Bloque 8–9 Oct del plan de cierre (`docs/tfm-closure-plan.md`): README Final.
  Rama `feature/readme-final` desde `main` sincronizado (PR #184 en MERGED).
- Cambios en `README.md` (60+/20-):
  1. Cabecera: línea **Despliegue productivo:** https://tfm.rubentouceda.es
     (URL verificada en `deploy/.env` → `TFM_DOMAIN` y en task de hardening).
  2. Modelo IA principal `poolside/laguna-s-2.1:free` + 4 modelos de reserva
     (`nvidia/nemotron-3-ultra-550b-a55b:free`, `poolside/laguna-xs-2.1:free`,
     `google/gemma-4-31b-it:free`, `google/gemma-4-26b-a4b-it:free`) en 5 sitios
     (componentes, arquitectura, stack, tabla de variables, bloque `.env`),
     coincidiendo con `.env.example`. Nota: los defaults del código
     (`application.properties:63-68`, `OpenrouterAdapter`, `docker-compose.yml`)
     siguen siendo `gemma-4-26b…`; manda el entorno y el README lo indica.
     (Corrección tras 1ª validación: el README decía `gemma` como principal.)
  3. Referencias obsoletas a Railway eliminadas (5 sitios: arquitectura,
     `.env`, tabla de variables DB/plots/`PORT`): ahora VPS + `deploy/guia_vps.md`.
  4. Testing: `test containers` (inexistente en el repo) → MockMvc
     (verificado: `LoginLockoutTest`, `SecurityConfigTest`, … usan MockMvc).
  5. Métricas: cobertura JaCoCo ≥ 80% como hecho (`mvn verify`); SonarQube
     Quality Gate A como **objetivo** (tarea del 12 Oct, no afirmar lo pendiente).
  6. Autor: fecha Febrero 2026 → Octubre 2026.
  7. Nueva sección **🤖 Desarrollo con IA**: tres fases (Copilot inicial,
     OpenCode/Muse Spark con procedimiento `AGENTS.md` §3, desarrollo propio),
     fronteras IA (motor determinista vs IA interpretativa), trazabilidad en
     `/docs`. Solo enlaza archivos existentes; sin enlaces muertos (ADRs/prompts
     van el 13 Oct). (Corrección tras 1ª validación: antes decía solo OpenCode.)
  8. Login: bloqueo documentado como **por nombre de usuario** (verificado en
     `LoginAttemptService`: clave = username normalizado; la IP solo se
     registra en logs) en "Acceso Restringido" + troubleshooting. (Corrección
     tras 1ª validación: el usuario recordaba bloqueo por IP — no es así.)
  9. Nueva sección **🔌 Endpoints JSON**: tabla de los 4 GET JSON reales
     (`/health` pública + 3 con login) + justificación de no-Swagger con enlace
     al plan (coherente con revisión 8 Oct (2)).
  10. Nueva sección **🛟 Troubleshooting**: 7 filas basadas en incidentes reales
     (CSRF 403, lockout login #174, 504s→async #177/#179, 429→rotación #171,
     puerto ocupado, redeploy idempotente #178).
     (Corrección tras 2ª validación: nota de planes gratuitos en "APIs Externas"
     — latencias altas asumidas por diseño en proyecto educativo — y fila de
     504s reenmarcada como comportamiento esperado, no fallo.)
     (Corrección tras 3ª validación: "Su propósito ES" incluye como punto
     principal el desarrollo con IA del Máster Big School; nueva subsección
     "Integración Continua" con los 2 workflows reales verificados +
     mención en competencias DevOps.)
- `readme_aux.md` eliminado (`git rm`, 413 líneas): contenido duplicado y
  desactualizado respecto al README (estructura con HTMX, integraciones
  OpenAI/Anthropic/Google inexistentes, Railway, `pom.xml pendiente`). Verificado
  que nada lo referencia (`grep readme_aux` → 0 resultados). Petición explícita
  del usuario. Contenido único rescatable (objetivos, tipos de reglas, flujo)
  ya cubierto en el README actual.

## Código generado
No aplica (cambio solo-docs, sin código). Ver diff de `README.md` + `git rm readme_aux.md`.

## Decisiones técnicas tomadas
- No inventar badges nuevos (CI no existe): se mantienen los 4 actuales + URL
  de deploy como texto (verificable a mano).
- SonarQube como objetivo, no como hecho: el README no debe mentir antes del
  12 Oct; el DoD del plan sigue exigiendo el Gate A.
- Troubleshooting desde incidentes reales documentados en `/docs`, no genérico.
- Una rama = un tema: este PR es solo README; SonarQube/ADRs van aparte.

## Cobertura de tests y pruebas añadidas
- Sin cambios de código: no se añaden ni modifican tests.
- No se ejecuta `mvn test` (cambio solo `README.md` + borrado de `.md`;
  sin comportamiento que cubrir).
- Verificación runtime Docker omitida: `README.md` no está en la lista de rutas
  con impacto en ejecución (AGENTS.md §3.7) — se indica aquí explícitamente.

## Advertencias de SonarQube o arquitectura
- Ninguna: documentación, sin impacto en arquitectura ni calidad de código.
- Nota: el borrado de `readme_aux.md` reduce ruido documental; el árbol de
  paquetes del README (§ Estructura Detallada) puede haber derivado del código
  real — se deja como está (fuera del alcance: no expandir sin menú).

## Próximos pasos sugeridos
1. Validar esta PR con menú (la tarea se detiene aquí según AGENTS.md §3.8).
2. Tras MERGED (12 Oct): **SonarQube Local** + Quality Gate A + fix critical.
3. Después (13 Oct): **ADRs (4)** + **Prompt Library** (entonces sí enlazarlos
   desde el README si aporta, como tarea aparte).
