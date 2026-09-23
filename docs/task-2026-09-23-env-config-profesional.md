# Task 2026-09-23 - Configuración de entorno profesional (.env.example / compose / README)

## 1. Título
Normalizar `FINNHUB_BASE_URL` y variables de entorno: obligatorias vs opcionales con defaults seguros.

## 2. Resumen
`FINNHUB_BASE_URL` en `.env.example` no sobraba, pero estaba incompleta e inconsistente:
- `application.properties` define defaults para `FINNHUB_BASE_URL`, `POLYGON_BASE_URL` y `OPENROUTER_MODEL`, por lo que son opcionales.
- `.env.example` solo traía `FINNHUB_BASE_URL`, faltaban `POLYGON_BASE_URL` y `OPENROUTER_MODEL`.
- `docker-compose.yml` pasaba `FINNHUB_BASE_URL: ${FINNHUB_BASE_URL}` y `POLYGON_BASE_URL: ${POLYGON_BASE_URL}` sin default. Si faltan en `.env`, Compose inyecta cadena vacía y pisa el default de Spring (el placeholder `${VAR:default}` solo aplica si la variable no existe, no si es vacía).
- `.env.example` decía `SPRING_PROFILES_ACTIVE=prod` pero `docker-compose.yml` fuerza `docker` y `README` decía `docker`.
- `DB_URL` (perfil `prod`) aparecía como si fuese de Docker local, donde es ignorada (el perfil `docker` construye `SPRING_DATASOURCE_URL` desde `DB_DATABASE`).

Se aplica la opción A profesional: documentar todo lo que Compose y Spring aceptan, separado en obligatorias / opcionales / solo-prod.

## 3. Código generado

### 3.1 `.env.example` (reescrito)
```ini
# ============================================================
# market-analysis-app - Plantilla de entorno local (Docker)
# Copiar a .env: cp .env.example .env
# En Railway NO se usa este fichero: las variables se crean
# en el servicio app (perfil prod). Ver README "Variables de Entorno".
# ============================================================

# --- Obligatorias: APIs e IA (sin default, la app arranca vacía sin ellas) ---
FINNHUB_API_TOKEN=your_token_here
POLYGON_API_TOKEN=your_token_here
OPENROUTER_API_KEY=your_key_here

# --- Obligatorias: Docker local (perfil docker) ---
# docker-compose.yml fuerza SPRING_PROFILES_ACTIVE=docker, este valor es informativo.
SPRING_PROFILES_ACTIVE=docker

DB_DATABASE=marketanalysisdb
DB_USER=marketuser
DB_PASSWORD=tu_password_segura
DB_ROOT_PASSWORD=tu_password_root_segura

DB_PORT_EXTERNAL=3306
APP_PORT_EXTERNAL=8080

APP_SECURITY_USERNAME=admin
APP_SECURITY_PASSWORD=admin

JAVA_DEBUG_ENABLED=false # habilita debug en docker (puerto 5005)

# --- Opcionales: solo para sobreescribir el default de application.properties ---
FINNHUB_BASE_URL=https://finnhub.io/api/v1
POLYGON_BASE_URL=https://api.polygon.io/
OPENROUTER_MODEL=google/gemma-4-26b-a4b-it:free

# --- Solo perfil prod / Railway (ignorado en Docker local) ---
# En Docker la URL se construye como jdbc:mariadb://market-analysis-mysql:3306/${DB_DATABASE}.
DB_URL=jdbc:mariadb://localhost:3306/marketanalysisdb
```

### 3.2 `docker-compose.yml` (diff)
```diff
-      FINNHUB_BASE_URL: ${FINNHUB_BASE_URL}
+      FINNHUB_BASE_URL: ${FINNHUB_BASE_URL:-https://finnhub.io/api/v1}
-      POLYGON_BASE_URL: ${POLYGON_BASE_URL}
+      POLYGON_BASE_URL: ${POLYGON_BASE_URL:-https://api.polygon.io/}
```
`OPENROUTER_MODEL` ya tenía `:-default`, se mantiene.

### 3.3 `README.md` sección "Variables de Entorno"
Tabla requerido / default / notas + bloques `env` alineados con `.env.example`:
- Añadida tabla con `FINNHUB_API_TOKEN`, `POLYGON_API_TOKEN`, `OPENROUTER_API_KEY`, seguridad, DB local vs `DB_URL` prod, `FINNHUB_BASE_URL`, `POLYGON_BASE_URL`, `OPENROUTER_MODEL`, `SPRING_PROFILES_ACTIVE`, `DB/APP_PORT_EXTERNAL`, `PORT`, `JAVA_DEBUG_ENABLED/JAVA_OPTS`.
- Bloque APIs ahora incluye `FINNHUB_BASE_URL`, `POLYGON_BASE_URL` y fija `OPENROUTER_MODEL=google/gemma-4-26b-a4b-it:free` (antes mostraba `qwen/...` distinto al default real).

## 4. Decisiones técnicas
- **Opción A explícita (12-factor):** todo lo configurable documentado. Nada de magia oculta en defaults. Facilita onboarding (`cp .env.example .env` arranca a la primera) y despliegue en Railway (se ve qué NO copiar).
- **No se amplía alcance:** no se cablean en Compose `FINVIZ_*` ni `OPENROUTER_TEMPERATURE/MAX_TOKENS/TOP_P/FREQUENCY_PENALTY` (existen en `application.properties` pero Compose no los pasaba). Van a "Ideas futuras". Una rama = un tema.
- **Arquitectura:** sin cambios de dominio/aplicación. Solo config de infraestructura local + docs. Cumple SRP/DIP, sin lógica en Thymeleaf. Sin `@Autowired` en campos, sin recursos manuales, sin `System.out`.
- **`SPRING_PROFILES_ACTIVE=docker` en `.env.example`:** lo profesional para plantilla local es `docker` (coherente con `README` y con el `hardcode` de Compose). `prod` queda documentado como valor de Railway.
- **`version: '3.8'` obsoleto:** Compose avisa `the attribute version is obsolete`. No se toca en esta tarea (alcance), se anota abajo.

## 5. Cobertura de tests y pruebas
- Cambio solo de config/docs: no hay comportamiento Java nuevo que cubrir, no se añaden tests unitarios (cobertura existente intacta).
- `docker compose config`: OK. Resuelve `FINNHUB_BASE_URL=https://finnhub.io/api/v1`, `POLYGON_BASE_URL=https://api.polygon.io/`, `OPENROUTER_MODEL=google/gemma-4-26b-a4b-it:free` desde `.env` local.
- `mvn test`: **1068 tests, 0 fallos, 0 errores. BUILD SUCCESS.**
- Runtime (exigido por AGENTS.md al tocar `docker-compose.yml`): `docker compose down && docker compose up --build -d` OK. `market-analysis-mysql` Healthy, `GET /` → 200 con `AlphaSeeker - Análisis Técnico Determinista`.

## 6. Advertencias SonarQube / arquitectura
- Sin código Java: no aplican S107/S3776/S134 ni god-class.
- Seguridad: `.env` real sigue ignorado por `.gitignore`/`.dockerignore` (verificado). No se commitean secretos. Solo se versiona `.env.example` con placeholders.
- Thymeleaf: no tocado. Sin `th:utext`, sin SpEL larga.
- Aviso Compose (no bloqueante): `version` obsoleta. Limpiar en tarea aparte.

## 7. Próximos pasos sugeridos
**A. Probar ahora:** ver checklist en la PR (no requiere prueba manual nueva: cubierto por `docker compose config` + `mvn test`, no requiere prueba manual en navegador).
**B. Ideas futuras (no hacer ahora):**
- Retirar `version: '3.8'` de `docker-compose.yml`.
- Decidir si cablear en Compose `OPENROUTER_TEMPERATURE/MAX_TOKENS/TOP_P/FREQUENCY_PENALTY` y `FINVIZ_*` o dejarlos solo como `-e` manual / Railway.
- Rotar los tokens que quedaron expuestos en el `.env` local mostrado en el historial del chat.
