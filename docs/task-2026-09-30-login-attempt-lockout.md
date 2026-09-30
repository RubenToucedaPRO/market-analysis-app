# Task 2026-09-30 — Login TFM (3 intentos + bloqueo 30 min + logs sin secretos)

## Resumen
- Rama: `feature/login-attempt-lockout` (desde `main` en `3d982e2`, limpio).
- 3 fallos → bloqueo 30 min por username (en memoria); ni la contraseña correcta entra bloqueado.
- Logs SLF4J solo con `user`, `ip` y `attemptsLeft`. Nunca password ni tokens.
- Ficheros nuevos en `infrastructure/config/security/` (detalle de seguridad, fuera del dominio):
  - `LoginAttemptService` — `ConcurrentHashMap` + `Clock` por constructor, `MAX_ATTEMPTS=3`, `LOCK_DURATION=30min`, normalización lowercase, expiración con limpieza.
  - `LoginBlockFilter` (`OncePerRequestFilter` antes de `UsernamePasswordAuthenticationFilter`) — rechaza el POST /login bloqueado con `302 /login?locked` sin llegar a autenticar.
  - `LoginFailureHandler` — cuenta fallos: `?error` (1º-2º) / `?locked` (3º), WARN sin secretos.
  - `LoginSuccessHandler` — resetea contador, INFO sin secretos, `302 /analysis` (sustituye a `defaultSuccessUrl`, que se ignora con handler propio).
- Modificados: `SecurityConfig` (bean `LoginAttemptService` + filtro + handlers; proveedor en memoria y `permitAll` intactos), `login.html` (alerta `param.locked` con `th:text`), `messages.properties` (`login.locked`).

## Decisiones técnicas tomadas
- Filtro previo en vez de `AuthenticationProvider` decorador: determinista (sin depender del orden de proveedores del `AuthenticationManager` padre) y testeable con MockMvc.
- Detección del POST vía `requestURI - contextPath` (no `servletPath`): en MockMvc `servletPath` es `""` y el filtro no saltaba — lo cazó el test `correctPasswordShouldBeRejectedWhileBlocked`.
- En memoria (no BD): el bloqueo es mitigación efímera de fuerza bruta; se pierde al reiniciar (documentado, defendible en TFM).
- `clear()` público como test support para aislar los tests MockMvc (bean real compartido por la clase).

## Cobertura de tests y pruebas añadidas
- `LoginAttemptServiceTest` (7 tests, `Clock` fijo + `MutableClock`): fresco no bloqueado, 1-2 fallos restan intentos, 3º bloquea, expiración a 30 min resetea, éxito resetea, case-insensitive + null-safe, aislamiento por usuario.
- `LoginLockoutTest` (5 tests MockMvc): 2 fallos → `?error`, 3º → `?locked`, credencial buena bloqueada → `?locked`, éxito resetea a 3 intentos, `GET /login?locked` 200.
- `SecurityConfigTest` existente: sin tocar, sigue en verde (éxito → `/analysis`, fallo → `?error`).
- Suite: `mvn test` → **1087/1087, BUILD SUCCESS** (12 nuevos: 7 unitarios + 5 MockMvc).
- Verificación runtime Docker (`docker compose up --build -d`): `/health` UP, `GET /login` 200, `?locked` muestra "bloqueada temporalmente"; con curl+CSRF: `?error`, `?error`, `?locked`, y con contraseña buena → `?locked` (`LoginBlockFilter: login_blocked` en logs). Nota: el `admin` del contenedor local quedó bloqueado 30 min (solo memoria local, se limpia al reiniciar).

## Advertencias de SonarQube o arquitectura
- Sin field injection (constructor + `@Bean`); `th:text` (no `th:utext`); CSRF intacto (`th:action`); constantes con nombre (sin números mágicos); sin lógica de negocio en la vista.
- Deuda visible (fuera de alcance): password con `{noop}` y `UserDetailsService` en memoria; IP vía `getRemoteAddr()` (tras proxy sería la del proxy).

## Próximos pasos sugeridos
1. Validar (menú abajo) → commits `feat:` + `test:` + `docs:` → push → `gh pr create --base main`.
2. Tras el merge: pasar la guía VPS → `chore/vps-deploy` (config; despliegue sobre `main`).

## B. Ideas futuras (no hacer ahora)
- Persistir intentos en BD; rate-limit por IP; pasar password a bcrypt.
