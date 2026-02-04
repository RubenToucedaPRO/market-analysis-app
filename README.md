# market-analysis-app

# Motor de Análisis Técnico de Acciones - TFM Desarrollo con IA

## 📊 Descripción General del Proyecto
Este proyecto implementa un sistema avanzado de análisis técnico y apoyo a la toma de decisiones en la gestión de activos financieros. La aplicación se ha diseñado siguiendo una **Arquitectura Hexagonal  con Clean Architecture estricta**, separando de forma explícita el **dominio**, los **casos de uso** y los **adaptadores de infraestructura**, con el objetivo de obtener un sistema desacoplado, mantenible y fácilmente testeable.

El núcleo de la aplicación concentra la lógica de negocio y actúa como orquestador de los casos de uso, integrando datos de mercado históricos y en tiempo real obtenidos de APIs externas (Finnhub y Polygon.io) con un motor de reglas técnicas y una capa de análisis asistida por inteligencia artificial. Las dependencias técnicas quedan relegadas a la periferia del sistema, evitando su propagación al dominio.

### Principales componentes del sistema

- **Filtrado dinámico de activos**: Mecanismo previo de selección que procesa los tickers a analizar, descartando activos de alto riesgo mediante una lista negra configurable y criterios sectoriales predefinidos.
- **Motor de estrategias declarativas**: Motor basado en reglas técnicas (medias móviles, volumen, indicadores y patrones de velas) que permite definir y evaluar estrategias de inversión de forma desacoplada de la persistencia y de las fuentes de datos.
- **Integración de IA generativa**: Uso de modelos de lenguaje (GPT-4o-mini de OpenAI) para generar una síntesis cualitativa y una evaluación del binomio riesgo/beneficio a partir de los resultados técnicos calculados.
- **Arquitectura Hexagonal**: Separación clara entre dominio, lógica de aplicación y adaptadores de infraestructura, incluyendo Spring Boot, la capa de persistencia con MariaDB y la integración con APIs externas.
- **Interfaz web ligera**: Frontend desarrollado con Thymeleaf y HTMX, que actúa como adaptador de entrada y proporciona una experiencia de usuario reactiva sin recurrir a un cliente pesado, manteniendo la lógica de presentación separada del núcleo del sistema.


### Arquitectura del Sistema
La aplicación está construida para ser desplegada en **Railway**, enfocándose en la eficiencia de datos y la seguridad personal.

- **Acceso Restringido:** Sistema de autenticación privado sin registro público.
- **Gestión de Datos:** Integración híbrida de APIs:
    - **Finnhub:** Datos de perfil, precios en tiempo real y calendario de ganancias.
    - **Polygon.io:** Extracción de indicadores técnicos y métricas de volumen.
    - **GPT-4o-mini de OpenAI:** Análisis cualitativo avanzado.


### Flujo de Procesamiento y Análisis
El motor de la aplicación ejecuta un pipeline de validación en cada consulta:

1.  **Filtro de Seguridad:** Verificación contra una lista negra de sectores de alto riesgo (ETFs, Biotecnología, Warrants, activos apalancados, etc.).
2.  **Enriquecimiento Técnico:** Cálculo automático de medias móviles (SMA 20, 50, 200) y análisis de volumen comparativo.
3.  **Evaluación de Estrategias:** Motor de reglas declarativas que valida si el activo cumple con criterios técnicos predefinidos.
4.  **Cálculo R:R:** Determinación automática de la relación Riesgo/Beneficio.
5.  **Análisis IA:** Generación de un resumen interpretativo basado en los resultados cuantitativos.

---

## 🛠️ Stack Tecnológico

La selección tecnológica prioriza la **estabilidad**, la **mantenibilidad** y la **coherencia con los objetivos académicos**, evitando dependencias innecesarias y garantizando una implementación realista dentro del alcance del proyecto.

### Backend
- **Java 21 (LTS)**  
  Lenguaje principal del sistema, seleccionado por su estabilidad, soporte a largo plazo y características modernas del ecosistema JVM.
- **Spring Boot 3.5.x**  
  Framework principal para el desarrollo backend, facilitando la configuración, la inyección de dependencias y el desarrollo estructurado de la aplicación.
- **Spring Data JPA**  
  Capa de persistencia relacional desacoplada del dominio mediante el uso de repositorios.
- **Spring WebFlux – WebClient**  
  Cliente HTTP no bloqueante para el consumo eficiente de APIs externas de datos de mercado.
- **Maven**  
  Herramienta de gestión de dependencias y automatización de la construcción del proyecto.
- **JUnit 5 + Mockito**  
  Frameworks utilizados para la implementación de pruebas unitarias y de integración.

### Frontend
- **Thymeleaf**  
  Motor de plantillas server-side para el renderizado dinámico de vistas HTML.
- **Bootstrap 5**  
  Framework CSS para la construcción de una interfaz responsiva y consistente.
- **HTMX**  
  Librería para interactividad dinámica basada en peticiones HTTP, reduciendo la complejidad del cliente.
- **JavaScript vanilla**  
  Uso mínimo y puntual, limitado a funcionalidades no cubiertas por HTMX.

### Base de Datos
- **H2**  
  Base de datos en memoria utilizada durante el desarrollo y la ejecución de pruebas.
- **MariaDB 10.11**  
  Sistema gestor de base de datos relacional destinado al entorno productivo.

### Integración de IA
- **GPT-4o-mini de OpenAI**  
  Servicio externo utilizado para la generación de análisis interpretativo.
- **Prompt engineering controlado**  
  Construcción de prompts basada exclusivamente en resultados cuantitativos generados por el sistema, sin impacto en la lógica de evaluación determinista.

---

## 🌐 APIs Externas

La aplicación integra servicios externos únicamente como **fuentes de datos** o **servicios de apoyo**, manteniendo la lógica de negocio completamente encapsulada dentro del dominio.

### Finnhub.io
Proveedor de datos de mercado utilizado para la obtención de información actual y contextual de los activos financieros.

**Funcionalidades utilizadas:**
- Cotización actual
- Información corporativa básica
- Calendario de resultados financieros

**Uso en el sistema:**
- Contextualización del activo evaluado
- Soporte a evaluaciones puntuales

**Variable de entorno:**  
`FINNHUB_API_TOKEN`

---

### Polygon.io
Proveedor principal de datos históricos de mercado, utilizado como base para la evaluación determinista de estrategias.

**Funcionalidades utilizadas:**
- Datos históricos OHLCV
- Indicadores técnicos (SMA, EMA, RSI)
- Velas ajustadas por splits y dividendos

**Uso en el sistema:**
- Construcción de snapshots de mercado
- Evaluación de estrategias sobre rangos temporales
- Cálculo de métricas derivadas (relación riesgo/beneficio, tasas de cumplimiento)

**Variable de entorno:**  
`POLYGON_API_TOKEN`

---

### API de IA (OpenAI)
Servicio de modelos de lenguaje utilizado **exclusivamente para análisis interpretativo** de los resultados generados por el sistema.

**Funcionalidades utilizadas:**
- Generación de análisis cualitativo de estrategias
- Redacción de observaciones técnicas basadas en métricas calculadas
- Detección de incoherencias conceptuales a nivel descriptivo

**Limitaciones explícitas:**
- No influyen en la evaluación de reglas
- No validan estrategias
- No generan señales ni recomendaciones de inversión

**Variable de entorno:**  
`OPENAI_API_KEY`

---

## Instalación y Ejecución

### Requisitos Previos
- Java 21+
- Maven 3.9+
- MariaDB (producción) o H2 (desarrollo)

### Variables de Entorno
```bash
export FINNHUB_API_TOKEN=your_token_here
export POLYGON_API_TOKEN=your_token_here
export OPENAI_API_KEY=your_key_here
export SPRING_PROFILES_ACTIVE=dev
```

### Ejecución Local
```bash
# Clonar repositorio
git clone https://github.com/RubenToucedaPRO/market-analysis-app.git
cd market-analysis-app

# Compilar
mvn clean install

# Ejecutar
mvn spring-boot:run
```

La aplicación estará disponible en `http://localhost:8080`

---


## Estructura del Proyecto

La estructura del proyecto se ha diseñado siguiendo una **Arquitectura Hexagonal inspirada en Clean Architecture**, con el objetivo de separar claramente responsabilidades, aislar el dominio del problema de los detalles técnicos y facilitar la mantenibilidad, testabilidad y evolución del sistema.

---

### Módulos Funcionales

**Análisis y Seguimiento**
- **Vista Analysis**: Panel principal para la ingesta de nuevos tickers y la ejecución del análisis técnico y de IA.
- **Vista Tracking**: Monitorización de activos seleccionados para seguimiento a medio y largo plazo.

**Motor de Estrategias**
- Definición de estrategias técnicas mediante lógica declarativa.
- Las estrategias se componen de reglas técnicas evaluables.
- Ejemplo de estrategia: Precio > SMA(50) AND Volumen > Media(20).
- Cada regla genera un resultado determinista y una justificación técnica explicable.

**Control de Exclusiones**
- **Vista Prohibited**: Gestión de activos vetados por el sistema.
- Permite la limpieza manual de la lista negra para rehabilitar tickers excluidos por filtros automáticos.

**Monitorización**
- **Vista Errors**: Registro centralizado de excepciones y eventos del sistema para garantizar estabilidad y trazabilidad.

---

### Arquitectura General

La aplicación está organizada en capas, con las dependencias apuntando siempre hacia el dominio:
  -   Presentation: controladores web, vistas Thymeleaf y HTMX.
  -   Application: casos de uso y orquestación de la lógica de negocio.
  -   Domain: entidades, reglas y servicios de dominio.
  -   Infrastructure: persistencia, integración con APIs externas, IA y configuración técnica.

---

### Estructura de Paquetes

market-analysis-app/
├── .github/                   Configuración de GitHub Actions
├── config/                    Configuración de Spring Boot
├── docs/                      Documentación adicional tareas copilot  
├── src/main/java/com/market/analysis/  
│   ├── presentation/          Adaptadores de entrada (controladores web, DTOs)  
│   ├── application/           Casos de uso y orquestación  
│   ├── domain/                Núcleo del sistema (entidades, reglas, contratos)  
│   └── infrastructure/        Persistencia, APIs externas, integración IA y monitoreo  
├── src/main/resources/        Plantillas Thymeleaf, recursos estáticos y configuración  
├── src/test/java/             Tests unitarios y de integración  
├── terraform/                 Infraestructura como código (IaC)
├── .dockerignore              Configuración de Docker  
├── .gitignore                 Configuración de Git  
├── AGENTS.md                  Información de agentes
├── docker-compose.yml        Configuración de Docker Compose  
├── Dockerfile                 Imagen Docker de la aplicación  
├── LICENSE                    Licencia del proyecto
├── pom.xml                    Configuración Maven  
└── README.md                  Documentación principal  

---

### Estructura Detallada de Paquetes

src/main/java/com/market/analysis/

├── domain                         # Núcleo puro, sin dependencias
│   ├── model                      # Entidades y Value Objects
│   │   ├── Ticker.java
│   │   ├── Strategy.java
│   │   ├── Rule.java
│   │   └── AnalysisResult.java
│   │
│   ├── service                    # Lógica de negocio pura
│   │   └── StockAnalyzer.java
│   │
│   ├── port
│   │   ├── in                     # Puertos de entrada (Use Case interfaces)
│   │   │   ├── ObtainTickerDataUseCase.java
│   │   │   └── EvaluateStrategyUseCase.java
│   │   └── out                    # Puertos de salida (repositorios y APIs)
│   │       ├── TickerRepository.java
│   │       ├── ProhibitedTickerRepository.java
│   │       ├── FinnhubPort.java
│   │       ├── PolygonPort.java
│   │       └── OpenAiPort.java
│   │
│   └── exception                  # Excepciones del dominio
│       └── DomainException.java
│
├── application                    # Implementación de Use Cases (Orquestación)
│   └── usecase
│       ├── ObtainTickerDataService.java
│       └── EvaluateStrategyService.java
│
├── infrastructure                 # Adaptadores técnicos
│   ├── persistence
│   │   ├── entity                 # Entidades JPA
│   │   │   └── TickerEntity.java
│   │   ├── repository             # Repositorios JPA
│   │   │   ├── JpaTickerRepository.java
│   │   │   └── JpaProhibitedTickerRepository.java
│   │   └── mapper                 # Mapper Domain ↔ JPA
│   │       └── TickerMapper.java
│   │
│   ├── external                   # Integraciones externas
│   │   ├── finnhub
│   │   │   └── FinnhubClient.java
│   │   ├── polygon
│   │   │   └── PolygonClient.java
│   │   └── openai
│   │       └── OpenAiClient.java
│   │
│   ├── config                     # Beans y wiring de Use Cases
│   │    └── BeanConfig.java
│   │
│   └── monitoring                 # Monitorización y logging
│
├── presentation                    # Adaptadores de entrada (UI)
│   ├── controller
│   │   ├── AnalysisController.java
│   │   ├── StrategyController.java
│   │   ├── TrackingController.java
│   │   ├── ProhibitedController.java
│   │   └── ErrorController.java
│   │
│   ├── dto                        # DTOs para request/response
│   │   ├── TickerDto.java
│   │   └── StrategyDto.java
│   │
│   └── mapper                     # Mapper Domain ↔ DTO
│       └── TickerViewMapper.java
│
└── MarketAnalysisApplication.java  # Clase principal Spring Boot


### Descripción de Capas

**Presentation**  
Gestiona la interacción con el usuario y envía las solicitudes a la capa de aplicación.

**Application**  
Define y orquesta los casos de uso, coordinando la interacción entre el dominio y los adaptadores externos.

**Domain**  
Contiene el núcleo del sistema: entidades, reglas técnicas y lógica de evaluación. Es completamente independiente de frameworks, bases de datos y APIs externas.

**Infrastructure**  
Implementa los detalles técnicos necesarios para ejecutar el sistema, siempre a través de interfaces definidas en capas internas.

---

## 🧠 Enfoque Conceptual del Dominio

**Estrategia**  
Una estrategia es una composición ordenada de reglas técnicas evaluables sobre un conjunto de datos de mercado.  
Estrategia = Regla₁ + Regla₂ + … + Reglaₙ → Evaluación determinista → Métricas cuantitativas → Análisis interpretativo mediante IA.

**Regla**  
Condición técnica autocontenida y reutilizable que produce:
- Un resultado booleano (cumple / no cumple).
- Una justificación basada en valores calculados.

**Evaluación de Estrategia**
Incluye:
1. Evaluación determinista de reglas mediante un operador lógico AND.
2. Cálculo de métricas cuantitativas como la relación riesgo/beneficio.
3. Resultado explicable y trazable.
4. Análisis interpretativo mediante IA, sin influencia sobre el resultado de la evaluación.

---

## 🗃️ Persistencia

Las estrategias se almacenan como configuración.
- Tabla strategy: definición general de la estrategia.
- Tabla strategy_rule: reglas asociadas, ordenadas y parametrizadas.

Los parámetros de las reglas se almacenan en formato JSON, permitiendo flexibilidad y extensibilidad sin cambios estructurales en la base de datos.

Todas las integraciones externas están desacopladas mediante interfaces, garantizando la independencia del dominio y facilitando el testing.

---

### Principios Aplicados
- Dependencias dirigidas exclusivamente hacia el dominio
- Dominio libre de anotaciones de frameworks (Spring, JPA, etc.)
- Desacoplamiento de la infraestructura mediante puertos e interfaces
- Casos de uso explícitos, cohesivos y fácilmente testeables
- Aplicación estricta del Principio de Inversión de Dependencias (DIP)
- Uso de patrones de diseño:
  - Strategy (composición de reglas)
  - Factory (creación dinámica de reglas)
  - Repository (persistencia desacoplada)

---

## 🚫 Limitaciones y Disclaimer

⚠️ **Esta aplicación NO:**
- Proporciona recomendaciones de inversión
- Garantiza rentabilidad
- Ejecuta operaciones reales
- Constituye asesoramiento financiero
- Implementa notificaciones en tiempo real
- Realiza trading automático

✅ **Su propósito ES:**
- Demostración académica de arquitectura de software
- Estudio de integración de APIs externas
- Práctica de patrones de diseño
- Caso de uso educativo
- Integración de IA generativa

---

## 🎓 Enfoque Académico (TFM)

### Competencias Demostradas
- **Arquitectura de Software**: Hexagonal, DDD, SOLID
- **Clean Architecture**: Separación de capas, inversión de dependencias
- **Domain-Driven Design**: Modelado del dominio financiero
- **Design Patterns**: Strategy, Factory, Builder, Repository
- **Integration**: APIs REST, WebClient reactivo, persistencia JPA
- **AI Integration**: Prompt engineering, consulta a LLMs
- **Testing**: Unit tests, integration tests, test containers
- **DevOps**: CI/CD, despliegue en cloud

### Métricas de Calidad
- Cobertura de tests > 80%
- SonarQube quality gate: A
- Sin deuda técnica crítica
- Documentación completa (JavaDoc + README)

---

## 📅 Planificación

### Fase 1: Fundamentos (Semana 1)
- [x] Modelado del dominio
- [x] Definición de interfaces de reglas
- [x] Arquitectura base (capas)
- [x] Configuración de proyecto Maven
- [x] Setup de H2 + Spring Data JPA

### Fase 2: Core Funcional (Semana 1-2)
- [ ] Implementación de reglas técnicas
- [ ] RuleFactory y construcción dinámica
- [ ] Integración Finnhub + Polygon
- [ ] Motor de evaluación de estrategias
- [ ] Cálculo de R:R

### Fase 3: IA y Persistencia (Semana 1-2)
- [ ] Integración con API de IA
- [ ] Generación de prompts contextuales
- [ ] Persistencia de estrategias
- [ ] CRUD completo de estrategias

### Fase 4: Interfaz Web (Semana 1-2)
- [ ] Diseño de vistas Thymeleaf
- [ ] Formularios dinámicos con HTMX
- [ ] Visualización de evaluaciones
- [ ] Dashboard de estrategias

### Fase 5: Testing y Despliegue (Semana 2-3)
- [ ] Suite completa de tests
- [ ] Configuración CI/CD (GitHub Actions)
- [ ] Migración a MariaDB en producción
- [ ] Despliegue en Railway/Render/AWS
- [ ] Documentación final

---

## 📄 Licencia

Proyecto académico desarrollado con fines educativos.  
**Máster en Desarrollo con IA - 2026**

---

## 👨‍💻 Autor

**Rubén Touceda**  
Trabajo Fin de Máster - Desarrollo con IA  
Fecha: Febrero 2026