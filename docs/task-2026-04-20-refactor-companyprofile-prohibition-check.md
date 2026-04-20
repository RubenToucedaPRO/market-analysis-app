# Refactor de validación de `CompanyProfile` y prohibición de tickers

## Resumen
Se ha refactorizado `ManageAnalyzeStockService` para separar la resolución del `CompanyProfile` de la comprobación de keywords prohibidas. El objetivo era evitar que un ticker existente en base de datos entrara directamente en `validTickers` sin pasar antes por la validación de prohibición.

## Cambios realizados
- Se sustituyó el flujo bifurcado de `validateAndUpdateCompanyProfiles` por un flujo común que procesa cada ticker con una sola ruta.
- Se introdujo una resolución explícita del `CompanyProfile`:
  - si existe y está vigente, se usa el perfil ya almacenado;
  - si no existe o está obsoleto, se consulta al proveedor externo.
- Se mantuvo la comprobación de prohibición para ambos casos antes de añadir el ticker a `validTickers`.
- Si el perfil se refresca y finalmente es válido, se persiste en el repositorio.

## Decisiones técnicas
- Se evitó duplicar la lógica de prohibición entre el caso de perfil existente y el caso de perfil descargado.
- Se mantuvo el comportamiento actual de no volver a llamar al proveedor externo cuando el `CompanyProfile` ya está disponible y no está obsoleto.
- Se preservó la lógica de veto en `ProhibitedTickerRepository` como guard clause previa al resto del flujo.

## Código afectado
- `src/main/java/com/market/analysis/application/usecase/ManageAnalyzeStockService.java`
- `src/test/java/com/market/analysis/unit/application/usecase/ManageAnalyzeStockServiceTest.java`

## Cobertura de tests
Se añadió un test específico que valida que un `CompanyProfile` ya existente pero prohibido no se añade a `validTickers` y no llega a solicitar cotización.

Validación ejecutada:
- `ManageAnalyzeStockServiceTest`: 37 tests pasados, 0 fallidos.

## Advertencias o riesgos
- La lógica sigue concentrada en el caso de uso, como orquestación de aplicación; no se ha movido al dominio para no alterar la arquitectura existente.
- Si en el futuro se quiere reutilizar esta comprobación en otros casos de uso, conviene extraer un pequeño servicio de dominio o de aplicación para la resolución de perfiles y prohibiciones.

## Próximos pasos sugeridos
- Extraer nombres más explícitos para el flujo, si se quiere mejorar todavía más la legibilidad.
- Añadir tests adicionales si se introduce lógica de expiración o actualización más compleja para `CompanyProfile`.
