# Tarea: Adaptar tests P2 al filtrado de capacidades usadas en getCatalogCapabilities

## Resumen

La commit `12de412` modificó el método `getCatalogCapabilities()` en `ManageRuleDefinitionService` para filtrar los códigos de indicadores que ya están registrados en el repositorio (`ruleDefinitionRepository.findAll()`). Esta tarea adapta los tests de la clase `ManageRuleDefinitionServiceP2Test` para reflejar dicho comportamiento.

## Cambio en el método

```java
@Override
public List<RuleCapabilityDTO> getCatalogCapabilities() {
    Set<String> usedCodes = ruleDefinitionRepository.findAll().stream()
        .map(RuleDefinition::getCode)
        .filter(Objects::nonNull)
        .map(String::toUpperCase)
        .collect(Collectors.toSet());
    return RuleCapabilityCatalog.getSupportedCodes().stream()
            .filter(code -> !usedCodes.contains(code.toUpperCase()))
            .sorted()
            ...
}
```

## Decisiones técnicas

- Los tests existentes de `getCatalogCapabilities()` no mockeaban `findAll()`, dependiendo del comportamiento por defecto de Mockito (lista vacía). Se añadió explícitamente `when(ruleDefinitionRepository.findAll()).thenReturn(List.of())` para hacer visible y clara la precondición de "ningún código en uso".
- Se añadieron dos nuevos tests para cubrir el comportamiento de filtrado:
  - `testGetCatalogCapabilitiesFiltersOutUsedCodes`: verifica que los códigos registrados no aparecen en la lista.
  - `testGetCatalogCapabilitiesFiltersOutUsedCodesCaseInsensitive`: verifica que la comparación es insensible a mayúsculas/minúsculas.

## Cobertura de tests añadida

| Test | Comportamiento cubierto |
|------|------------------------|
| `testGetCatalogCapabilitiesReturnsAllCodes` | Actualizado: stub explícito de `findAll()` vacío |
| `testGetCatalogCapabilitiesIsSorted` | Actualizado: stub explícito de `findAll()` vacío |
| `testSmaCapabilityConstraints` | Actualizado: stub explícito de `findAll()` vacío |
| `testPriceCapabilityConstraints` | Actualizado: stub explícito de `findAll()` vacío |
| `testConstantCapabilityConstraints` | Actualizado: stub explícito de `findAll()` vacío |
| `testGetCatalogCapabilitiesFiltersOutUsedCodes` | **Nuevo**: filtra SMA y RSI cuando están en uso |
| `testGetCatalogCapabilitiesFiltersOutUsedCodesCaseInsensitive` | **Nuevo**: filtrado case-insensitive (ema → EMA) |

## Próximos pasos sugeridos

- Considerar añadir un test de integración que valide el filtrado en un contexto con base de datos H2.
