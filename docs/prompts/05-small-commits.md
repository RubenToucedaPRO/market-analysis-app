# 05 — Commits pequeños por tipo (small commits)

## Cuándo
Durante toda tarea con varios cambios.

## Plantilla
> Commitea por tipo durante la tarea (`fix:` código, `test:` tests, `docs:`
> documentación, `chore:`, `refactor:`) en vez de un único commit gigante al
> final. Prohibido push directo a `main`, `--force` y `--amend` sobre rama ya
> publicada. Commits convencionales en inglés, descriptivos.

## Ejemplo real
PR #186 (SonarQube): 26 commits (`fix: use @Repository…`, `test: join
assertion chain…`, `refactor:…`, `docs:…`, `chore:…`) — cada uno revisable
y revertible por separado, incluido el del propio usuario (S8688).
