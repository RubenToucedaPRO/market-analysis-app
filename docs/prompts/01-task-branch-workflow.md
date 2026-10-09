# 01 — Flujo de tarea por rama (task-branch workflow)

## Cuándo
Toda tarea con cambio de código o docs: features, fixes, docs, chores.

## Plantilla
> Antes de generar código: sincroniza `main` (`git fetch origin && git checkout
> main && git pull --ff-only origin main`; aborta si `git status --porcelain`
> no está vacío). Crea `feature/<slug>` o `fix/<slug>` (kebab-case). Implementa,
> ejecuta tests, documenta en `/docs/task-YYYY-MM-DD-<slug>.md`, valida con
> menú y solo entonces commit + push + PR. Una rama = un tema = un doc.
> Nunca empieces una tarea con la PR anterior sin mergear (regla 10).

## Ejemplo real
`fix/ia-fallback-friendly-message` → PR #187: rama creada desde `main`
sincronizado post-#186, 2 commits (`fix:` + `docs:`), validación con menú
antes del push. Doc: `docs/task-2026-10-09-ia-fallback-friendly-message.md`.
