# 04 — Validación siempre con menú (menu validation)

## Cuándo
Al terminar cada tarea y ante cualquier decisión con trade-offs.

## Plantilla
> Detén la ejecución y pídeme validación con un menú de opciones (no con
> pregunta abierta), incluyendo el resumen de estado en el propio mensaje:
> rama, `git status --short`, `git diff --stat`, doc creado, resultado de
> tests y estado de contenedores, más checklist de prueba manual con pasos
> literales (URL exacta, clic exacto, resultado visible exacto).

## Ejemplo real
Práctica sistemática en todas las PRs #182-#187: "Validar y continuar /
Pedir cambios". Evita avances no deseados y deja trazabilidad de la decisión
humana (defendible ante tribunal: la IA propone, la persona dispone).
