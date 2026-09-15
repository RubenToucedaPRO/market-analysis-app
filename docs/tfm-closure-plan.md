# Plan de Cierre TFM — 2 Semanas (10 días laborables)

**Objetivo**: Entregar proyecto completo, desplegado, documentado y defendible usando OpenCode como herramienta principal.

**Fecha inicio**: Martes 16 Sep 2026  
**Fecha objetivo entrega**: Jueves 25 Sep 2026 (tag `tfm-v1.0`)

---

## Semana 1: Core + Deploy (16-19 Sep)

| Día | Bloque | Tarea | OpenCode Pattern |
|-----|--------|-------|------------------|
| **Mar 16** | AM | **Sesión Arquitectura** — Walkthrough `RuleEvaluator` → `AnalyzeAndPersistStockService` → `PolygonAdapter` + crear `docs/architecture-walkthrough.md` personal | `explain` + `generate` |
| | PM | **Scoring Ponderado** — Añadir `weight` a `Rule`, `threshold` a `Strategy`, modificar `EvaluateStrategyService` → score 0-100 | `refactor --test` |
| **Mié 17** | AM | Tests scoring + JaCoCo verify | `test --coverage --fix` |
| | PM | **Deploy Railway** — Provision BD, vars entorno, health checks, custom domain | `bash` (manual Railway CLI) |
| **Jue 18** | AM | **OpenAPI/Swagger** — `springdoc-openapi-starter-webmvc-ui` + documentar endpoints clave | `generate` |
| | PM | **README Final** — Badges, URL deploy, sección "Desarrollo con IA", troubleshooting | `edit` |
| **Vie 19** | AM | **SonarQube Local** (Docker) + Quality Gate A + fix critical | `bash` + `test` |
| | PM | Buffer / bug fixes deploy | — |

---

## Semana 2: Docs Metodológicos + Defensa (22-25 Sep)

| Día | Bloque | Tarea | OpenCode Pattern |
|-----|--------|-------|------------------|
| **Lun 22** | AM | **ADRs (4)** — Hexagonal, Rule Engine, IA Boundaries, Testing Strategy | `generate --template` |
| | PM | **Prompt Library** — `docs/prompts/` con 8-10 patrones reutilizables usados | `generate` |
| **Mar 23** | AM | **Slides Defensa** — 12 slides: Problema → Arquitectura → IA Workflow → Demo → Métricas → Lecciones → Futuro | `generate --context` |
| | PM | Refinamiento slides + speaker notes | — |
| **Mié 24** | AM | **Rehearsal Grabado** — Demo end-to-end 5 min + Q&A simulado | — |
| | PM | Ajustes finales código/docs | — |
| **Jue 25** | — | **Entrega** — Tag `tfm-v1.0`, repo público, URLs en README, slides link | `bash` (git tag/push) |

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

> **Nota modelo predictivo**: Menciónalo en slides como *"Línea futura: servicio ML separado (Python/FastAPI) consumiendo datos persistidos vía API interna, desacoplado del motor determinista"*.

---

## Criterios de Done (Definition of Done)

| Criterio | Verificación |
|----------|--------------|
| **Funcional** | Scoring 0-100 funciona, deploy Railway responde, Swagger carga |
| **Calidad** | `mvn verify` → BUILD SUCCESS, JaCoCo ≥80%, SonarQube Quality Gate A |
| **Documentación** | README completo, 4 ADRs, Prompt Library, Slides 12 páginas |
| **Entrega** | Repo público, tag `tfm-v1.0`, URLs en README, slides accesibles |

---

## Comandos OpenCode Clave

```bash
# Mar AM - Arquitectura
opencode explain src/main/java/com/market/analysis/domain/service/RuleEvaluator.java
opencode explain src/main/java/com/market/analysis/application/usecase/AnalyzeAndPersistStockService.java
opencode generate "Personal architecture walkthrough markdown" --output docs/architecture-walkthrough.md

# Mar PM - Scoring
opencode refactor "Add weight field to Rule, threshold to Strategy, weighted scoring in EvaluateStrategyService" --test

# Mié AM - Tests
opencode test --coverage --fix

# Lun AM - ADRs
opencode generate "ADR: Hexagonal Clean Architecture" --template docs/adr/template.md --output docs/adr/001-hexagonal-clean.md

# Mar AM - Slides
opencode generate "TFM defense slide deck outline 12 slides" --context README.md,docs/ --output docs/slides-outline.md
```

---

## Decisiones Confirmadas

1. **Scoring**: Opción B — ponderado 0-100 con pesos por regla + threshold por estrategia
2. **Deploy**: Railway
3. **Sesión arquitectura**: Hoy martes 16 (AHORA)
4. **Features extra**: Ninguna — scope locked. Modelo predictivo solo en slides como future work.

---

## Próximo Paso Inmediato

**Sesión arquitectura AHORA** (30-45 min):
1. `opencode explain` sobre `RuleEvaluator`, `AnalyzeAndPersistStockService`, `PolygonAdapter`
2. Crear `docs/architecture-walkthrough.md` personal
3. Empezar scoring ponderado esta tarde

## Recuperar contexto instantáneo en nueva sessión OpenCode
opencode read docs/tfm-closure-plan.md
opencode read docs/architecture-walkthrough.md  