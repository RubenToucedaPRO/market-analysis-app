# AlphaSeeker - Motor de Análisis Técnico de Acciones

## Evaluación determinista + IA solo interpretativa

**Rubén Touceda** · Máster Desarrollo con IA (Big School) · Octubre 2026

Proyecto real de uso personal: seleccionar acciones para el análisis fundamental.


---

# El problema

- Filtraba acciones en Finviz por parámetros, pero luego tocaba ir **una a
  una** clasificándolas como más o menos interesantes
- Ese triaje manual no escala: decenas de candidatas pidiendo **análisis
  fundamental** (históricos, beneficios, ganancias...)
- Faltaba un **prefiltro cuantitativo**: que cada acción se evalúe sola con
  ratio riesgo/beneficio + entrada + stop-loss…
- …para quedarme solo con las mejores y dedicarles el análisis fundamental


---

# La idea: división clara

1. **Motor determinista**: reglas técnicas → score 0-100 + R:R + cumplimiento
2. **IA solo interpretativa**: redacta la síntesis desde métricas ya calculadas
3. **Uso de API externas**: Finviz, Polygon, Finnhub, OpenRouter. Todas sin coste aunque suponga limitar la velocidad de respuesta (planes gratuitos)


---

# ¿Cómo funciona?

El recorrido de una acción, de candidata a evaluada:

1. Ingesta de tickers o sugerencias automáticas (scrapper propio de Finviz async) con filtro de seguridad
2. Enriquecimiento: OHLCV + SMA 20/50/200 y volumen (Polygon/Finnhub)
3. Evaluación declarativa: Ej: `Precio > SMA(50)` AND `Volumen > Media(20)`
4. Cálculo de R:R y score ponderado 0-100 por estrategia
5. Análisis IA a petición (async, con rotación de modelos ante 429)
6. Persistencia y seguimiento


---


# Motor de reglas + puntuación

- Cada **regla** es una condición simple sobre el mercado, del tipo "el precio
  está por encima de la media de 50 días". Responde sí o no, y explica por
  qué enseñando los números.
- Una **estrategia** junta varias reglas que deben cumplirse **todas a la
  vez**. Cada regla tiene un **peso** (su importancia) y la estrategia una
  **nota mínima**: el resultado es una puntuación de 0 a 100.
- El **catálogo** es la lista cerrada de indicadores disponibles, con la ficha
  de cada uno: qué valores acepta (por ejemplo la SMA admite 20, 50 o 200 días),
  cómo se calcula y dónde puede usarse.
- Actúa como guardián: si pides una media de 100 días, lo rechaza antes de
  calcular nada, en vez de fallar en silencio a mitad del cálculo.
- Todo queda registrado: se puede ver regla por regla cuáles se cumplieron.


---


# Arquitectura hexagonal

![Diagrama de arquitectura hexagonal](../../image/arquitectura_hexagonal.png)

De fuera adentro: Thymeleaf solo presenta; Application orquesta los casos de
uso; el Dominio (`Rule`, `Strategy`, `RuleEvaluator`) no depende de ningún
framework; Infrastructure adapta la base de datos, las APIs y la IA. Cambiar
de proveedor significa tocar un solo adaptador.


---

# Stack y calidad

- **Backend**: Java 21 · Spring Boot · JPA · WebClient · Thymeleaf + Bootstrap 5
- **Datos**: H2 (dev) · MariaDB (prod) · Finnhub / Polygon / Finviz / OpenRouter
- **Calidad**: 1165 tests, JaCoCo ≥80%, **SonarQube A** (63→0 issues), CI en PRs
- **Despliegue**: Docker + VPS propio con Nginx/SSL → <https://tfm.rubentouceda.es>
- Se evaluó Railway, pero la comunicación con la base de datos era lenta;
  el VPS además amplió conocimientos (endurecimiento, fail2ban, backups,
  red y certificados gestionados a mano)


---

# Desarrollo con IA + Sello Propio

### 1. Ecosistema de Asistentes
* **Copilot:** Autocompletado en editor y creación automatizada de ramas y apertura de PRs para revisión iterativa mediante comentarios.
* **OpenCode:** Modos *Plan* y *Build* según criticidad (análisis arquitectónico, resolución de dudas complejas e implementación de componentes completos).

### 2. Marco de Control y Metodología
* **Regla de oro:** `1 Rama = 1 Tema = 1 Documento (/docs) = 1 PR`.
* **Directrices:** Contextualización del agente mediante directivas en `AGENTS.md`.
* **CI/CD:** GitHub Actions (compilación + tests en cada PR) y Quality Gate SonarQube verificado antes de mergear a `main`.

### 3. El Sello Propio
* **Criterio humano:** Refactorización manual de propuestas, ajuste fino (*tuning*) y resolución de imprevistos.
* **Cobertura garantizada:** Modificación e incorporación de tests unitarios específicos para auditar el código generado.

> *"La IA propone, el desarrollador dispone."*


---

# La aplicación: Acceso

![Portada w:560px](<Imagenes/1-Inicio.png>)

Portada pública con las 4 áreas que la componen, el resto exige login.

---

# La aplicación: Login

![Login w:560px](<Imagenes/2-Login.png>)

Login (usuario de prueba en el formulario de entrega), con bloqueo anti-fuerza bruta: tras 3 fallos, el usuario queda bloqueado 30 minutos y se muestra un aviso.

---

# La aplicación: Reglas

![Catálogo de reglas w:560px](<Imagenes/4-Reglas.png>) ![Alta de regla w:560px](<Imagenes/6-Alta regla.png>)

El catálogo válido vive en código (PRICE, SMA, RSI, VOLUMEN…) y la tabla permite gestionarlo: alta, edición y borrado. El alta es guiada: al elegir indicador, la pantalla activa o apaga el parámetro sola. Con estos indicadores se construyen las reglas de las estrategias.

---

# La aplicación: Estrategias

![Lista de estrategias w:560px](<Imagenes/7-Estrategias.png>)

Aquí se pueden definir y listar todas las estrategias.
Ejemplo: "El sandwich": compra en pullback sobre SMA50, 9 reglas, umbral 100%, con
objetivo (SMA20), stop (SMA50) y capital a arriesgar definidos.

---

# La aplicación: Detalle de estrategia
![Detalle de estrategia w:560px](<Imagenes/9-Estrategia.png>)

Vista detalles de la estrategia, con sus reglas y pesos individuales, nota mínima de aprobación y gestión de riesgo.

--- 

# La aplicación: Alta de estrategia

![Alta de estrategia h:400px](<Imagenes/8-Alta estrategia.png>)

Nombre y descripción, nota mínima de aprobación (0-100), gestión de riesgo
y reglas de ejecución con su peso individual.

---

# La aplicación: Filtro de seguridad

![Filtro de prohibidos h:400px](<Imagenes/10-Filtro prohibidos.png>)

Se pueden definir filtros para evitar acciones de riesgo (p.ej. ETF, trust, apalancados 2X/3X, etc.) y que no se evalúen nunca. Optimizamos el tiempo de análisis y evitamos acciones volátiles. El filtro se aplica en la ingesta de tickers y en la evaluación de estrategias.

---

# La aplicación: alta y evaluación

![Panel de análisis w:1000px](<Imagenes/11-Analisis.png>)

Se escribe el ticker, se elige estrategia y se pulsa Añadir (en
<https://tfm.rubentouceda.es>, con usuario de prueba del formulario). La app
descarga datos, evalúa las reglas y calcula score, R:R y plan de trade.
FEIM: APROBADO 100%, R:R 8,58. CBLL: FALLIDO 77,78% — se descarta aquí.

---

# La aplicación: detalle del candidato

![Detalle FEIM w:560px](<Imagenes/12-Detalle analissi ticker ok.png>)

El detalle muestra veredicto, cumplimiento, plan (entrada, target, stop-loss)
y velas. "Generar análisis" lanza la IA en segundo plano con banner de
progreso: se puede seguir navegando.

---
# La aplicación: análisis IA

![Generando análisis w:560px](<Imagenes/13-Generacion analisis IA.png>)

Se lanza la IA en segundo plano, con banner de progreso y rotación de modelos ante fallo.

---

# La aplicación: síntesis IA

![Síntesis IA en 4 secciones w:950px](<Imagenes/14-Resultado analisis IA.png>)

La IA redacta 4 secciones citando las métricas (R:R 8,58:1, stop 74,05…).


---

# La aplicación: gráfico

![Gráfico con SMA h:380px](<Imagenes/15-Grafico ticker.png>)

Gráfico completo con SMA 20/50/200 para el estudio visual antes de proceder al análisis fundamental.


---

# La aplicación: descarte
Ante un ticker que no cumple la estrategia, se muestra el veredicto y el detalle de las reglas fallidas. En este caso, CBLL falla con 77,78% y se descarta y explica qué 2 reglas fallaron (Precio\<SMA20, RSI14).

![Descarte explicado h:380px](<Imagenes/16-Ticker analisis fallido.png>)

---

# Limitaciones y futuro

**Hoy no hace**: recomendar inversión · operar · tiempo real · notificaciones
**Líneas futuras**:
- OpenAPI pública cuando haya API REST real
- Servicio ML separado (Python/FastAPI) desacoplado del motor
- Migración Spring Boot 4.x (fin del soporte OSS 3.5.x)
- Actualización datos en tiempo real (WebSocket) y alertas.
- Indicadores adicionales y estrategias más complejas
- Integración en gráfico puntos de entrada y salida, soporte y resistencia, etc.

---

# Gracias

- Código en GitHub y aplicación funcionando en https://tfm.rubentouceda.es
- Resto de URLs (slides, formulario) en la documentación de entrega

