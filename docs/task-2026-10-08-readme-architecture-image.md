# Task 2026-10-08 — README apunta al PNG de arquitectura hexagonal

## Título
Corregir referencia rota a `image/arquitectura_hexagonal.jpeg` en `README.md`
(apunta a `image/arquitectura_hexagonal.png`).

## Resumen
- `README.md:13` referenciaba `/image/arquitectura_hexagonal.jpeg`, archivo que no
  existe en el árbol actual → imagen rota en GitHub y en cualquier render del README.
- Investigación en historial (`git log --all -- "*arquitectura_hexagonal*"`):
  el commit `153c770a` (11 Jun 2026, "feat: Add security configuration…") eliminó el
  `.jpeg` (95 KB, blob `e9a5ba01`) y añadió en el mismo commit el `.png`
  (661 KB, 1024×559). No fue borrado accidental: fue reemplazo de formato, pero la
  referencia del README quedó sin actualizar.
- Verificado que `image/arquitectura_hexagonal.png` existe, es un PNG válido y su
  contenido es el diagrama de arquitectura hexagonal + clean architecture del TFM
  (DOMAIN / APPLICATION / INFRASTRUCTURE & PRESENTATION + adaptadores).
- Decisión del usuario (menú 8 Oct): apuntar el README al PNG existente en vez de
  restaurar el JPEG original del historial (se conserva mayor resolución, 0 binarios
  nuevos).
- Rama: `fix/readme-architecture-image` (desde `main` sincronizado tras el merge de
  #183; estado limpio; regla 10 AGENTS.md cumplida). Cambio: 1 línea en README.

## Código generado
```diff
-![Diagrama de Arquitectura Hexagonal](/image/arquitectura_hexagonal.jpeg)
+![Diagrama de Arquitectura Hexagonal](/image/arquitectura_hexagonal.png)
```

## Decisiones técnicas tomadas
- No se restaura el `.jpeg` del historial (`git show 7101752d:image/...` disponible si
  algún día hiciera falta): duplicar el binario solo añadiría peso al repo.
- No se toca `image/`: el PNG ya está versionado y es correcto.
- Micro-tarea en rama propia (una rama = un tema = un doc); no se mezcla con el
  README Final pendiente, que irá en su propia rama/PR.

## Cobertura de tests y pruebas añadidas
- Sin cambios de código: no se añaden ni modifican tests.
- No se ejecuta `mvn test` (cambio solo `README.md`; sin comportamiento que cubrir).
- Verificación runtime Docker omitida: `README.md` no está en la lista de rutas con
  impacto en ejecución (AGENTS.md §3.7) — se indica aquí explícitamente.

## Advertencias de SonarQube o arquitectura
- Ninguna: cambio de documentación, sin impacto en arquitectura ni calidad de código.

## Próximos pasos sugeridos
1. Validar esta micro-PR con menú (la tarea se detiene aquí según AGENTS.md §3.8).
2. Tras MERGED: **README Final** en rama propia (badges, URL deploy
   `https://tfm.rubentouceda.es`, sección "Desarrollo con IA", troubleshooting +
   tabla endpoints JSON) → doc → menú → PR.
