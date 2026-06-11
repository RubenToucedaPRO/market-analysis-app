# Tarea: Home Landing Page + Spring Security Básico

**Fecha:** 2026-06-10
**Slug:** home-landing-login

---

## Resumen

Se implementó una **landing page pública** (`/`) con presentación de la aplicación y **Spring Security básico** con login por formulario. La home utiliza una navbar simplificada; las páginas internas mantienen la navbar existente con botón de logout añadido.

---

## Archivos modificados

| Archivo | Acción |
|---------|--------|
| `pom.xml` | Añadidas dependencias `spring-boot-starter-security`, `thymeleaf-extras-springsecurity6`, `spring-security-test` |
| `src/main/java/.../infrastructure/config/SecurityConfig.java` | **Nuevo** — Configuración Spring Security |
| `src/main/java/.../presentation/controller/HomeController.java` | **Nuevo** — Controlador para `/` y `/login` |
| `src/main/java/.../presentation/util/WebConstants.java` | Añadidas constantes `TEMPLATE_HOME`, `TEMPLATE_LOGIN` |
| `src/main/resources/templates/home.html` | **Nuevo** — Landing page |
| `src/main/resources/templates/login.html` | **Nuevo** — Página de login |
| `src/main/resources/templates/fragments/navbar-public.html` | **Nuevo** — Fragment navbar simplificada |
| `src/main/resources/templates/fragments/navbar.html` | Añadido botón de Logout con `sec:authorize` |
| `src/main/resources/static/css/styles.css` | Añadidos estilos para home, features, screenshots, CTA |
| `src/main/resources/messages.properties` | Añadidas 23 entradas i18n para home y login |
| `src/test/.../HomeControllerTest.java` | **Nuevo** — Tests del controlador home |
| `src/test/.../SecurityConfigTest.java` | **Nuevo** — Tests de integración de seguridad |
| `src/test/.../StrategyControllerViewTest.java` | Añadido `@AutoConfigureMockMvc(addFilters = false)` |
| `src/test/.../ProhibitedTickerControllerTest.java` | Añadido `@AutoConfigureMockMvc(addFilters = false)` |
| `src/test/.../HealthCheckControllerTest.java` | Añadido `@AutoConfigureMockMvc(addFilters = false)` |

---

## Decisiones técnicas

1. **Spring Security básico**: `InMemoryUserDetailsManager` con usuario `admin`/`admin`. Suficiente para MVP; migrable a DB con `JdbcUserDetailsManager`.

2. **CSRF habilitado**: Se mantiene la protección CSRF por defecto. Los formularios existentes ya incluían `th:if="${_csrf != null}"`.

3. **Rutas públicas**: Solo `/`, `/login`, y estáticos (`/css/**`, `/js/**`, `/images/**`). Todo lo demás requiere autenticación.

4. **Navbar dual**: `navbar-public.html` para home/login, `navbar.html` (modificada) para páginas autenticadas con botón de logout.

5. **Tests**: Se añadió `@AutoConfigureMockMvc(addFilters = false)` a los tests `@WebMvcTest` existentes que no necesitan verificar seguridad, evitando regresiones.

---

## Cobertura de tests

- **Tests nuevos**: 7 (2 HomeController + 5 SecurityConfig)
- **Tests existentes actualizados**: 3 archivos (StrategyControllerViewTest, ProhibitedTickerControllerTest, HealthCheckControllerTest)
- **Total suite**: 1045 tests, 0 fallos

### Tests nuevos detallados

| Test | Descripción |
|------|-------------|
| `HomeControllerTest.shouldReturnHomeView` | `GET /` retorna vista "home" |
| `HomeControllerTest.shouldReturnLoginView` | `GET /login` retorna vista "login" |
| `SecurityConfigTest.homePageShouldBePublic` | `/` accesible sin auth |
| `SecurityConfigTest.loginPageShouldBePublic` | `/login` accesible sin auth |
| `SecurityConfigTest.protectedPageShouldRedirectToLogin` | `/analysis` redirige a login |
| `SecurityConfigTest.loginWithValidCredentialsShouldSucceed` | Login válido → redirect a `/analysis` |
| `SecurityConfigTest.loginWithInvalidCredentialsShouldFail` | Login inválido → redirect a `/login?error` |

---

## Pendiente (usuario)

- **Screenshots**: Añadir imágenes a `src/main/resources/static/images/`:
  - `screenshot-analysis.png`
  - `screenshot-strategies.png`
  - `screenshot-strategy-detail.png`
  - `screenshot-rule-definitions.png`

---

## Próximos pasos sugeridos

1. Migrar a `BCryptPasswordEncoder` y usuarios en DB (`JdbcUserDetailsManager`)
2. Añadir página de registro de usuarios
3. Implementar roles (ADMIN/USER) si se necesita RBAC
4. Añadir rate limiting al endpoint de login
