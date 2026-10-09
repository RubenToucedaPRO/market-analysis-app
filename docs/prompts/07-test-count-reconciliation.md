# 07 — Cuadrar nº de tests en refactors (test count reconciliation)

## Cuándo
Al fusionar, parametrizar o reestructurar tests.

## Plantilla
> Compara ejecuciones antes/después caso a caso (nombres en surefire XML),
> no solo totales de consola: cada caso eliminado debe tener su equivalente
> parametrizado. Si falta alguno, es pérdida de cobertura real, no ruido.

## Ejemplo real
Fusión S4144 eliminó sin querer 4 casos (menos código pero menos cobertura).
Detectado comparando sets de casos baseline-vs-rama (1158=1158); restaurado
y re-verificado antes del merge. Doc: tarea SonarQube § Advertencias.
