# 06 — Build limpio ante la duda (clean build on doubt)

## Cuándo
Resultados incoherentes: cobertura que no cuadra, tests que pasan cuando no
deberían, errores que aparecen/desaparecen solos.

## Plantilla
> No teorices más: `rm -rf target` + build completo desde cero y re-mide.
> La compilación incremental puede enmascarar errores (clases obsoletas) e
> invalidar cualquier conclusión sobre cobertura o tests.

## Ejemplo real
Un `sed` rompió 2 ficheros de test sin que ningún run "verde" lo detectara
(compilación incremental ciega); el build limpio lo reveló al instante.
Igual con los números de cobertura: solo el build limpio dio datos fiables
para cuadrar JaCoCo vs SonarQube. Doc:
`docs/task-2026-10-08-sonarqube-quality-gate.md` § Advertencias.
