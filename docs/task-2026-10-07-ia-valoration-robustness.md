# Task 2026-10-07 — Robustez valoración IA (validador tolerante + evidencia)

- **Rama:** `fix/ia-valoration-robustness` (desde `main` = `bba0bb7`)
- **Alcance:** validador de respuesta IA + log de evidencia. Sin cambios en prompts, modelos, workers async ni esquema BD.
- **Fecha:** 2026-10-07.

---

## 1. Resumen de la tarea

En producción, "Generar análisis" fallaba sistemáticamente (`fallbackRatio≈0.57`): el LLM respondía, pero `PromptResponseValidator` rechazaba el texto dos veces (inicial + retry) y se persistía el fallback. El flujo async (B1) funcionaba bien —el fallo era de contenido, no de plumbing— pero las respuestas crudas no se registraban en ningún sitio: diagnóstico a ciegas.

## 2. Código generado

| Fichero | Cambio |
|---|---|
| `domain/service/ValorationSections.java` (nuevo) | Regla única de frontera: cabecera válida solo si **abre línea** (tras markdown/números/espacios), insensible a mayúsculas/tildes; cuerpo por sección ≥40 chars. `isStructured` + `split` (4 cuerpos en orden, nunca null) |
| `domain/service/PromptResponseValidator.java` | `isValid` delega en `ValorationSections.isStructured` (se elimina su `normalize` duplicado) |
| `domain/service/PromptBuilder.java` | Línea anti-eco: no repetir descripciones ni explicar el formato |
| `application/dto/ValorationSectionsDTO.java` (nuevo) | `from(raw)`: `structured` + 4 partes (vacías si no estructurado, preservando la tarjeta vacía actual) |
| `presentation/controller/AnalyzeTickerController.java` | GET detalle añade `valorationSections` solo si hay texto (mismo comportamiento que antes) |
| `templates/fragments/ai-valoration.html` | Recibe el DTO partido; fuera la cirugía `substringAfter/Before` |
| `application/usecase/ManageAnalyzeStockService.java` | WARN con la respuesta cruda (una línea, truncada a 2000 chars) en los dos rechazos + helper `preview()` |
| `ValorationSectionsTest.java` (nuevo) + `PromptResponseValidatorTest.java` | 5 + 5 tests: caso eco de captura real (regresión), variantes, cabeceras a mitad de frase, cuerpos cortos, nulos |

## 3. Decisiones técnicas tomadas

1. **Evidencia primero**: sin la respuesta cruda en el log es imposible distinguir truncado de deriva de formato. El `preview()` la deja legible en una línea sin inundar el log.
2. **Estructural, no solo tolerante**: tras ver una captura real donde el eco pasaba el `contains`, la regla exige cabecera **abriendo línea** + cuerpo ≥40 chars. El eco de una línea (~33 chars) y el apelotonado bajo la última cabecera ya no cuelan; un falso negativo solo provoca retry/fallback honesto, nunca una tarjeta mal asignada.
3. **Fuente única de frontera**: `ValorationSections` la usan el validador y la vista; imposible que validen una cosa y partan por otra.
4. **Prompt mínimo, sin tocar modelo, temperatura ni workers**: solo una línea anti-eco (no repetir descripciones ni explicar el formato). El ajuste fino (temperatura 0.7→0.2, max-tokens 500→1000) queda como cambio de `.env` en el VPS (sección 6A), reversible sin deploy.
5. **Defaults de `application.properties` intactos**: el tuning es operativo (prod), no cambio de código para todos los entornos.

## 4. Cobertura de tests y pruebas

- `ValorationSectionsTest`: 5 tests (incluye regresión con el eco de la captura real), `PromptResponseValidatorTest`: 9 tests, `ManageAnalyzeStockServiceTest` y controlador/vista IA intactos salvo lo descrito.
- **Suite completa: 1149 tests, 0 failures, BUILD SUCCESS.**
- **JaCoCo: `ValorationSections` 100% líneas y ramas; `PromptResponseValidator` 100%.**
- Validación real pendiente: desplegar imagen nueva, reintentar PL y leer el log (si sigue fallando, la respuesta cruda dirá por qué).

## 5. Advertencias de SonarQube / arquitectura

- Regex sencillas, sin anidamiento; `Normalizer` de `java.text` (sin dependencias).
- Hexagonal: validador sigue en dominio puro; logging en Application. Sin cambios de contrato salvo tolerancia documentada.

## 6. Próximos pasos sugeridos

### A. Operativo en el VPS (sin deploy de código, tú mismo)
1. En `/opt/apps/tfm/.env`: añadir `OPENROUTER_TEMPERATURE=0.2` y `OPENROUTER_MAX_TOKENS=1000` (cambian `0.7`/`500` por defecto).
2. `cd /opt/apps/tfm && docker compose up -d app` (recrea con el nuevo entorno; la BD no se toca).
3. Reintentar "Generar análisis" en PL y mirar el log: o sale bien, o el WARN muestra el texto exacto.

### B. Tras desplegar la imagen con este fix
1. Si el WARN muestra truncado (`…[truncated]`): confirma que el paso A3 era necesario.
2. Si muestra otro formato: ajustar validador o prompt según evidencia.
3. Valorar `temperature=0.2` como default en `application.properties` si rinde mejor en general.

### C. Ideas futuras (no hacer ahora)
1. Métrica/alertado sobre `fallbackRatio` en Actuator en vez de leer logs a mano.
