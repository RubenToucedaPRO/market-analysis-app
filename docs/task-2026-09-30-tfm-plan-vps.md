# Task 2026-09-30 — Re-plan a VPS + Login TFM

## Resumen
- Rama: `docs/tfm-plan-vps` (desde `main` en `4a0bd7c`, limpio).
- Se actualiza `docs/tfm-closure-plan.md`:
  - Deploy Railway → **Deploy VPS** (Railway queda como histórico; PR #170 sigue OPEN pendiente de cierre).
  - Nuevo alcance: **Login TFM** — 3 intentos + bloqueo 30 min en memoria (por username) + logs sin secretos.
  - Día 3 PM desdoblado en dos filas: Login TFM (ejecutable ya) y Deploy VPS (bloqueado hasta la guía).
  - Scope locked, DoD funcional, notas de partida, decisiones y próximo paso inmediato alineados con lo anterior.
  - Revisión 30 Sep con el trabajo mergeado desde el 17 Sep (#163-#172).

## Decisiones técnicas tomadas
- Login en memoria (`ConcurrentHashMap` + `Clock` inyectado): suficiente para TFM, sin migración BD, testeable con reloj fijo.
- Bloqueo por username normalizado (lowercase): la app es monousuario (`APP_SECURITY_*`), pero el diseño queda genérico y defendible.
- Logs SLF4J sin secretos: solo username, IP, intentos restantes y `lockedUntil`. Nunca password ni tokens.
- Deploy VPS bloqueado hasta la guía: se deja por escrito qué debe traer (SO/SSH, stack, Nginx+SSL, dominio, secretos, perfil prod, healthcheck) para no avanzar a ciegas.

## Cobertura de tests y pruebas
- Cambio solo en `/docs`: no hay código que cubrir ni tests que añadir. No se ejecuta `mvn test` (nada que compilar) ni verificación Docker (no se toca `src/`, `pom.xml`, compose, plantillas ni properties). Se indica explícitamente según AGENTS.md §3.7.

## Advertencias de SonarQube o arquitectura
- Ninguna: sin código. Hexagonal intacta.

## Conflicto de flujo (Regla 10)
- La PR #170 (`docs/railway-deploy`) sigue OPEN. Se abre esta rama por petición explícita del usuario (excepción prevista a la Regla 10).

## Próximos pasos sugeridos
1. Validar esta tarea (menú abajo) → `git add` de los 2 ficheros → commit `docs:` → push → `gh pr create --base main`.
2. `feature/login-attempt-lockout` (diseño ya propuesto en el mensaje anterior).
3. Pasar la guía VPS → `chore/vps-deploy` (solo cambios de config; el despliegue se ejecuta sobre `main` tras el merge).
4. Decidir PR #170 (merge o cierre como superada).

## B. Ideas futuras (no hacer ahora)
- Migrar el registro de intentos a BD si el tribunal lo pide (persistencia ante reinicios).
- Rate-limit por IP además de por username.
