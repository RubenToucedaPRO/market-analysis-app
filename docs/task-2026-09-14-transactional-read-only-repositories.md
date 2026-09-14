# Task: @Transactional(readOnly=true) en repositorios Sql*

**Fecha:** 2026-09-14
**Rama:** fix/mix-fixes
**Commits:** pendiente

---

## Resumen

Añadir `@Transactional(readOnly = true)` a todos los métodos read de los 7 repositorios Sql* para optimizar Hibernate (desactivar dirty checking, change detection y write-behind queue en operaciones de solo lectura).

Eliminar `@Query` redundante de `SqlStockDataRepository.findById()` — la query real ya está definida en `JpaStockDataRepository.findByIdWithProfile()`.

---

## Contexto técnico

`SimpleJpaRepository` (Spring Data JPA) envuelve todos sus métodos con `@Transactional` por defecto **sin** `readOnly = true`. Esto significa que Hibernate ejecuta dirty checking y change detection innecesariamente en lecturas.

Al añadir `@Transactional(readOnly = true)` explícitamente en el adapter, Spring usa la anotación más específica del método en lugar de la heredada, y Hibernate desactiva las optimizaciones de escritura.

---

## Cambios realizados

### 1. SqlProhibitedTickerRepository (3 métodos)

```java
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
public List<ProhibitedTicker> findAll() { ... }

@Transactional(readOnly = true)
public PageResult<ProhibitedTicker> findAll(int pageNumber, int pageSize) { ... }

@Transactional(readOnly = true)
public boolean existsByTicker(String ticker) { ... }
```

### 2. SqlProhibitedKeywordRepository (3 métodos)

```java
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
public List<ProhibitedKeyword> findAll() { ... }

@Transactional(readOnly = true)
public PageResult<ProhibitedKeyword> findAll(int pageNumber, int pageSize) { ... }

@Transactional(readOnly = true)
public boolean existsByKeyword(String keyword) { ... }
```

### 3. SqlApiCallRateRepository (1 método)

```java
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
public Optional<ApiCallLog> findByTicker(String ticker) { ... }
```

### 4. SqlRuleDefinitionRepository (5 métodos)

```java
@Transactional(readOnly = true)
public Optional<RuleDefinition> findById(Long id) { ... }

@Transactional(readOnly = true)
public Optional<RuleDefinition> findByCode(String code) { ... }

@Transactional(readOnly = true)
public List<RuleDefinition> findAll() { ... }

@Transactional(readOnly = true)
public boolean existsById(Long id) { ... }

@Transactional(readOnly = true)
public boolean existsByCode(String code) { ... }
```

### 5. SqlCompanyProfileRepository (1 método)

```java
@Transactional(readOnly = true)
public Optional<CompanyProfile> findByTicker(String ticker) { ... }
```

### 6. SqlStockDataRepository (1 método + limpieza)

```java
// ANTES:
@Query("SELECT s FROM StockEntity s LEFT JOIN FETCH s.companyProfile WHERE s.id = :id")
public Optional<Stock> findById(@Param("id") Long id) { ... }

// DESPUÉS:
@Override
@Transactional(readOnly = true)
public Optional<Stock> findById(@Param("id") Long id) { ... }
```

- Eliminado `@Query` redundante (la query real está en `JpaStockDataRepository.findByIdWithProfile()`)
- Eliminado import `org.springframework.data.jpa.repository.Query` sin uso

### 7. SqlCandleHistoryRepository (2 métodos)

```java
@Transactional(readOnly = true)
public List<Candle> findCandlesByTicker(String ticker) { ... }

@Transactional(readOnly = true)
public Optional<Candle> findLatestCandleByTicker(String ticker) { ... }
```

---

## Resumen de cambios

| Repositorio | Import nuevo | Métodos anotados | Limpieza |
|-------------|:---:|:---:|:---:|
| SqlProhibitedTickerRepository | Sí | 3 | — |
| SqlProhibitedKeywordRepository | Sí | 3 | — |
| SqlApiCallRateRepository | Sí | 1 | — |
| SqlRuleDefinitionRepository | No | 5 | — |
| SqlCompanyProfileRepository | No | 1 | — |
| SqlStockDataRepository | No | 1 | @Query + import eliminados |
| SqlCandleHistoryRepository | No | 2 | — |
| **Total** | **3** | **16** | **1** |

---

## Decisiones técnicas

1. **Solo `readOnly = true` en reads** — Los métodos write ya están cubiertos por `SimpleJpaRepository`. Añadir `@Transactional` explícito a writes sería pura documentación sin benefit funcional.

2. **No se tocaron métodos write** — `save()`, `deleteByTicker()`, `deleteById()`, etc. ya funcionan correctamente con la anotación heredada de `SimpleJpaRepository`.

3. **`@Query` eliminado del adapter** — Spring Data solo procesa `@Query` en interfaces de repositorio, no en clases `@Component`. La query real estaba en `JpaStockDataRepository.findByIdWithProfile()`.

---

## Próximos pasos

- Commit de los cambios
- Verificar que los tests existentes siguen pasando
- Considerar añadir tests de integración que validen el comportamiento read-only
