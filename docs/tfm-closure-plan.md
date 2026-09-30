# Plan de Cierre TFM — 2 Semanas (10 días laborables)

**Objetivo**: Entregar proyecto completo, desplegado, documentado y defendible usando OpenCode como herramienta principal.

**Día 1**: 16 Sep 2026
**Entrega (Día 10)**: 25 Sep 2026 (tag `tfm-v1.0`)

> Revisión 16 Sep: fechas por número de día (los días de semana del borrador no cuadraban).
> Revisión 17 Sep: scoring (a-d) terminado y mergeado (PRs #158-#161, suite
> 1056/1056 + `mvn verify` con JaCoCo ≥80%). Siguiente: Deploy Railway (Día 3 PM).
> Revisión 30 Sep: giro de Deploy Railway a **Deploy VPS** (Railway queda como
> histórico; PR #170 `docs/railway-deploy` sigue OPEN pendiente de cierre).
> Nuevo alcance: **Login TFM** (3 intentos + bloqueo 30 min en memoria + logs sin
> secretos). Desde el 17 Sep se mergeó además: throttles Finnhub/Polygon
> (#163, #164), suggest tickers (#165), filtros de rango (#166), puerto Railway
> + `/health` pública (#167), env config (#168), batch de velas (#169), rotación
> OpenRouter ante 429 (#171) y bind local a localhost (#172). Siguiente: Login TFM.
> Procedimiento aplicable en todo el plan: `AGENTS.md` §3
> (rama → tests → doc → Docker si aplica → validación con menú → PR).
> Los comandos `opencode explain/generate/...` del borrador no existen en esta
> sesión y se sustituyen por ese procedimiento.

---

## Semana 1: Core + Deploy (Día 1-4)

| Día | Bloque | Tarea | Procedimiento |
|-----|--------|-------|---------------|
| **Día 1** | AM | **Sesión Arquitectura** — Verificar/completar `docs/architecture-walkthrough.md` (ya existe; walkthrough `RuleEvaluator` → `AnalyzeAndPersistStockService` → `PolygonAdapter`) | ✅ hecho |
| | PM | **Scoring (a)** — `weight` en `Rule` + migración BD (JPA, mappers, tests) | ✅ hecho (PR #158) |
| **Día 2** | AM | **Scoring (b)** — `threshold` en `Strategy` + score 0-100 en `EvaluateStrategyService` + tests | ✅ hecho (PR #159) |
| | PM | **Scoring (c)** — Mostrar el score en vistas + **(d)** tests scoring + JaCoCo verify | ✅ hecho (PRs #160, #161) |
| **Día 3** | AM | Tests scoring + JaCoCo verify (cierre) | ✅ hecho en (d): 1056/1056 + `mvn verify` |
| | PM | **Login TFM** — 3 intentos + bloqueo 30 min en memoria (por username) + logs sin secretos | rama `feature/login-attempt-lockout` → tests → doc → menú → PR |
| | PM | **Deploy VPS** (bloqueado hasta la guía de despliegue) — Docker/Nginx/SSL, vars entorno, `/health` | cambios en rama `chore/vps-deploy` + doc → merge → despliegue ejecutado sobre `main` |
| **Día 4** | AM | **OpenAPI/Swagger** — añadir `springdoc-openapi-starter-webmvc-ui` (hoy no está en `pom.xml`) + documentar endpoints clave | rama → tests → doc → menú → PR |
| | PM | **README Final** — Badges, URL deploy, sección "Desarrollo con IA", troubleshooting | rama → doc → menú → PR |
| | + | **SonarQube Local** (Docker, hoy no configurado) + Quality Gate A + fix critical | `bash` + tests + doc |

> Nota: SonarQube se adelantó al Día 4 PM/junto a README si el Viernes queda como buffer.
> **Regla 10 (`AGENTS.md`)**: ninguna tarea empieza sin la PR anterior en MERGED.
> Hay colchón entre PR y PR para tu merge. El Viernes (Día 5) queda de buffer / bug fixes deploy.

---

## Semana 2: Docs Metodológicos + Defensa (Día 6-10)

| Día | Bloque | Tarea | Procedimiento |
|-----|--------|-------|---------------|
| **Día 6** | AM | **ADRs (4)** — Hexagonal, Rule Engine, IA Boundaries, Testing Strategy | generar desde plantilla + menú → PR |
| | PM | **Prompt Library** — `docs/prompts/` con 8-10 patrones reutilizables usados | generar + menú → PR |
| **Día 7** | AM | **Slides Defensa** — 12 slides: Problema → Arquitectura → IA Workflow → Demo → Métricas → Lecciones → Futuro | generar con contexto |
| | PM | Refinamiento slides + speaker notes | — |
| **Día 8** | AM | **Rehearsal Grabado** — Demo end-to-end 5 min + Q&A simulado | — |
| | PM | Ajustes finales código/docs | — |
| **Día 9** | — | **Buffer** — remates, re-verificación deploy + docs | — |
| **Día 10** | — | **Entrega** — Tag `tfm-v1.0`, repo público, URLs en README, slides link | `bash` (git tag/push) |

---

## Scope Locked (No Feature Creep)

| ✅ Incluido | ❌ Excluido (Future Work en slides) |
|-------------|-------------------------------------|
| Scoring ponderado 0-100 | Modelo predictivo Python/ML |
| Deploy VPS funcional | Alertas email/Telegram |
| Login hardening (3 intentos / 30 min / logs sin secretos) | Portfolio tracking |
| OpenAPI + Swagger UI | Portfolio tracking |
| ADRs + Prompt Library | Multi-strategy watchlist |
| Slides + Rehearsal | Backtesting histórico walk-forward |
| SonarQube Quality Gate A | Real-time WebSocket quotes |

> Regla vigente hasta la entrega: toda idea que pida código nuevo va a "Futuro",
> no a esta semana (norma de `AGENTS.md`: no expandir alcance sin menú).

> **Nota modelo predictivo**: Menciónalo en slides como *"Línea futura: servicio ML separado (Python/FastAPI) consumiendo datos persistidos vía API interna, desacoplado del motor determinista"*.

---

## Criterios de Done (Definition of Done)

| Criterio | Verificación |
|----------|--------------|
| **Funcional** | Scoring 0-100 funciona, deploy VPS responde (`/health` UP), login bloquea tras 3 fallos durante 30 min, Swagger carga |
| **Calidad** | `mvn verify` → BUILD SUCCESS, JaCoCo ≥80% (plugin ya en `pom.xml`), SonarQube Quality Gate A (pendiente de configurar) |
| **Documentación** | README completo, 4 ADRs, Prompt Library, Slides 12 páginas |
| **Entrega** | Repo público, tag `tfm-v1.0`, URLs en README, slides accesibles |

---

## Notas de partida verificadas el Día 1

- `docs/architecture-walkthrough.md` ya existe (personal, 16 Sep): verificar/completar, no crear.
- `Rule` (dominio) tiene campos `final` + validación en constructor: el scoring exige migración BD (tablas `rules`/`strategies` vía `script-bd.sql` + entidades JPA + mappers).
- `terraform/` es del provider GitHub (gestión del repo), irrelevante para el deploy. README apuntaba a Railway; pasa a VPS.
- JaCoCo presente en `pom.xml`; SonarQube y `springdoc` ausentes (a añadir en sus tareas).

---

## Decisiones Confirmadas

1. **Scoring**: Opción B — ponderado 0-100 con pesos por regla + threshold por estrategia (en 4 PRs: weight+migración, threshold+evaluador, vistas, tests+JaCoCo)
2. **Deploy**: VPS (sustituye a Railway, que queda como histórico)
3. **Sesión arquitectura**: Día 1 (verificar walkthrough existente)
4. **Login TFM**: 3 intentos + bloqueo 30 min + en memoria (por username) + logs sin secretos
5. **Features extra**: Ninguna más — scope locked. Modelo predictivo solo en slides como future work.

---

## Próximo Paso Inmediato

**Login TFM + Deploy VPS (pendiente de guía)**:
1. Login TFM (sin bloqueo): `LoginAttemptService` en memoria (3 intentos / 30 min /
   por username) + handlers de éxito/fallo + logs WARN/INFO sin secretos +
   mensaje `login.locked` i18n. Tests unitarios + MockMvc, doc en `/docs`.
2. Deploy VPS (bloqueado hasta recibir la guía; se ejecuta sobre `main` una vez
   mergeados los cambios de config): la guía debe traer SO/acceso SSH,
   stack (Docker Compose o jar+systemd), Nginx + SSL, dominio/DNS, ubicación de
   secretos, perfil Spring prod y healthcheck esperado (`GET /health` → 200 +
   `database_healthy:true`). Documentar en `/docs` con el procedimiento `AGENTS.md` §3.

## Recuperar contexto instantáneo en nueva sesión OpenCode
Leer `docs/tfm-closure-plan.md`, `AGENTS.md` §3 y `docs/architecture-walkthrough.md`
