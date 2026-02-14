## 🎯 Objetivos del Proyecto

### Objetivo General
Diseñar e implementar una aplicación web extensible para la evaluación determinista de estrategias de análisis técnico, definidas mediante reglas configurables, capaz de calcular métricas cuantitativas de riesgo/beneficio y ofrecer análisis interpretativo complementario mediante IA, sin capacidad de decisión automática.

### Objetivos Específicos
- ✅ Permitir al usuario definir y configurar estrategias de análisis técnico desde una interfaz web.
- ✅ Modelar reglas técnicas como componentes independientes, reutilizables y combinables, desacoplados de la infraestructura.
- ✅ Evaluar estrategias sobre datos de mercado históricos y actuales obtenidos a través de APIs externas.
- ✅ Calcular métricas cuantitativas como:
  - relación Riesgo/Beneficio (R:R)
  - tasa de cumplimiento de estrategias
  - métricas básicas derivadas de evaluaciones históricas.
- ✅ Integrar modelos de lenguaje como mecanismo de análisis cualitativo, limitado a la interpretación de resultados numéricos previamente calculados.
- ✅ Aplicar principios de Clean Architecture y patrones de diseño para garantizar mantenibilidad, testabilidad y extensibilidad.
- ✅ Mantener el dominio completamente desacoplado de frameworks, bases de datos y proveedores externos.
- ✅ Desplegar la aplicación en un entorno productivo con configuración reproducible.

---
# Estructura del proyecto


#### 📈 Análisis y Seguimiento
* **Vista Analysis:** Panel de control para la ingesta de nuevos tickers y ejecución del análisis IA.
* **Vista Tracking:** Monitorización exclusiva de activos seleccionados para seguimiento a largo plazo.

#### ⚙️ Motor de Estrategias
Interfaz para configurar reglas técnicas personalizadas mediante lógica declarativa:
* **Ejemplo:** `Precio > SMA(50)` AND `Volumen > Media(20)`.
* Cada regla genera una justificación técnica y un resultado booleano.

#### 🛡️ Control de Exclusiones
* **Vista Prohibited:** Gestión de la base de datos de activos vetados. Permite la limpieza manual para rehabilitar tickers bloqueados por el filtro automático.

#### 🛠️ Monitorización
* **Vista Errors:** Registro centralizado de excepciones capturadas por el sistema para asegurar la estabilidad del despliegue.

---

## 🏗️ Arquitectura

La aplicación está diseñada siguiendo los principios de **Clean Architecture**, con el objetivo de aislar el **dominio del problema** de cualquier detalle tecnológico y garantizar mantenibilidad, testabilidad y extensibilidad a largo plazo.

```
┌─────────────────────────────────────┐
│          Presentation Layer         │
│    (Web Controllers, Templates)     │
└──────────────┬──────────────────────┘
               │
┌──────────────▼──────────────────────┐
│        Application Layer            │
│  (Use Cases, DTOs, Orchestration)   │
└──────────────┬──────────────────────┘
               │
┌──────────────▼──────────────────────┐
│          Domain Layer               │
│  (Entities, Rules, Domain Services) │
└──────────────┬──────────────────────┘
               │
┌──────────────▼──────────────────────┐
│      Infrastructure Layer           │
│ (Persistence, APIs externas, IA)    │
└─────────────────────────────────────┘
```
```
market-analysis-app/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/market/analysis/
│   │   │       ├── presentation/
│   │   │       │   ├── controllers/          # Controladores REST/MVC
│   │   │       │   │   └── .gitkeep
│   │   │       │   └── dto/                  # DTOs de entrada/salida (Response DTOs)
│   │   │       │       └── .gitkeep
│   │   │       │
│   │   │       ├── application/
│   │   │       │   ├── usecases/             # Casos de uso (servicios de aplicación)
│   │   │       │   │   └── .gitkeep
│   │   │       │   ├── mappers/              # Mapeadores entre capas (DTO ↔ Domain)
│   │   │       │   │   └── .gitkeep
│   │   │       │   ├── dto/                  # DTOs de aplicación (Command/Query DTOs)
│   │   │       │   │   └── .gitkeep
│   │   │       │   └── services/             # Servicios orquestadores
│   │   │       │       └── .gitkeep
│   │   │       │
│   │   │       ├── domain/
│   │   │       │   ├── entities/             # Entidades del dominio (Agregados)
│   │   │       │   │   └── .gitkeep
│   │   │       │   ├── interfaces/           # Puertos (contratos del dominio)
│   │   │       │   │   └── .gitkeep
│   │   │       │   ├── rules/                # Reglas técnicas independientes
│   │   │       │   │   └── .gitkeep
│   │   │       │   └── exceptions/           # Excepciones de dominio
│   │   │       │       └── .gitkeep
│   │   │       │
│   │   │       └── infrastructure/
│   │   │           ├── persistence/
│   │   │           │   ├── repositories/     # Implementaciones Spring Data JPA
│   │   │           │   │   └── .gitkeep
│   │   │           │   └── entities/         # Entidades JPA (mapeo BD)
│   │   │           │       └── .gitkeep
│   │   │           ├── external/
│   │   │           │   ├── finnhub/          # Integración API Finnhub
│   │   │           │   │   └── .gitkeep
│   │   │           │   └── polygon/          # Integración API Polygon
│   │   │           │       └── .gitkeep
│   │   │           ├── ai/
│   │   │           │   ├── openai/           # Integración OpenAI
│   │   │           │   │   └── .gitkeep
│   │   │           │   ├── anthropic/        # Integración Anthropic
│   │   │           │   │   └── .gitkeep
│   │   │           │   └── google/           # Integración Google IA
│   │   │           │       └── .gitkeep
│   │   │           └── config/               # Configuración Spring Boot
│   │   │               └── .gitkeep
│   │   │
│   │   └── resources/
│   │       ├── templates/                    # Plantillas Thymeleaf
│   │       │   └── .gitkeep
│   │       ├── static/
│   │       │   ├── css/                      # Estilos Bootstrap 5 + personalizados
│   │       │   │   └── .gitkeep
│   │       │   ├── js/                       # Scripts HTMX
│   │       │   │   └── .gitkeep
│   │       │   └── images/                   # Assets
│   │       │       └── .gitkeep
│   │       ├── application.properties        # Propiedades generales
│   │       └── application-dev.properties    # Propiedades perfil dev
│   │
│   └── test/
│       ├── java/
│       │   └── com/market/analysis/
│       │       ├── unit/                     # Tests unitarios (JUnit 5 + Mockito)
│       │       │   └── .gitkeep
│       │       └── integration/              # Tests de integración
│       │           └── .gitkeep
│       └── resources/
│           └── .gitkeep
│
├── config/                                   # Configuración externa
│   └── .gitkeep
│
├── docs/                                     # Documentación Markdown
│   └── .gitkeep
│
├── pom.xml                                   # Dependencias Maven (pendiente)
├── .gitignore                                # Exclusiones Git
└── README.md                                 # Documentación del proyecto
```

**Descripción de Capas**

- Presentation Layer:
Responsable de la interacción con el usuario. Contiene controladores web y vistas, limitándose a la validación básica de entrada y delegando toda la lógica de negocio a los casos de uso de la capa de aplicación.
- Application Layer:
Define los casos de uso del sistema y coordina la ejecución del dominio. No contiene reglas de negocio, sino que orquesta la evaluación de estrategias, la validación previa y la integración con servicios externos a través de interfaces.
- Domain Layer:
Contiene el núcleo del sistema: entidades, reglas técnicas, validadores y lógica determinista de evaluación.
Esta capa es completamente independiente de frameworks, bases de datos y APIs externas.
- Infrastructure Layer:
Implementa los detalles técnicos necesarios para la ejecución del sistema, como persistencia, consumo de APIs de mercado e integración con modelos de lenguaje, siempre a través de contratos definidos en el dominio o la aplicación.

---

## 🧠 Enfoque Conceptual

### Estrategia
Una estrategia se define como una composición ordenada de reglas técnicas evaluables, aplicada sobre un conjunto de datos de mercado en un instante o rango temporal determinado.

**Estrategia = Regla₁ + Regla₂ + ... + Reglaₙ → Evaluación + R:R + Análisis IA**

Las estrategias representan configuración declarativa, sin lógica de negocio embebida, permitiendo su persistencia, validación y evaluación independiente del contexto de ejecución.

### Regla
Una regla representa una condición técnica evaluable sobre datos de mercado.
Cada regla es independiente, reutilizable y autocontenida, y produce un resultado determinista acompañado de información explicativa.

**Salida de una regla:**
- Resultado booleano (cumple / no cumple)
- Justificación basada en valores calculados

**Ejemplos de reglas:**
- Precio por encima de SMA(50)
- Jerarquía alcista de medias móviles: SMA(20) > SMA(50) > SMA(200)
- Vela alcista con cuerpo > X% del rango
- RSI dentro del intervalo definido
- Volumen por encima de la media
- Retroceso (pullback) a una media móvil o zona de soporte

### Evaluación de Estrategia
El resultado de evaluar una estrategia incluye:

1. **Evaluación determinista de reglas**: Todas las reglas deben cumplirse según un criterio lógico AND.
2. **Cálculo de métricas cuantitativas**:
  - Relación Riesgo/Beneficio (R:R)
  - Métricas derivadas de evaluaciones históricas cuando aplica.
3. **Resultado explicable**: Detalle de qué reglas se cumplen o fallan y por qué.
4. **Análisis interpretativo mediante IA**: Texto generado a partir de los resultados numéricos, sin influencia sobre el resultado de la evaluación.

---
> Todas las integraciones externas están desacopladas mediante interfaces, permitiendo su sustitución o simulación durante el testing y garantizando la independencia del dominio

---

## 📐 Modelo de Dominio (Resumen)

### Strategy
- id
- name
- description
- reglas ordenadas

### Rule
Interfaz del dominio:
RuleResult evaluate(MarketSnapshot snapshot)

### RuleResult
- passed (boolean)
- reason (string)

### MarketSnapshot
Representa el estado del mercado en un momento concreto:
- precio actual
- apertura
- máximos y mínimos
- medias móviles
- datos técnicos calculados

---

## 🗃️ Persistencia de Estrategias

Las estrategias se almacenan como configuración.

### Strategy
```java
class Strategy {
    Long id;
    String name;
    String description;
    List<Rule> rules;  // ordenadas
    
    StrategyEvaluation evaluate(MarketSnapshot snapshot);
}
```

### Rule (interface)
```java
interface Rule {
    RuleResult evaluate(MarketSnapshot snapshot);
    String getType();
    Map<String, Object> getParameters();
}
```

### MarketSnapshot
```java
class MarketSnapshot {
    String symbol;
    Instant timestamp;
    BigDecimal currentPrice;
    BigDecimal open, high, low, close;
    Long volume;
    Map<Integer, BigDecimal> smaValues;  // SMA(20), SMA(50), etc.
    TechnicalIndicators indicators;
}
```

### StrategyEvaluation
```java
class StrategyEvaluation {
    boolean passed;
    List<RuleResult> ruleResults;
    RiskRewardRatio riskReward;
    BigDecimal potentialGainPercentage;
    String aiAnalysis;
}
```

### RiskRewardRatio
```java
class RiskRewardRatio {
    BigDecimal entryPrice;
    BigDecimal stopLoss;
    BigDecimal target;
    BigDecimal ratio;  // (target - entry) / (entry - stopLoss)
}
```

---

## 🗃️ Persistencia

### Tabla `strategy`
| Campo       | Tipo         | Descripción                    |
|-------------|--------------|--------------------------------|
| id          | BIGINT (PK)  | Identificador único            |
| name        | VARCHAR(255) | Nombre de la estrategia        |
| description | TEXT         | Descripción detallada          |
| created_at  | TIMESTAMP    | Fecha de creación              |

### Tabla `strategy_rule`
| Campo           | Tipo         | Descripción                           |
|-----------------|--------------|---------------------------------------|
| id              | BIGINT (PK)  | Identificador único                   |
| strategy_id     | BIGINT (FK)  | Referencia a strategy                 |
| rule_type       | VARCHAR(100) | Tipo de regla (SMA_CROSSOVER, etc.)   |
| parameters      | JSON/TEXT    | Parámetros configurables              |
| execution_order | INT          | Orden de evaluación                   |

**Ejemplo de parámetros JSON:**
```json
{
  "sma_period": 50,
  "comparison": "ABOVE",
  "threshold": 0.02
}
```

---

## 🖥️ Funcionalidades Web

### Gestión de Estrategias
1. **Crear estrategia**: formulario con nombre y descripción
2. **Añadir reglas**: selector dinámico con tipos disponibles
3. **Configurar parámetros**: formularios específicos por tipo de regla (cargados con HTMX)
4. **Ordenar reglas**: drag & drop o controles de orden
5. **Guardar estrategia**: persistencia en base de datos

### Evaluación de Activos
1. **Seleccionar estrategia**: lista de estrategias guardadas
2. **Introducir símbolo**: ticker del activo (ej: AAPL, TSLA)
3. **Ejecutar evaluación**: análisis en tiempo real
4. **Visualizar resultados**:
   - Estado de cada regla (✓/✗)
   - R:R calculado
   - Análisis de IA

---

## ⚙️ Flujo de Evaluación

```
1. Usuario selecciona estrategia + símbolo/s
         ↓
2. Carga estrategia desde BD
         ↓
3. Construye reglas mediante RuleFactory
         ↓
4. Obtiene datos de mercado (APIs externas)
         ↓
5. Crea MarketSnapshot
         ↓
6. Evalúa reglas secuencialmente
         ↓
7. Calcula R:R
         ↓
8. Genera prompt para IA
         ↓
9. Consulta API de IA
         ↓
10. Retorna StrategyEvaluation completa
```

**Criterio de éxito**: Todas las reglas deben cumplirse (AND lógico)

---

## 🤖 Integración con IA

### Generación del Prompt
```
Analiza la siguiente estrategia de trading técnico:

Estrategia: {nombre}
Activo: {símbolo}
Precio actual: ${precio}

Reglas evaluadas:
{lista de reglas con resultados}

Niveles calculados:
- Entrada: ${entrada}
- Stop Loss: ${stopLoss}
- Objetivo: ${objetivo}
- R:R: {ratio}

Proporciona un análisis conciso (máx. 200 palabras) sobre:
1. Coherencia técnica de la estrategia
2. Fortalezas y debilidades
3. Contexto de mercado relevante
```

### Respuesta Esperada
Texto interpretativo que complementa las métricas cuantitativas.

---

## 📊 Tipos de Reglas Implementadas

| Tipo de Regla           | Parámetros                      | Descripción                                    |
|-------------------------|---------------------------------|------------------------------------------------|
| `PRICE_ABOVE_SMA`       | `sma_period`                    | Precio actual > SMA(n)                         |
| `SMA_HIERARCHY`         | `periods: [20, 50, 200]`        | SMA(20) > SMA(50) > SMA(200)                   |
| `BULLISH_CANDLE`        | `min_body_percentage`           | Vela alcista con cuerpo mínimo                 |
| `VOLUME_SPIKE`          | `multiplier`                    | Volumen > promedio * multiplicador             |
| `RSI_ZONE`              | `period`, `min`, `max`          | RSI entre valores definidos                    |
| `PULLBACK_TO_SUPPORT`   | `sma_period`, `max_distance`    | Retroceso a SMA con distancia máxima           |
| `RISK_REWARD_MIN`       | `min_ratio`                     | R:R >= valor mínimo                            |

---
