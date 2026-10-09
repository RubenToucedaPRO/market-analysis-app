# Motor de Análisis Técnico de Acciones - TFM Desarrollo con IA

![Java 21](https://img.shields.io/badge/Java-21-orange.svg)
![Coverage >80%](https://img.shields.io/badge/Coverage-%3E80%25-brightgreen.svg)
![Build](https://img.shields.io/badge/Build-passing-brightgreen.svg)
![License](https://img.shields.io/badge/License-Academic-blue.svg)

**Despliegue productivo:** https://tfm.rubentouceda.es

## 📊 Descripción General del Proyecto
Este proyecto implementa un sistema avanzado de análisis técnico y apoyo a la toma de decisiones en la gestión de activos financieros de uso personal. La aplicación se ha diseñado siguiendo una **Arquitectura Hexagonal  con Clean Architecture estricta**, separando de forma explícita el **dominio**, los **casos de uso** y los **adaptadores de infraestructura**, con el objetivo de obtener un sistema desacoplado, mantenible y fácilmente testeable.

El núcleo de la aplicación concentra la lógica de negocio y actúa como orquestador de los casos de uso, integrando datos de mercado históricos y en tiempo real obtenidos de APIs externas (Finnhub y Polygon.io) con un motor de reglas técnicas y una capa de análisis asistida por inteligencia artificial. Las dependencias técnicas quedan relegadas a la periferia del sistema, evitando su propagación al dominio.

![Diagrama de Arquitectura Hexagonal](/image/arquitectura_hexagonal.png)

### Principales componentes del sistema

- **Filtrado dinámico de activos**: Mecanismo previo de selección que procesa los tickers a analizar, descartando activos de alto riesgo mediante una lista negra configurable y criterios sectoriales predefinidos.
- **Motor de estrategias declarativas**: Motor basado en reglas técnicas (medias móviles, volumen, indicadores y patrones de velas) que permite definir y evaluar estrategias de inversión de forma desacoplada de la persistencia y de las fuentes de datos.
- **Integración de IA generativa**: Uso de modelos de lenguaje vía OpenRouter (modelo principal `poolside/laguna-s-2.1:free`, con rotación automática ante 429 sobre 4 modelos de reserva) para generar una síntesis cualitativa y una evaluación del binomio riesgo/beneficio a partir de los resultados técnicos calculados.
- **Arquitectura Hexagonal**: Separación clara entre dominio, lógica de aplicación y adaptadores de infraestructura, incluyendo Spring Boot, la capa de persistencia con MariaDB y la integración con APIs externas.
- **Interfaz web ligera**: Frontend desarrollado con Thymeleaf y Bootstrap 5, con apoyo puntual de JavaScript vanilla para interacciones específicas, manteniendo la lógica de presentación separada del núcleo del sistema.


### Arquitectura del Sistema
La aplicación está desplegada en un **VPS propio** (https://tfm.rubentouceda.es) con Docker, Nginx y SSL, enfocándose en la eficiencia de datos y la seguridad personal. Ver `deploy/guia_vps.md`.

- **Acceso Restringido:** Sistema de autenticación privado sin registro público, con protección anti-fuerza bruta: 3 intentos fallidos = bloqueo de 30 min **por nombre de usuario** (en memoria; un login correcto resetea el contador; la IP solo se registra en logs, sin secretos).
- **Gestión de Datos:** Integración híbrida de APIs:
    - **Finnhub:** Datos de perfil, precios en tiempo real y calendario de ganancias.
    - **Polygon.io:** Extracción de indicadores técnicos y métricas de volumen.
    - **OpenRouter (modelo principal `poolside/laguna-s-2.1:free`):** Análisis cualitativo avanzado.


### Flujo de Procesamiento y Análisis
Flujo de procesamiento para cada ticker ingresado o sugerido:

1.  **Inserción de Ticker o sugerencia:** El usuario ingresa un ticker específico o solicita sugerencias basadas en criterios predefinidos.
2.  **Filtro de Seguridad:** Verificación contra una lista negra de sectores de alto riesgo (ETFs, Biotecnología, Warrants, activos apalancados, etc.).
3.  **Enriquecimiento Técnico:** Cálculo automático de medias móviles (SMA 20, 50, 200) y análisis de volumen comparativo.
4.  **Evaluación de Estrategias:** Motor de reglas declarativas que valida si el activo cumple con criterios técnicos predefinidos.
5.  **Cálculo R:R:** Determinación automática de la relación Riesgo/Beneficio.
6.  **Análisis IA:** Generación de un resumen interpretativo basado en los resultados cuantitativos a petición del usuario.
7.  **Persistencia y Seguimiento:** Almacenamiento de resultados, sugerencias de tickers y registros de llamadas a APIs para análisis posterior.

---

## 🛠️ Stack Tecnológico

La selección tecnológica prioriza la **estabilidad**, la **mantenibilidad** y la **coherencia con los objetivos académicos**, evitando dependencias innecesarias y garantizando una implementación realista dentro del alcance del proyecto.

### Backend
- **Java 21 (LTS)**  
  Lenguaje principal del sistema, seleccionado por su estabilidad, soporte a largo plazo y características modernas del ecosistema JVM.
- **Spring Boot 3.5.16**  
  Framework principal para el desarrollo backend, facilitando la configuración, la inyección de dependencias y el desarrollo estructurado de la aplicación.
- **Spring Data JPA**  
  Capa de persistencia relacional desacoplada del dominio mediante el uso de repositorios.
- **Spring WebFlux – WebClient**  
  Cliente HTTP no bloqueante para el consumo eficiente de APIs externas de datos de mercado.
- **Maven**  
  Herramienta de gestión de dependencias y automatización de la construcción del proyecto.
- **JUnit 5 + Mockito**  
  Frameworks utilizados para la implementación de pruebas unitarias y de integración.
- **SLF4J + Logback**  
  Logging estructurado con `@Slf4j` y encoder Logstash.

### Frontend
- **Thymeleaf**  
  Motor de plantillas server-side para el renderizado dinámico de vistas HTML.
- **Bootstrap 5**  
  Framework CSS para la construcción de una interfaz responsiva y consistente.
- **JavaScript vanilla**  
  Uso mínimo y puntual para interacciones no cubiertas por Thymeleaf y Bootstrap.

### Base de Datos
- **H2**  
  Base de datos en memoria utilizada durante el desarrollo y la ejecución de pruebas.
- **MariaDB 10.11**  
  Sistema gestor de base de datos relacional destinado al entorno productivo.

### Integración de IA
- **OpenRouter (modelo principal `poolside/laguna-s-2.1:free`)**  
  Servicio externo utilizado para la generación de análisis interpretativo, con rotación automática ante 429 sobre los modelos de reserva (`nvidia/nemotron-3-ultra-550b-a55b:free`, `poolside/laguna-xs-2.1:free`, `google/gemma-4-31b-it:free`, `google/gemma-4-26b-a4b-it:free`).
- **Prompt engineering controlado**  
  Construcción de prompts basada exclusivamente en resultados cuantitativos generados por el sistema, sin impacto en la lógica de evaluación determinista.

---

## 🌐 APIs Externas

La aplicación integra servicios externos únicamente como **fuentes de datos** o **servicios de apoyo**, manteniendo la lógica de negocio completamente encapsulada dentro del dominio.

> **Nota sobre planes gratuitos**: al ser un proyecto educativo se usan los niveles gratuitos de las APIs y de los modelos de IA (Finnhub 60 peticiones/min, Polygon 5/min, cupos diarios en OpenRouter). Por decisión consciente **las latencias altas no son un problema a optimizar**: se asumen y se gestionan con throttles, trabajos asíncronos con polling y rotación de modelos. La corrección de los resultados importa; la velocidad, no.

### Finnhub.io
Proveedor de datos de mercado utilizado para la obtención de información actual y contextual de los activos financieros.

**Funcionalidades utilizadas:**
- Cotización actual
- Información corporativa básica

**Uso en el sistema:**
- Contextualización del activo evaluado
- Soporte a evaluaciones puntuales

**Variable de entorno:**  
`FINNHUB_API_TOKEN`


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


### Finviz
Fuente pública de datos utilizada para **sugerencias de tickers** mediante scraping controlado (adapter `JsoupFinvizAdapter`).

**Funcionalidades utilizadas:**
- Generación de listas de candidatos a analizar por estrategia segun las reglas definidas.

**Uso en el sistema:**
- Alimenta el caso de uso de sugerencias de tickers
- Registra snapshots de resultados


### API de IA (OpenRouter)
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
`OPENROUTER_API_KEY`

---

## Instalación y Ejecución

### Requisitos Previos
- **Java 21+**
- **Maven 3.9+**
- **Docker & Docker Compose** (Para levantar MariaDB y el entorno aislado)


### Variables de Entorno

El proyecto utiliza un archivo `.env` en la raíz para desarrollo local con Docker (ver `.env.example` como plantilla).
En el VPS productivo NO se usa `.env` del repo: las variables viven fuera del código y se inyectan en el despliegue (ver `deploy/guia_vps.md`).

| Variable | Requerida | Default en `application.properties` | Notas |
|---|---|---|---|
| `FINNHUB_API_TOKEN` | Sí | _(vacío)_ | Datos de mercado actuales |
| `POLYGON_API_TOKEN` | Sí | _(vacío)_ | Datos históricos y OHLCV |
| `OPENROUTER_API_KEY` | Sí | _(vacío)_ | Análisis interpretativo |
| `APP_SECURITY_USERNAME` / `APP_SECURITY_PASSWORD` | Sí | `admin` / `admin` | Login de la app, cambiar en producción |
| `DB_DATABASE` / `DB_USER` / `DB_PASSWORD` / `DB_ROOT_PASSWORD` | Sí (Docker local) | — | Solo contenedor MySQL local |
| `DB_URL` / `DB_USER` / `DB_PASSWORD` | Sí (prod / VPS) | — | Ej: `jdbc:mariadb://HOST:PORT/DATABASE`. Ignorado en Docker local |
| `FINNHUB_BASE_URL` | No | `https://finnhub.io/api/v1` | Solo para override (mock/proxy) |
| `POLYGON_BASE_URL` | No | `https://api.polygon.io/` | Solo para override (mock/proxy) |
| `OPENROUTER_MODEL` | No | `poolside/laguna-s-2.1:free` | Modelo principal (default del código: `google/gemma-4-26b-a4b-it:free`; manda el entorno) |
| `OPENROUTER_FALLBACK_MODELS` | No | `nvidia/nemotron-3-ultra-550b-a55b:free,poolside/laguna-xs-2.1:free,google/gemma-4-31b-it:free,google/gemma-4-26b-a4b-it:free` | Reserva si el principal da 429, en orden (coma) |
| `SPRING_PROFILES_ACTIVE` | No (local) | `dev` | En local usar `docker`, en VPS usar `prod` (`docker-compose.yml` fuerza `docker`) |
| `DB_PORT_EXTERNAL` / `APP_PORT_EXTERNAL` | No | `3306` / `8080` | Solo mapeo de puertos local |
| `PORT` | No | `8080` | Puerto de escucha de la app |
| `JAVA_DEBUG_ENABLED` / `JAVA_OPTS` | No | `false` / _(vacío)_ | `true` solo para debug local (puerto 5005) |

#### Configuración de APIs e Inteligencia Artificial
```env
FINNHUB_API_TOKEN=your_token_here
POLYGON_API_TOKEN=your_token_here
OPENROUTER_API_KEY=your_key_here
SPRING_PROFILES_ACTIVE=docker

# Opcionales (defaults en application.properties, solo para override)
FINNHUB_BASE_URL=https://finnhub.io/api/v1
POLYGON_BASE_URL=https://api.polygon.io/
OPENROUTER_MODEL=poolside/laguna-s-2.1:free
# Reserva si el principal devuelve 429, en orden (separados por comas)
OPENROUTER_FALLBACK_MODELS=nvidia/nemotron-3-ultra-550b-a55b:free,poolside/laguna-xs-2.1:free,google/gemma-4-31b-it:free,google/gemma-4-26b-a4b-it:free
OPENROUTER_TEMPERATURE=0.2
OPENROUTER_MAX_TOKENS=1000
OPENROUTER_TOP_P=0.9
OPENROUTER_FREQUENCY_PENALTY=0.0
```

#### Configuración de Base de Datos
```env
DB_DATABASE=marketanalysisdb
DB_USER=marketuser
DB_PASSWORD=tu_password_segura
DB_ROOT_PASSWORD=tu_password_root_segura
DB_PORT_EXTERNAL=3306
APP_PORT_EXTERNAL=8080
```

#### Configuración de Seguridad (Login)
```env
APP_SECURITY_USERNAME=admin
APP_SECURITY_PASSWORD=admin
```
Credenciales de acceso a la aplicación. Se recomienda cambiar los valores por defecto en producción.



### Ejecución Local con Docker

Para simplificar el despliegue en desarrollo, el proyecto utiliza **Docker Compose**, lo que automatiza la creación de la base de datos MariaDB, la ejecución del script de inicialización (`script-bd.sql`) y el arranque de la aplicación Spring Boot de forma aislada.

#### 1. Clonar el repositorio
```bash
git clone https://github.com/RubenToucedaPRO/market-analysis-app.git
cd market-analysis-app
```

#### 2. Configurar el archivo de entorno
Crea un archivo llamado `.env` en la raíz del proyecto basándote en la plantilla `.env.example` y rellena tus credenciales y tokens de APIs:
```bash
cp .env.example .env
```

#### 3. Compilar la aplicación Java
Antes de levantar los contenedores, empaqueta el artefacto `.jar` de Spring Boot para que Docker pueda construir la imagen de la aplicación:
```bash
mvn clean package -DskipTests
```
*(Nota: Se añade `-DskipTests` debido a que los tests de integración requieren que la base de datos esté levantada previamente).*

#### 4. Levantar el entorno
Arranca la base de datos y la aplicación en segundo plano ejecutando:
```bash
docker compose up -d
```

Este comando realizará las siguientes tareas automáticamente de forma secuencial:
1. Levantará el contenedor de MariaDB y aplicará las restricciones de RAM idóneas para producción.
2. Inyectará y ejecutará de forma automática el archivo `./config/database/script-bd.sql`.
3. Validará la salud de la base de datos a través de su *healthcheck*.
4. Construirá y arrancará el contenedor de la aplicación conectándose a la red interna compartida.

La aplicación estará completamente disponible en `http://localhost:8080`.

#### 5. Detener el entorno
Si deseas parar los servicios y liberar los puertos de tu máquina local:
```bash
docker compose down
```
*(Si necesitas limpiar por completo la base de datos y borrar los datos locales para forzar una reinstalación limpia del script SQL, utiliza `docker compose down -v`)*.

### Integración Continua (workflows)

Cada push/PR a `main` dispara validación automática en GitHub Actions (`.github/workflows/`):

| Workflow | Disparador | Qué hace |
|---|---|---|
| `testUnitarios-workflow.yml` | push y PR a `main` (+ manual) | JDK 21 + `mvn -B verify`, publica resultados de tests y reporte JaCoCo (mínimo 80%) como comentario en la PR |
| `docker.yml` | push a `main` | Compila la imagen Docker y la publica en GHCR (`tfm:latest` + tag por SHA, imagen que despliega el VPS) |

---


## Estructura del Proyecto

La estructura del proyecto se ha diseñado siguiendo una **Arquitectura Hexagonal inspirada en Clean Architecture**, con el objetivo de separar claramente responsabilidades, aislar el dominio del problema de los detalles técnicos y facilitar la mantenibilidad, testabilidad y evolución del sistema.


### Módulos Funcionales
**Login y Seguridad**
- **Vista Login**: Sistema de autenticación básico para acceso restringido.

**Análisis y Seguimiento**
- **Vista Analysis**: Panel principal para la ingesta de nuevos tickers y la ejecución del análisis técnico y de IA.
- **Vista Tracking**: Monitorización de activos seleccionados para analisis.

**Motor de Estrategias**
- Definición de estrategias técnicas mediante lógica declarativa.
- Las estrategias se componen de reglas técnicas evaluables.
- Ejemplo de estrategia: Precio > SMA(50) AND Volumen > Media(20).
- Cada regla genera un resultado determinista y una justificación técnica explicable.

**Control de Exclusiones**
- **Vista Prohibited**: Gestión de activos vetados por el sistema.
- Permite la limpieza manual de la lista negra para rehabilitar tickers excluidos por filtros automáticos.


### Arquitectura General

La aplicación está organizada en capas, con las dependencias apuntando siempre hacia el dominio:
  -   Presentation: controladores web y vistas Thymeleaf.
  -   Application: casos de uso y orquestación de la lógica de negocio.
  -   Domain: entidades, reglas y servicios de dominio.
  -   Infrastructure: persistencia, integración con APIs externas, IA y configuración técnica.


### Estructura de Paquetes

```text
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
```


### Estructura Detallada de Paquetes

```text
src/main/java/com/market/analysis/

├── domain                         # Núcleo puro, sin dependencias
│   ├── model                      # Entidades y Value Objects
│   │   ├── Stock.java
│   │   ├── Strategy.java
│   │   ├── Rule.java
│   │   ├── AnalysisResult.java
│   │   ├── SuggestedTickerSnapshot.java
│   │   ├── SuggestionSnapshot.java
│   │   └── ApiCallLog.java
│   │
│   ├── service                    # Lógica de negocio pura
│   │   ├── RuleEvaluator.java
│   │   ├── RiskRewardCalculator.java
│   │   └── PromptBuilder.java
│   │
│   ├── port
│   │   ├── in                     # Puertos de entrada (Use Case interfaces)
│   │   │   ├── ManageAnalyzeTickerUseCase.java
│   │   │   ├── ManageStrategyUseCase.java
│   │   │   ├── ManageRuleDefinitionUseCase.java
│   │   │   ├── ManageProhibitedTickerUseCase.java
│   │   │   ├── ManageProhibitedKeywordUseCase.java
│   │   │   ├── EvaluateStrategyUseCase.java
│   │   │   └── SuggestTickersUseCase.java
│   │   └── out                    # Puertos de salida (repositorios y APIs)
│   │       ├── StockDataRepository.java
│   │       ├── StockProviderPort.java
│   │       ├── CandleHistoryRepository.java
│   │       ├── CompanyProfileRepository.java
│   │       ├── StrategyRepository.java
│   │       ├── StrategyEvaluationRepository.java
│   │       ├── RuleDefinitionRepository.java
│   │       ├── ProhibitedTickerRepository.java
│   │       ├── ProhibitedKeywordRepository.java
│   │       ├── SuggestionSnapshotRepository.java
│   │       ├── ApiCallRateRepository.java
│   │       ├── HistoricalProviderPort.java
│   │       ├── ApiIAPort.java
│   │       ├── FinvizScreenerPort.java
│   │       └── HealthCheckPort.java
│   │
│   └── exception                  # Excepciones del dominio
│       └── StockDataNotFoundException.java
│
├── application                    # Implementación de Use Cases (Orquestación)
│   ├── dto
│   │   ├── StockDataDTO.java
│   │   ├── StrategyDTO.java
│   │   ├── RuleDefinitionDTO.java
│   │   └── SuggestTickersRequestDTO.java
│   │
│   ├── mapper
│   │   ├── StockDataDTOMapper.java
│   │   ├── StrategyDTOMapper.java
│   │   ├── RuleDefinitionDTOMapper.java
│   │   └── HealthCheckMapper.java
│   │
│   └── usecase
│       ├── ManageAnalyzeStockService.java
│       ├── AnalyzeAndPersistStockService.java
│       ├── ManageStrategyService.java
│       ├── ManageRuleDefinitionService.java
│       ├── ManageProhibitedTickerService.java
│       ├── ManageProhibitedKeywordService.java
│       ├── SuggestTickersService.java
│       └── HealthCheckService.java
│
├── infrastructure                 # Adaptadores técnicos
│   ├── persistence
│   │   ├── entity                 # Entidades JPA
│   │   │   ├── StockEntity.java
│   │   │   ├── StrategyEntity.java
│   │   │   ├── RuleDefinitionEntity.java
│   │   │   ├── SuggestionSnapshotEntity.java
│   │   │   └── ApiCallLogEntity.java
│   │   ├── repository             # Repositorios JPA
│   │   │   ├── JpaStockDataRepository.java
│   │   │   └── JpaProhibitedTickerRepository.java
│   │   └── mapper                 # Mapper Domain ↔ JPA
│   │       ├── StockMapper.java
│   │       └── SuggestionSnapshotMapper.java
│   │
│   ├── external                   # Integraciones externas
│   │   ├── finnhub
│   │   │   └── FinnhubAdapter.java
│   │   ├── polygon
│   │   │   └── PolygonAdapter.java
│   │   ├── finviz
│   │   │   └── JsoupFinvizAdapter.java
│   │   └── openrouter
│   │       └── OpenrouterAdapter.java
│   │
│   ├── monitoring                 # Monitorización y health checks
│   │   └── HealthCheckAdapter.java
│   │
│   ├── config                     # Beans y wiring de Use Cases
│   │    └── BeanConfig.java
│   │
│   └── migration                  # Semillas y ajustes de datos
│
├── presentation                    # Adaptadores de entrada (UI)
│   ├── controller
│   │   ├── AnalyzeTickerController.java
│   │   ├── StrategyController.java
│   │   ├── ProhibitedTickerController.java
│   │   ├── RuleDefinitionController.java
│   │   └── HealthCheckController.java
│   │
│   ├── dto                        # DTOs de UI
│   │   └── UiNotification.java
│   │
│   └── exception                  # Errores web centralizados
│       └── GlobalExceptionHandler.java
│
└── MarketAnalysisApplication.java  # Clase principal Spring Boot
```


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

## Principios Aplicados
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
## Funcionalidades Clave
- Definición de estrategias técnicas mediante reglas declarativas
- Ingesta de tickers para análisis técnico y evaluación en base a estrategias predefinidas
- Cálculo automático de la relación riesgo/beneficio
- Generación de análisis interpretativo mediante IA
- Sugerencias de tickers basadas en la estrategia definida
- Gestión de activos prohibidos y palabras clave de exclusión

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
- Desarrollo con IA aplicando los conocimientos adquiridos en el Máster "Desarrollo con IA" de Big School.
- Uso personal y educativo para la práctica de análisis técnico y evaluación de estrategias de inversión.
- Investigación y experimentación con integración de APIs externas y modelos de lenguaje.
- Demostración académica de arquitectura de software.
- Práctica de patrones de diseño y principios SOLID.
- Caso de uso educativo
- Integración de IA generativa para análisis interpretativo de resultados cuantitativos.

---

## 🎓 Enfoque Académico (TFM)

### Competencias Demostradas
- **Copilot + OpenCode**: Desarrollo asistido por IA con supervisión y control de calidad. Uso de Agents y prompts para guiar la generación de código y documentación.
- **Arquitectura de Software**: Hexagonal, DDD, SOLID
- **Clean Architecture**: Separación de capas, inversión de dependencias
- **Domain-Driven Design**: Modelado del dominio financiero
- **Design Patterns**: Strategy, Factory, Builder, Repository
- **Integration**: APIs REST, WebClient reactivo, persistencia JPA
- **AI Integration**: Prompt engineering, consulta a LLMs
- **Testing**: Unit tests (JUnit 5 + Mockito), controladores con MockMvc, JaCoCo ≥ 80%
- **DevOps**: Docker Compose, despliegue en VPS con Nginx + SSL, workflows de CI (tests + build de imagen)
- **Documentación**: JavaDoc, README, documentación de tareas en `/docs`

### Métricas de Calidad
- Cobertura de tests ≥ 80% (JaCoCo, verificado con `mvn verify`)
- SonarQube Quality Gate A (verificado, Community Build local): S107 (máximo 7 parámetros en constructor), S3776 (complejidad cognitiva < 15), S134 (profundidad de anidamiento < 4)
- Sin deuda técnica crítica
- Documentación completa (JavaDoc + README + docs de tarea en `/docs`)

---

## 🤖 Desarrollo con IA

El proyecto se ha desarrollado con IA bajo supervisión y con desarrollo propio:

- **Fase inicial con Copilot**: arranque del proyecto, estructura base y primeras iteraciones con autocompletado y sugerencias inline.
- **Fase principal con OpenCode** (modelo Muse Spark): desarrollo por tareas con procedimiento estricto definido en `AGENTS.md` (§3) — rama por tarea → tests → doc en `/docs` → validación con menú → PR. Ninguna tarea empieza sin la PR anterior en MERGED.
- **Desarrollo propio intercalado**: decisiones de diseño, ajustes finos, tuning de modelos/prompt y resolución de imprevistos de producción directamente por el autor.

### Fronteras de la IA (diseño intencionado)
- **Motor determinista**: la evaluación de reglas (`Rule` → `RuleEvaluator` → `EvaluateStrategyService`) y el cálculo de métricas (R:R, score 0-100, cumplimiento) son código puro y testeado. La IA nunca decide ni altera resultados.
- **IA solo interpretativa**: los modelos de lenguaje (vía OpenRouter) generan texto a partir de métricas ya calculadas (síntesis de estrategia, valoración de ticker). Si la IA falla o devuelve 429, la evaluación numérica sigue intacta (reintentos + rotación de modelos + valoración degradada).
- **Nunca se modifica la lógica de evaluación para contentar a la IA**: es una regla explícita del proyecto.

### Trazabilidad
Cada tarea genera un documento autocontenido en `/docs` (`task-YYYY-MM-DD-<slug>.md`) con decisiones técnicas, cobertura de tests y próximos pasos. Ver también `docs/architecture-walkthrough.md` (flujo `RuleEvaluator` → `AnalyzeAndPersistStockService` → `PolygonAdapter`), `docs/tfm-closure-plan.md` (plan de cierre), `docs/adr/` (4 decisiones de arquitectura) y `docs/prompts/` (patrones reutilizables de desarrollo con IA).

---

## 🔌 Endpoints JSON (uso interno)

La aplicación es una web MVC con Thymeleaf (no expone API REST pública). Los únicos endpoints JSON son los que consumen el propio frontend (`fetch()`) y la monitorización del despliegue. No hay Swagger/OpenAPI por decisión consciente (ver `docs/tfm-closure-plan.md`: movido a Future Work hasta que exista API pública).

| Método | Ruta | Auth | Uso |
|---|---|---|---|
| `GET` | `/health` | No (pública) | Estado de la app + BD para Nginx/VPS. Ej: `curl https://tfm.rubentouceda.es/health` |
| `GET` | `/analysis/ticker/{id}/candles` | Sí (login) | Serie OHLCV + SMA20/50/200 para `candle-chart.js` |
| `GET` | `/analysis/ia-jobs/{jobId}` | Sí (login) | Polling del estado de la valoración IA asíncrona |
| `GET` | `/strategies/suggest-jobs/{jobId}` | Sí (login) | Polling del estado de las sugerencias de tickers asíncronas |

---

## 🛟 Troubleshooting

| Síntoma | Causa probable | Solución |
|---|---|---|
| La app no arranca en Docker | Falta `.env` o BD no lista | `cp .env.example .env`, rellenar tokens y `docker compose up --build -d`. MariaDB tiene healthcheck: la app espera a que esté sana |
| `403` al enviar formularios | Token CSRF ausente/caducado | Recargar la página y reenviar; los formularios usan `th:action` con CSRF de Spring Security |
| Login bloqueado tras 3 intentos | Protección anti-fuerza bruta: 3 fallos = bloqueo de 30 min **por nombre de usuario** (no por IP; la IP solo se registra en logs) | Esperar 30 min (el bloqueo es en memoria: reiniciar la app también lo limpia; un login correcto resetea el contador). Los logs no registran secretos |
| Sugerencias o valoración IA tardan / dan 504 | Planes gratuitos con cupos y throttles (asumido por diseño, no es un fallo) | Son trabajos asíncronos: la vista hace polling a los endpoints de estado (`ia-jobs`, `suggest-jobs`). Ante 429 de OpenRouter rota automáticamente de modelo |
| La IA devuelve error o vacío | 429 persistente o respuesta inválida | La app degrada a valoración vacía/por defecto sin romper la evaluación numérica; revisar logs y `OPENROUTER_FALLBACK_MODELS` |
| Puerto 8080 ocupado en local | Otro servicio lo usa | Cambiar `APP_PORT_EXTERNAL` en `.env` |
| Redeploy en VPS | — | `deploy-tfm.sh` es idempotente (re-pull de imagen + restart). Verificar `GET /health` → 200 y `https://tfm.rubentouceda.es` → 200. Detalle en `deploy/guia_vps.md` |

---

## 📄 Licencia

Proyecto académico desarrollado con fines educativos.  
**Máster en Desarrollo con IA - 2026**

---

## 👨‍💻 Autor

**Rubén Touceda**  
Trabajo Fin de Máster - Desarrollo con IA  
Fecha: Octubre 2026