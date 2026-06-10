# Tarea: Escalado /1000 en reglas estaticas de volumen para Finviz

## Resumen de la tarea
Se implemento el comportamiento solicitado para que, cuando:
- `subjectCode` sea `VOLUME` o `AVG_VOLUME`
- y `targetCode` sea `VALUE` o `CONSTANT`

el valor objetivo se divida entre 1000 antes de generar el sufijo del filtro Finviz.

## Codigo generado
Archivo modificado principal:
- `src/main/java/com/market/analysis/domain/service/FinvizFilterMapperImpl.java`

Cambios clave:
1. Se agrego escalado `/1000` para reglas con valor estatico de volumen:
   - `VOLUME` + (`VALUE` o `CONSTANT`)
   - `AVG_VOLUME` + (`VALUE` o `CONSTANT`)
2. Se mantuvo el mapeo en `MAPPINGS`.
3. Se agregaron entradas para `VOLUME` contra valor estatico:
   - `sh_curvol_o`
   - `sh_curvol_u`

## Cobertura de tests
Archivo actualizado:
- `src/test/java/com/market/analysis/unit/domain/service/FinvizFilterMapperTest.java`

Escenarios verificados:
- `PRICE` estatico no escala (sin cambios)
- `VOLUME` estatico escala /1000
- `AVG_VOLUME` estatico escala /1000

Resultado de ejecucion:
- 9 tests ejecutados
- 9 tests correctos
- 0 fallos

## Decisiones tecnicas
- El escalado se aplico solo para reglas estaticas de volumen, no para `PRICE`.
- Se conserva la responsabilidad del mapper de dominio para traducir reglas a filtros Finviz.
- La condicion de escalado se basa en codigos normalizados (`VOLUME`, `AVG_VOLUME`, `VALUE`, `CONSTANT`).

## Advertencias de arquitectura y SonarQube
- Sin impacto en capas de infraestructura o presentacion.
- Sin cambios de seguridad.
- Se mantiene separacion de responsabilidades en arquitectura hexagonal.

## Proximos pasos sugeridos
1. Añadir un test de integracion del flujo completo de sugerencias para validar el filtro final enviado a Finviz.
2. Documentar en la guia funcional que valores de volumen en reglas estaticas se interpretan en miles para Finviz.
