# Task 2026-10-08/09 — SonarQube Local + Quality Gate A + cero issues

## Título
SonarQube Community local con Docker, Quality Gate OK, cero issues abiertas
(63→0) y cobertura en código nuevo ≥94% (adelanta el bloque del 12 Oct).

## Resumen
- Rama `feature/sonarqube-quality-gate` (desde `main` post-#185, limpio, 0 PRs).
- Nuevo `docker-compose.sonar.yml` (fichero separado: no interfiere con el
  `docker-compose.yml` de la app): SonarQube Community con imagen fijada por
  digest (Community Build **26.9.0.129388** vigente; el label
  `org.opencontainers.image.version=24.04` de la imagen está obsoleto).
  Requisito ES `vm.max_map_count=1048576` verificado (mínimo 262144).
  Puerto publicado solo en `127.0.0.1:9000`.
- Proyecto `market-analysis-app` creado vía Web API + tokens de análisis de
  proyecto (temporales, no versionados; revocar al cerrar la tarea).
- **Fase 1 (fix critical)**: 4 BLOCKER S2699 (tests sin asserts → asserts reales),
  2 CRITICAL (S3776: extraer `checkRangePair`; S1192: constante
  `TICKER_REQUIRED_MESSAGE`), 2 BUG (S2259: guardia de nulidad documentada,
  luego refinada a `Optional`; S5841: `isNotEmpty()` previo).
- **Fase 2 (lote smells, 54→0, a petición del usuario)**: agrupados por regla —
  S5673 (11× `@Component`→`@Repository`), S5778 (14× lambdas de una sola
  invocación: hoist de construcciones + `EntryPrice.of` inválidos asertados
  directamente), S5976 (3 tríos → `@ParameterizedTest`/`@MethodSource`),
  S1130 (8× `throws Exception` imposibles), S1128 (4 imports), S1068
  (campos muertos + eliminado `allowedOperators` sin uso de `RuleCapability`
  con sus 18 call-sites del catálogo), S135 (2× bucles a salida única),
  S1172 (parámetro `context` sin uso), S4144 (tests duplicados fusionados),
  S6068/S8924/S1612/S5853/S8714 (asserts/imports/method-ref), S2925
  (supresión justificada: espera real de 400ms en test de throttler) y S8688
  (`ZoneId` explícito, cambio del usuario).
- **Cobertura en código nuevo**: 5 tests añadidos donde faltaba —
  `findLatestCandleByTicker` (2), paginación agotada + páginas llenas (2),
  bounds en orden inverso (1).
- **`pom.xml`**: `sonar.coverage.exclusions` que refleja los excludes de JaCoCo
  (imprescindible: sin esto Sonar exige cobertura a ficheros que JaCoCo ignora
  — VOs de `domain/model`, DTOs, configs — y el gate no puede pasar).
- **Resultado final**: gate OK (new_coverage ≥94.8%, 0 violaciones, duplicación
  0.0), 0 issues abiertas (63→0), ratings A/A/A, 0 vulnerabilidades,
  0 hotspots, suite **1163/1163** (1158 + 5 tests nuevos) + JaCoCo OK,
  cobertura global 89.0%.
- `docs/tfm-closure-plan.md`: revisión 8 Oct (3), fila 12 Oct marcada hecha,
  DoD Calidad actualizado, cola renumerada (siguiente: ADRs + Prompts 13 Oct).
- `README.md`: "Quality Gate A (objetivo)" → "(verificado, Community Build local)".

## Código generado
1. `docker-compose.sonar.yml` (nuevo): servicio `market-analysis-sonarqube`
   + 3 volúmenes (data/extensions/logs) + comentarios de uso.
2. `pom.xml`: propiedad `sonar.coverage.exclusions` (ver Decisiones).
3. `FinvizFilterMapperImpl.java`: `checkRangePair` + `mapSingleRule`
   extraídos; `resolveTargetParamForFilter` devuelve `Optional` (sin rama
   muerta inalcanzable y S2259 resuelto de forma que el tribunal también lo ve
   limpio desde cero).
4. `JsoupFinvizAdapter.java`: `advancePaging` con salida única + `Objects::nonNull`.
5. `SqlCandleHistoryRepository.java`: constante + `@Repository`.
   Resto de `Sql*`: `@Repository` (11 ficheros).
6. `RuleCapability.java` + `RuleCapabilityCatalog.java`: eliminado
   `allowedOperators` (siempre idéntico a `VALID_OPERATORS` global y sin
   lectores); `RiskRewardCalculator.java`: eliminado parámetro `context`.
7. `PolygonAdapter.java` (usuario): `LocalDate.now(ZoneId America/New_York)`.
8. Tests: asserts reales (S2699), `isNotEmpty` (S5841), lambdas de una sola
   invocación (S5778), 3 tríos parametrizados (S5976), fusión de duplicado
   (S4144), limpieza throws/imports/fields/asserts (S1130/S1128/S1068/S6068/
   S8924/S8714/S1612/S5853), supresión justificada S2925, 5 tests nuevos de cobertura.
9. Deduplicación throttlers (a petición del usuario en esta PR):
   `PolygonThrottler`/`FinnhubThrottler` eran el mismo algoritmo con 3
   diferencias (claves/defaults de config y nombre en log). Nueva clase común
   `infrastructure/external/shared/SlidingWindowThrottler` (algoritmo una sola
   vez) + subclases finas que conservan inyección (`@Component`), firmas de
   constructor (tests intactos) y mensajes de log idénticos. Adapters sin
   cambios. Gate sigue OK (new_coverage 93.9%, 0 violaciones), 0 issues.

## Decisiones técnicas tomadas
- Scanner vía coordenadas Maven completas y fijadas
  (`sonar-maven-plugin:4.0.0.4121`): sin plugin en el `pom` (el análisis es
  actividad local, no dependencia del build); solo se añadió la propiedad de
  exclusiones por reproducibilidad (tribunal/CI).
- Primer fallo del análisis por `sonar.projectKey`: el scanner derivaba
  `com.market.analysis:market-analysis-app` y el token de proyecto no podía
  crearlo → `-Dsonar.projectKey=market-analysis-app` explícito.
- S2259 se resolvió en código (primero guardia, luego `Optional`) en vez de
  "Won't fix" en la UI, para que un análisis desde cero también salga limpio.
- S2925 se suprime con justificación en código (mejor que estado UI, que no
  viaja en git): el test necesita espera real de reloj.
- `allowedOperators` se elimina (YAGNI: 18 call-sites con el mismo valor
  global y cero lectores) en vez de inventarle un uso.
- SonarQube se deja **levantado** para verificación manual
  (`http://localhost:9000`); parar con
  `docker compose -f docker-compose.sonar.yml down` (datos en volúmenes).

## Cobertura de tests y pruebas añadidas
- 5 tests nuevos (paginación ×2, findLatest ×2, bounds inversos ×1); resto:
  asserts/estructura en tests existentes. Sin `lenient`.
- `mvn -B verify` desde cero (`rm -rf target`): **1163 tests, 0 fallos**,
  JaCoCo check OK. Conteo cuadra: 1158 + 5 nuevos (las conversiones
  parametrizadas conservan nº de ejecuciones, verificado caso a caso).
- Verificación runtime Docker (§3.7, cambian `src/` y `pom.xml`):
  rebuild `up --build -d` OK (dos veces: tras fixes base y tras lote final),
  `market-analysis-mysql` Healthy, `GET /` → 200.

## Advertencias de SonarQube o arquitectura
- **Incidente sed/S1130**: el `sed` para quitar `throws Exception` comió los
  paréntesis (`void x) {`) en 2 ficheros; la compilación incremental no lo
  detectó y varios runs "verdes" usaron clases obsoletas (origen de los
  números contradictorios de cobertura). Detectado con `rm -rf target` +
  build limpio, reparado, y verificado todo desde cero. Lección: ante
  resultados incoherentes, build limpio antes de teorizar; preferir `Edit`.
- **Incidente S4144**: una fusión de tests eliminó sin querer 4 casos
  (menos código pero menos cobertura). Detectado comparando casos
  baseline-vs-rama (1158=1158), restaurado y re-verificado. Lección: en
  refactors de tests, comparar nº de ejecuciones antes/después.
- **Incidente new_coverage**: el gate pedía 80% y daba 60-72% con JaCoCo
  local al 99%: JaCoCo excluye `domain/model`, DTOs y configs, pero SonarQube
  les exigía cobertura. Solución: `sonar.coverage.exclusions` en el `pom`
  reflejando los excludes (gate OK, global 89.0%).
- Nota: el exclude JaCoCo `domain/exceptions/**` (plural) no coincide con el
  paquete real `domain/exception` (singular) y no excluye nada; en Sonar se
  usó la ruta correcta. Queda como higiene futura alinear el `pom`.
- Riesgo aceptado (no bloqueante): SonarLint avisa en `pom.xml:1` de que el
  soporte OSS de Spring Boot 3.5.x terminó el 2026-06-30 (solo comercial vía
  Tanzu). No se migra a Boot 4.x antes de la entrega (alcance bloqueado,
  regresión mayor a 7 días del TFM); la app es un entregable académico
  congelado, no un sistema con necesidad de ventana de soporte. Propuesto como
  línea de trabajo futuro en slides. No aparece en el servidor (0 issues).
- Dominio sigue sin imports de Spring/Jakarta.

## Próximos pasos sugeridos
1. Validar esta PR con menú (la tarea se detiene aquí según AGENTS.md §3.8).
2. Tras MERGED (13 Oct): **ADRs (4)** + **Prompt Library**.
3. Revocar los tokens temporales de SonarQube (My Account → Security).
