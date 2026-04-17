# Tarea: Finviz para PRICE y AVG_VOLUME contra valores estaticos

## Resumen de la tarea
Se agrego soporte en el mapeador de filtros de Finviz para reglas que comparan:
- `PRICE` contra un valor estatico
- `AVG_VOLUME` contra un valor estatico

El mapeo se declaro en `MAPPINGS` para mantener la traduccion de reglas centralizada en el mapa estatico del mapper.

## Codigo generado
Archivo modificado principal:
- `src/main/java/com/market/analysis/domain/service/FinvizFilterMapperImpl.java`

Cambios clave:
1. Se agregaron entradas a `MAPPINGS` para:
   - `PRICE > CONSTANT(x)` -> `sh_price_oX`
   - `PRICE < CONSTANT(x)` -> `sh_price_uX`
   - `PRICE > VALUE(x)` -> `sh_price_oX`
   - `PRICE < VALUE(x)` -> `sh_price_uX`
   - `AVG_VOLUME > CONSTANT(x)` -> `sh_avgvol_oX`
   - `AVG_VOLUME < CONSTANT(x)` -> `sh_avgvol_uX`
   - `AVG_VOLUME > VALUE(x)` -> `sh_avgvol_oX`
   - `AVG_VOLUME < VALUE(x)` -> `sh_avgvol_uX`
2. Se recupero el contrato del mapper con `Strategy`.
3. Se mantuvo el resto del comportamiento ya existente para reglas tecnicas soportadas.

Archivo de pruebas actualizado:
- `src/test/java/com/market/analysis/unit/domain/service/FinvizFilterMapperTest.java`

Nuevos escenarios cubiertos:
- Mapeo de `PRICE` con valor estatico.
- Mapeo de `AVG_VOLUME` con valor estatico.
- Soporte de `CONSTANT` y `VALUE` como codigo del valor fijo.

## Decisiones tecnicas tomadas
- El soporte se implemento directamente en `MAPPINGS`, tal como se solicito, en lugar de usar un helper aparte.
- Se utilizo un matcher de patron que permite que el valor numerico final de la regla sea dinamico solo en esas entradas.
- Se mantuvo la traduccion de filtros Finviz en capa de dominio, que es donde corresponde el mapeo semantico de reglas.

## Cobertura de tests y pruebas añadidas
Prueba ejecutada:
- `FinvizFilterMapperTest`

Resultado:
- 9 tests ejecutados
- 9 tests correctos
- 0 fallos

## Advertencias de SonarQube o arquitectura
- No se introdujo logica de negocio en infraestructura ni en vistas.
- El cambio respeta la separacion entre reglas de dominio y adaptadores externos.
- No se detectan riesgos obvios de seguridad por este cambio.

## Proximos pasos sugeridos
1. Si se quiere restringir mas el contrato, documentar en las definiciones de reglas que los valores estaticos deben usar `CONSTANT` o `VALUE`.
2. Ampliar la cobertura con un test de integracion del flujo de sugerencias para verificar que estos filtros llegan a Finviz.
3. Si aparecen mas reglas de comparacion con valores fijos, centralizar la convencion de nombres de filtro en una constante o enum.
