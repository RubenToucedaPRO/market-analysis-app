# 03 — Corregir por lotes agrupados por regla (fix by rule batch)

## Cuándo
Deuda de calidad numerosa (p. ej. 54 issues SonarQube): nunca uno a uno a mano.

## Plantilla
> Saca el desglose por regla (API/facets o UI). Agrupa por tipo de fix
> (constantes, complejidad, estilo, tests). Por grupo: aplica el patrón a
> todos los sitios a la vez, corre los tests afectados y re-mide para confirmar
> que el grupo baja a 0. Lo ambiguo o con trade-off se propone con menú antes
> de tocarlo.

## Ejemplo real
Lote SonarQube oct 2026 (PR #186): 54 issues → 16 reglas → grupos S5673 (11×
una edición mecánica), S5778 (14× un patrón), S5976 (3 conversiones), etc.
De 63 a 0 issues con commits por grupo y re-análisis tras cada tanda.
