# Task 2026-10-14 — Slides Defensa (Marp + PDF + PPTX)

## Título
Deck de defensa: 12 slides + speaker notes en Marp, exportado a PDF y PPTX.

## Resumen
- Rama `feature/slides-defensa` (desde `main` post-#188).
- Previo: commit `chore: ignore Requisitos-TFM.md` (cambio del usuario,
  incluido en esta rama a petición suya).
- Formato elegido: **Marp Markdown** (`docs/slides/defensa-tfm.md`, versionable)
  + `defensa-tfm.pdf` (entrega universal) + `defensa-tfm.pptx` (editable).
  Verificado: marp-cli 4.5.1 vía npm + Chrome para PDF.
- Contenido según `Requisitos-TFM.md` §8 (cómo creado, problema,
  funcionamiento, transmitir valor) + estructura del plan (problema →
  arquitectura → IA → demo → métricas → lecciones → futuro). Sin herramienta
  impuesta por el TFM (§15): Marp es decisión propia documentada aquí.
- Re-exportar tras cambios:
  `marp --pdf|--pptx --allow-local-files docs/slides/defensa-tfm.md -o ...`
- Pendiente (usuario): subir PDF a Drive/Slides para la URL de entrega y
  añadirla al README + formulario (§9 Requisitos-TFM).
- Verificado: 12 slides, 12 notas (PPTX), diagrama hexagonal incrustado,
  PDF válido de 12 páginas.

## Código generado
No aplica (docs + binarios de presentación).

## Decisiones técnicas tomadas
- Marp frente a Google Slides manual: fuente en git (revisión + diff),
  speaker notes versionadas, export reproducible a PDF/PPTX.
- Diagrama existente reutilizado (`image/arquitectura_hexagonal.png`).
- Datos veraces del repo (1165 tests, SonarQube A 63→0, URLs, modelos).

## Cobertura de tests y pruebas añadidas
- Sin código: no hay tests. Verificación = conteo slides/notas + PDF válido.
- Docker omitido: solo `docs/` (sin impacto en ejecución, excepción §3.7).

## Advertencias de SonarQube o arquitectura
- Ninguna.

## Próximos pasos sugeridos
1. Validar esta PR con menú (la tarea se detiene aquí según AGENTS.md §3.8).
2. Tras MERGED: subir PDF a Drive, URL al README + formulario (15 Oct:
   Rehearsal + 16 Oct: Buffer/tag `tfm-v1.0`).
