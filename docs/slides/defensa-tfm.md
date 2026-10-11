---
marp: true
theme: default
paginate: true
footer: "TFM · Motor de Análisis Técnico · Rubén Touceda"
---

<!-- _class: lead -->

# Motor de Análisis Técnico de Acciones

## Evaluación determinista + IA solo interpretativa

**Rubén Touceda** · Máster Desarrollo con IA (Big School) · Octubre 2026

<!--
Nota: Preséntate en 20 segundos: ingeniero que invierte por hobby y quería decisiones con base cuantitativa, no corazonadas. El proyecto es real y personal: lo usas tú. Eso responde al "proyecto original que te ilusione" del TFM.
-->

---

# El problema

- Invertir por intuición, foros o titulares: **sin base cuantitativa**
- Las herramientas existentes o son cajas negras o exigen programar
- La IA generativa sola **inventa**: no es reproducible ni auditable

**Pregunta del proyecto**: ¿se puede evaluar una estrategia técnica de forma
determinista, explicable… y que la IA solo la interprete?

<!--
Nota: Cuenta el problema en 30 segundos con un ejemplo: "Precio > SMA50 y volumen alto, ¿cuándo se cumplió y con qué riesgo/beneficio?" Sin números no hay decisión, solo opinión.
-->

---

# La idea: división clara

1. **Motor determinista**: reglas técnicas → score 0-100 + R:R + cumplimiento
2. **IA solo interpretativa**: redacta la síntesis desde métricas ya calculadas

Si la IA falla (429, vacíos), la evaluación numérica **sigue intacta** y la
vista muestra un aviso amistoso con fallback.

<!--
Nota: Esta es LA frase del TFM: determinista para decidir, IA para interpretar. Si el tribunal se queda con una idea, que sea esta. Anticípate: "¿y si falla la IA?" → "nada se rompe, lo veréis en la demo".
-->

---

# ¿Cómo funciona?

1. Ingesta de tickers o sugerencias (Finviz) con filtro de seguridad
2. Enriquecimiento: OHLCV + SMA 20/50/200 y volumen (Polygon/Finnhub)
3. Evaluación declarativa: `Precio > SMA(50)` AND `Volumen > Media(20)`
4. Cálculo de R:R y score ponderado 0-100 por estrategia
5. Análisis IA a petición (async, con rotación de modelos ante 429)
6. Persistencia y seguimiento

<!--
Nota: Recorre el flujo señalando que cada paso es un caso de uso independiente (ManageAnalyzeTicker, SuggestTickers, EvaluateStrategy…). Si preguntan "¿dónde está la lógica?", respuesta: en el dominio, nunca en los controladores.
-->

---

# Arquitectura hexagonal

![Diagrama de arquitectura hexagonal](../../image/arquitectura_hexagonal.png)

<!--
Nota: Explica de fuera a dentro: Thymeleaf/MVC solo presenta; Application orquesta casos de uso; Domain (Rule, Strategy, RuleEvaluator) no importa nada de Spring — verificado; Infrastructure adapta JPA, APIs e IA. Cambiar de proveedor = tocar un adapter.
-->

---

# Motor de reglas + scoring

- `Rule`: condición autocontenida → booleano + justificación
- `Strategy`: composición AND + `threshold`; cada regla tiene `weight`
- Catálogo de capacidades: qué indicador acepta qué parámetros (rechaza
  `SMA(100)` antes de evaluar, sin fallos silenciosos)
- Todo queda auditado: qué regla falló y por qué

<!--
Nota: Enseña un ejemplo vivo si puedes: estrategia PRICE > SMA50 con threshold 70. El scoring ponderado fue el bloque central del cierre (4 PRs). El catálogo es el "guardián" contra combinaciones imposibles.
-->

---

# Fronteras de la IA (diseño intencionado)

| Motor determinista | IA interpretativa |
|---|---|
| Evalúa reglas y métricas | Redacta síntesis desde métricas |
| Reproducible, testeable | No decide, no valida, no señaliza |
| 1165 tests lo cubren | Si falla → fallback + aviso |

Modelo principal `laguna-s-2.1` + 4 reservas · Planes gratuitos: latencias
altas **asumidas por diseño**, importa la corrección, no la velocidad.

<!--
Nota: Este slide blinda la pregunta "¿y la IA no se equivoca?". Respuesta: puede fallar el texto, nunca los números. Menciona el caso real de tus logs: los 5 modelos en 429 y la app siguió funcionando con fallback.
-->

---

# Stack y calidad

- **Backend**: Java 21 · Spring Boot · JPA · WebClient · Thymeleaf + Bootstrap 5
- **Datos**: H2 (dev) · MariaDB (prod) · Finnhub / Polygon / Finviz / OpenRouter
- **Calidad**: 1165 tests, JaCoCo ≥80%, **SonarQube A** (63→0 issues), CI en PRs
- **Despliegue**: Docker + VPS propio con Nginx/SSL → <https://tfm.rubentouceda.es>

<!--
Nota: Si preguntan por qué VPS y no Railway: control total, reproducible con Docker, healthchecks y pipeline GHCR. Los números (1165, 63→0) son tu prueba de "buen proyecto de software", no un fichero de 10.000 líneas.
-->

---

# Desarrollo con IA: 3 fases

1. **Copilot**: arranque, estructura base, autocompletado
2. **OpenCode supervisado**: 1 rama = 1 tema = 1 doc = 1 PR, validación con
   menú, sin avanzar sin aprobación (~25 PRs, `docs/` con trazabilidad total)
3. **Desarrollo propio**: diseño, tuning, imprevistos de producción

Frontera sagrada: **la IA propone, la persona dispone**.

<!--
Nota: Aquí demuestras lo que el máster evalúa: no "código escupido por IA", sino proceso disciplinado. Cita artefactos: AGENTS.md §3, docs/prompts/ (9 patrones), ADRs. El tribunal valora originalidad + disfrute: di qué parte disfrutaste más.
-->

---

# Demo (5 min)

En <https://tfm.rubentouceda.es>:

1. Login → `/analysis`: ingesta de un ticker y su evaluación con score
2. Detalle del ticker: velas + SMA + plan de riesgo
3. Generar análisis IA → banner con progreso → síntesis en 4 secciones
4. `/strategies`: crear estrategia y ver sugerencias

<!--
Nota: Ten abierta también la URL local por si falla la red en la defensa. Si la IA da 429 en directo, perfecto: enseñas el fallback amistoso (es una feature, no un fallo). No enseñes más de 4 pantallas.
-->

---

# Limitaciones y futuro

**Hoy no hace**: recomendar inversión · operar · tiempo real · notificaciones
**Líneas futuras**:
- OpenAPI pública cuando haya API REST real
- Servicio ML separado (Python/FastAPI) desacoplado del motor
- Migración Spring Boot 4.x (fin del soporte OSS 3.5.x)

<!--
Nota: Sé honesto con lo que NO hace (el disclaimer está en el README). Cada futuro enlaza con una decisión ya tomada: Swagger descartado a posta, ML desacoplado por hexagonal, Boot 4.x como riesgo aceptado documentado.
-->

---

# Cierre: las 5 piezas

- ✅ README que se entiende sin abrir código
- ✅ Código supervisado en GitHub
- ✅ Despliegue productivo funcionando
- ✅ Estas slides + speaker notes versionadas
- ⬜ Formulario final con las URLs

**Gracias** — preguntas.

<!--
Nota: Cierra con las 5 piezas del TFM marcadas y pide preguntas. Ten a mano: repo, deploy, PDF de slides y formulario. Termina con qué te llevas del máster en una frase.
-->
