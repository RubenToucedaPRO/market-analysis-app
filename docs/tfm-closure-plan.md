# Plan de Cierre TFM — 2 Semanas (10 días laborables)

**Objetivo**: Entregar proyecto completo, desplegado, documentado y defendible usando OpenCode como herramienta principal.

**Día 1**: 16 Sep 2026
**Entrega (Día 10)**: 25 Sep 2026 (tag `tfm-v1.0`)

> Revisión 16 Sep: fechas por número de día (los días de semana del borrador no cuadraban).
> Revisión 23 Sep: deploy Railway terminado y verificado (app en
> `market-analysis-app-production.up.railway.app`, `/health` UP, fix batch
> velas 41.6→5.9s con PR #169). Siguiente: OpenAPI/Swagger (Día 4 AM).
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
| | PM | **Deploy Railway** — Provision BD, vars entorno, health checks, custom domain | ✅ hecho (PRs #167-#169, app UP en Railway, fix batch 41.6→5.9s) |
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
| Deploy Railway funcional | Alertas email/Telegram |
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
| **Funcional** | Scoring 0-100 funciona, deploy Railway responde, Swagger carga |
| **Calidad** | `mvn verify` → BUILD SUCCESS, JaCoCo ≥80% (plugin ya en `pom.xml`), SonarQube Quality Gate A (pendiente de configurar) |
| **Documentación** | README completo, 4 ADRs, Prompt Library, Slides 12 páginas |
| **Entrega** | Repo público, tag `tfm-v1.0`, URLs en README, slides accesibles |

---

## Notas de partida verificadas el Día 1

- `docs/architecture-walkthrough.md` ya existe (personal, 16 Sep): verificar/completar, no crear.
- `Rule` (dominio) tiene campos `final` + validación en constructor: el scoring exige migración BD (tablas `rules`/`strategies` vía `script-bd.sql` + entidades JPA + mappers).
- `terraform/` es del provider GitHub (gestión del repo), irrelevante para el deploy. README ya apunta a Railway.
- JaCoCo presente en `pom.xml`; SonarQube y `springdoc` ausentes (a añadir en sus tareas).

---

## Decisiones Confirmadas

1. **Scoring**: Opción B — ponderado 0-100 con pesos por regla + threshold por estrategia (en 4 PRs: weight+migración, threshold+evaluador, vistas, tests+JaCoCo)
2. **Deploy**: Railway
3. **Sesión arquitectura**: Día 1 (verificar walkthrough existente)
4. **Features extra**: Ninguna — scope locked. Modelo predictivo solo en slides como future work.

---

## Próximo Paso Inmediato

**OpenAPI/Swagger (Día 4 AM)**:
1. Añadir `springdoc-openapi-starter-webmvc-ui` a `pom.xml` (hoy ausente)
2. Documentar endpoints clave
3. Procedimiento `AGENTS.md` §3 (rama → tests → doc → menú → PR)

## Recuperar contexto instantáneo en nueva sesión OpenCode
Leer `docs/tfm-closure-plan.md`, `AGENTS.md` §3 y `docs/architecture-walkthrough.md`
