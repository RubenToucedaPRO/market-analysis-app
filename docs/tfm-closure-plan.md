# Plan de Cierre TFM — 2 Semanas (10 días laborables)

**Objetivo**: Entregar proyecto completo, desplegado, documentado y defendible usando OpenCode como herramienta principal.

**Día 1**: 16 Sep 2026
**Entrega (Día 10 original)**: 25 Sep 2026 (superada — ver recalibración 8 Oct)
**Entrega recalibrada**: 16 Oct 2026 (tag `tfm-v1.0`)

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
> Revisión 8 Oct: mergeadas #173-#181, cero PRs abiertas, `main` en `0783599`
> (suite 1158 tests en verde). **Login TFM ✅** (#174) y **Deploy VPS ✅** (#175
> CI GHCR + #176 hardening; guía validada FASE11→15a: HTTPS 200, backup/restore
> OK, fail2ban, redeploy idempotente verificado 8 Oct con `OK despliegue TFM`).
> Imprevistos de producción 7-8 Oct (504s): suggest async (#177), pull-image en
> `deploy-tfm.sh` (#178), IA async (#179), robustez IA (#180), `env_file` local
> (#181). PR #170 (histórico Railway) quedó CLOSED sin merge. Calendario
> original superado → entrega recalibrada al **16 Oct** (ver plan 8→16 Oct).
> Revisión 8 Oct (2): **OpenAPI/Swagger movido a Future Work** — la app es
> Thymeleaf MVC (formularios con redirect), no REST; el "Try it out" no aporta
> prueba práctica (302/403/CSRF). Se sustituye por tabla de endpoints JSON en
> README Final (sin dependencia `springdoc`).
> Revisión 8 Oct (3): **SonarQube ✅** — Community Build 26.9 en Docker
> (`docker-compose.sonar.yml`), gate OK, ratings A/A/A, 0 BLOCKER/CRITICAL/BUG
> (suite 1158 verde, JaCoCo check OK). Siguiente: ADRs + Prompt Library (13 Oct).
> Revisión 13 Oct: PRs #186 (SonarQube + lote smells, 63→0 issues) y #187
> (fallback IA amistoso) mergeadas. **ADRs (4) + Prompt Library ✅**
> (`docs/adr/`, `docs/prompts/`). Siguiente: Slides Defensa (14 Oct).

---

## Semana 1: Core + Deploy (Día 1-4)

| Día | Bloque | Tarea | Procedimiento |
|-----|--------|-------|---------------|
| **Día 1** | AM | **Sesión Arquitectura** — Verificar/completar `docs/architecture-walkthrough.md` (ya existe; walkthrough `RuleEvaluator` → `AnalyzeAndPersistStockService` → `PolygonAdapter`) | ✅ hecho |
| | PM | **Scoring (a)** — `weight` en `Rule` + migración BD (JPA, mappers, tests) | ✅ hecho (PR #158) |
| **Día 2** | AM | **Scoring (b)** — `threshold` en `Strategy` + score 0-100 en `EvaluateStrategyService` + tests | ✅ hecho (PR #159) |
| | PM | **Scoring (c)** — Mostrar el score en vistas + **(d)** tests scoring + JaCoCo verify | ✅ hecho (PRs #160, #161) |
| **Día 3** | AM | Tests scoring + JaCoCo verify (cierre) | ✅ hecho en (d): 1056/1056 + `mvn verify` |
| | PM | **Login TFM** — 3 intentos + bloqueo 30 min en memoria (por username) + logs sin secretos | ✅ hecho (PR #174 `feature/login-attempt-lockout`, merge 30 Sep) |
| | PM | **Deploy VPS** — Docker/Nginx/SSL, vars entorno, `/health` | ✅ hecho + endurecido (PRs #175, #176; guía `deploy/guia_vps.md` validada FASE11→15a; redeploy idempotente OK 8 Oct) |
| **7–8 Oct** | — | **Imprevistos prod** (fuera del alcance inicial, obligados por 504s): suggest async (#177), pull-image en deploy (#178), IA async (#179), robustez IA (#180), `env_file` local (#181) | ✅ hecho, suite 1158 verde |
| **Día 4** | AM | **OpenAPI/Swagger descartado** — movido a Future Work (ver revisión 8 Oct (2)); no se añade `springdoc` | — |
| | PM | **README Final** — Badges, URL deploy, sección "Desarrollo con IA", troubleshooting + tabla endpoints JSON (`/health`, `/candles`, jobs) | rama → doc → menú → PR |
| | + | **SonarQube Local** (Docker, hoy no configurado) + Quality Gate A + fix critical | `bash` + tests + doc |

> Nota: SonarQube se adelantó al Día 4 PM/junto a README si el Viernes queda como buffer.
> **Regla 10 (`AGENTS.md`)**: ninguna tarea empieza sin la PR anterior en MERGED.
> Hay colchón entre PR y PR para tu merge. El Viernes (Día 5) queda de buffer / bug fixes deploy.

---

## Recalibración 8→16 Oct (alcance completo, entrega 16 Oct)

| Fecha | Bloque | Tarea | Procedimiento |
|-------|--------|-------|---------------|
| **8–9 Oct** | — | **README Final** (badges, URL deploy, "Desarrollo con IA", troubleshooting + tabla endpoints JSON; `springdoc` descartado → Future Work) | rama → doc → menú → PR |
| **12 Oct** | — | **SonarQube Local** (Docker) + Quality Gate A + fix critical | ✅ hecho 8 Oct (adelantado): Community 26.9 + gate OK + 0 critical |
| **13 Oct** | — | **ADRs (4)** + **Prompt Library** (`docs/prompts/`, 8-10 patrones) | ✅ hecho: `docs/adr/` + `docs/prompts/` (9 patrones) |
| **14 Oct** | — | **Slides Defensa** (12 slides + speaker notes) | generar con contexto |
| **15 Oct** | — | **Rehearsal Grabado** (demo 5 min + Q&A) + ajustes finales | — |
| **16 Oct** | AM | **Buffer** — remates, re-verificación deploy + docs | — |
| | PM | **Entrega** — Tag `tfm-v1.0`, repo público, URLs en README, slides link | `bash` (git tag/push) |

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
| Tabla endpoints JSON en README | OpenAPI + Swagger UI |
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
| **Funcional** | Scoring 0-100 funciona, deploy VPS responde (verificado 8 Oct: HTTPS 200 + redeploy idempotente), login bloquea tras 3 fallos durante 30 min (verificado), endpoints JSON documentados en README (Swagger movido a Future Work 8 Oct, sin `springdoc`) |
| **Calidad** | `mvn verify` → BUILD SUCCESS, JaCoCo ≥80% (plugin ya en `pom.xml`), SonarQube Quality Gate A ✅ (verificado 8 Oct: gate OK, ratings A/A/A, 0 BLOCKER/CRITICAL) |
| **Documentación** | README completo, 4 ADRs, Prompt Library, Slides 12 páginas |
| **Entrega** | Repo público, tag `tfm-v1.0`, URLs en README, slides accesibles |

---

## Notas de partida verificadas el Día 1

- `docs/architecture-walkthrough.md` ya existe (personal, 16 Sep): verificar/completar, no crear.
- `Rule` (dominio) tiene campos `final` + validación en constructor: el scoring exige migración BD (tablas `rules`/`strategies` vía `script-bd.sql` + entidades JPA + mappers).
- `terraform/` es del provider GitHub (gestión del repo), irrelevante para el deploy. README apuntaba a Railway; pasa a VPS.
- JaCoCo presente en `pom.xml`; SonarQube ausente (a añadir en su tarea). `springdoc` descartado el 8 Oct (Future Work, no se añade).

---

## Decisiones Confirmadas

1. **Scoring**: Opción B — ponderado 0-100 con pesos por regla + threshold por estrategia (en 4 PRs: weight+migración, threshold+evaluador, vistas, tests+JaCoCo)
2. **Deploy**: VPS (sustituye a Railway, que queda como histórico)
3. **Sesión arquitectura**: Día 1 (verificar walkthrough existente)
4. **Login TFM**: 3 intentos + bloqueo 30 min + en memoria (por username) + logs sin secretos
5. **Features extra**: Ninguna más — scope locked. Modelo predictivo solo en slides como future work.
6. **OpenAPI/Swagger descartado** (8 Oct): app Thymeleaf MVC, no REST — se documentan los 4 endpoints JSON (`/health`, `/candles`, 2× jobs) con tabla en README; `springdoc` queda como future work para cuando exista API pública (p. ej. servicio ML Python separado).

---

## Próximo Paso Inmediato

**Cola real pendiente (hasta ADRs+Prompts hecho — ver revisiones)**:
1. Slides Defensa 12 + speaker notes (14 Oct).
2. Rehearsal grabado + ajustes (15 Oct).
3. Buffer + tag `tfm-v1.0` (16 Oct).

## Recuperar contexto instantáneo en nueva sesión OpenCode
Leer `docs/tfm-closure-plan.md`, `AGENTS.md` §3 y `docs/architecture-walkthrough.md`
