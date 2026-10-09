# ADR 0001 — Arquitectura Hexagonal + Clean Architecture

- **Estado:** Aceptada (2026-10-13; vigente desde el inicio del proyecto).
- **Fecha:** 2026-10-13.

## Contexto
La aplicación evalúa estrategias de análisis técnico con un motor determinista,
consume APIs externas de mercado (Finnhub, Polygon, Finviz), persiste en
MariaDB/H2 y genera análisis interpretativo con LLMs vía OpenRouter. Sin una
separación estricta, los detalles técnicos (Spring, JPA, WebClient, Thymeleaf)
se filtrarían a la lógica de negocio y cualquier cambio de proveedor obligaría
a tocar el núcleo.

## Decisión
Estructurar el sistema en capas concéntricas con dependencias solo hacia
adentro (DIP), siguiendo `docs/architecture-walkthrough.md`:

- **Domain** (`domain/model`, `domain/service`, `domain/port/in|out`,
  `domain/exception`): entidades, reglas, evaluador y contratos. Cero imports
  de Spring/Jakarta (verificado: solo JDK + Lombok + código propio).
- **Application** (`application/usecase`, `mapper`, `dto`, `job`): casos de uso
  que orquestan dominio y puertos. Sin reglas de negocio.
- **Infrastructure** (`persistence`, `external/*`, `config`, `monitoring`,
  `migration`): adaptadores JPA, WebClient/RestClient, Jsoup, OpenRouter.
- **Presentation** (`controller`, `dto`, `exception`): controladores Spring MVC
  + Thymeleaf como adaptadores de entrada; solo validación básica y delegación.

## Consecuencias
- Cambiar de proveedor (p. ej. Finnhub→otro) solo toca un adapter.
- El dominio se testea sin Spring (tests unitarios puros y rápidos).
- Coste: más ficheros y mapeadores entre capas (DTO↔dominio↔JPA).
