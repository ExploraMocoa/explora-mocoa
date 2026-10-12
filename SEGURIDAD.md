# Seguridad del proyecto — Spring Security + JWT + BCrypt

Este documento explica **qué se creó, qué se modificó y cómo funciona** la seguridad
implementada en `expora_mocoa`: autenticación con **JWT**, autorización por
**roles** (`TURISTA`, `PROPIETARIO`, `ADMIN`) y encriptado de contraseñas con
**`PasswordEncoder` (BCrypt)**. También cubre el **usuario ADMIN inicial**
que se crea al arrancar el proyecto por primera vez.

---

## 1. Resumen rápido

| Necesidad | Solución |
|---|---|
| Login / registro | `AuthController` (`POST /api/auth/login`, `POST /api/auth/register`) |
| Generar y validar tokens | `JwtService` (librería JJWT) |
| Interceptar cada request y autenticar por token | `JwtAuthenticationFilter` |
| Cargar usuario + rol desde la BD | `CustomUserDetailsService` |
| Reglas: qué ruta exige qué rol | `SecurityConfig` |
| Encriptar contraseñas | `PasswordEncoder` (bean `BCryptPasswordEncoder`) en `SecurityConfig` + uso en `UsuarioService`, `AuthController` y `DataInitializer` |
| Crear roles + admin al arrancar | `config/DataInitializer` (CommandLineRunner) |
| No exponer la contraseña en los JSON | `@JsonProperty(WRITE_ONLY)` en `Usuario.contrasena` |
| Respuestas 401/403 claras | `GlobalExceptionHandler` ampliado |
| Configuración (clave JWT, expiración, admin) | `application.properties` |
| Dependencias | `pom.xml` (`spring-boot-starter-security` + `jjwt-*`) |

**Compilación verificada:** `./mvnw -q -o compile -DskipTests` → `EXIT:0`, y se
confirmó la generación de `target/classes/com/expora_mocoa/security/*.class`
y `target/classes/com/expora_mocoa/config/DataInitializer.class`.

---

## 2. Dependencias nuevas (`pom.xml`) — MODIFICADO

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.12.6</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.12.6</version>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>0.12.6</version>
    <scope>runtime</scope>
</dependency>
```

**Cómo funciona cada una:**

- **`spring-boot-starter-security`**: trae Spring Security (filtros, `AuthenticationManager`,
  `PasswordEncoder`, anotaciones `@PreAuthorize`, manejo de 401/403). Sin este starter,
  todas las rutas estarían abiertas.
- **`jjwt-api` / `jjwt-impl` / `jjwt-jackson`**: librería JJWT para crear y validar
  JWT firmados (HS256). Se separan en 3 artefactos por diseño de JJWT 0.12.x:
  la API, la implementación y el serializador JSON con Jackson. `jjwt-jackson`
  convierte los *claims* del token a/desde JSON.

---

## 3. Archivos CREADOS

### 3.1 `security/JwtService.java` — crear y validar tokens

**Responsabilidad:** es la única clase que sabe firmar y leer JWT.

- `generateToken(UserDetails)`: construye el token con:
  - `subject` = correo del usuario (identidad),
  - claim extra `"rol"` = autoridad principal (ej. `ROLE_ADMIN`),
  - `issuedAt` (ahora) y `expiration` (ahora + `jwt.expiration`),
  - firma HMAC-SHA con la clave de `jwt.secret`.
- `extractUsername(token)`: lee el `subject` (correo).
- `extractRol(token)`: lee el claim `"rol"` (útil para el frontend).
- `isTokenValid(token, userDetails)`: comprueba que la firma sea válida
  (lo hace el parser), que el `subject` coincida con el usuario y que
  **no haya expirado**.
- `getSigningKey()`: decodifica la clave Base64 de `jwt.secret` y crea
  la `SecretKey` con `Keys.hmacShaKeyFor(...)`. La clave debe medir
  **mínimo 256 bits** para HS256; si es más corta, JJWT lanza excepción
  al arrancar/usar.

> Si cambias `jwt.secret` en producción, todos los tokens viejos quedan
> inválidos automáticamente (la firma ya no coincide). Es el comportamiento esperado.

### 3.2 `security/CustomUserDetailsService.java` — puente BD ↔ Security

**Responsabilidad:** decirle a Spring Security *quién es el usuario y qué rol tiene*.

- Implementa `UserDetailsService.loadUserByUsername(correo)`.
- Busca en `UsuarioRepository.findByCorreo(...)`; si no existe lanza
  `UsernameNotFoundException`.
- Convierte el rol de BD (`ADMIN`, `TURISTA`, `PROPIETARIO`) a autoridad
  Spring (`ROLE_ADMIN`, …). Esta convención es la que permite usar
  `hasRole("ADMIN")` en `SecurityConfig` (Spring agrega el prefijo `ROLE_`
  automáticamente al comparar).
- Devuelve un `org.springframework.security.core.userdetails.User` con:
  `username = correo`, `password = hash BCrypt`, `authorities = [rol]`.
- Lo usan **dos flujos**: el login (vía `AuthenticationManager`) y el
  `JwtAuthenticationFilter` (en cada request con token, para reconstruir
  el principal).

### 3.3 `security/JwtAuthenticationFilter.java` — filtro por request

**Responsabilidad:** ejecutarse **una vez por petición** (`OncePerRequestFilter`)
antes del filtro estándar de usuario/contraseña.

Flujo paso a paso:

1. Lee el header `Authorization`. Si no existe o no empieza por `Bearer `,
   **deja pasar** la cadena sin autenticar (los endpoints públicos lo permiten;
   los protegidos responderán 401/403 más adelante).
2. Extrae el token (quita el prefijo `Bearer `) y obtiene el correo con
   `jwtService.extractUsername(token)`.
3. Si hay correo y nadie se autenticó aún en este request, carga el
   `UserDetails` y valida el token con `jwtService.isTokenValid(...)`.
4. Si es válido, crea un `UsernamePasswordAuthenticationToken` con las
   autoridades y lo guarda en el `SecurityContextHolder` → el request queda
   **autenticado** con su rol para el resto de la cadena.
5. Si el token es inválido/expirado, limpia el contexto y deja que Spring
   responda 401/403 según la regla que aplique. El `try/catch` evita que
   un token corrupto tumbe el servidor con un 500.

### 3.4 `security/SecurityConfig.java` — reglas y beans

**Responsabilidad:** el corazón de la configuración. Declara 3 beans:

1. **`PasswordEncoder passwordEncoder()`** → retorna `BCryptPasswordEncoder`.
   - **¿Qué es BCrypt?** Función de hash de contraseñas con *salt* aleatoria
     y factor de costo (lentitud intencional). Es **irreversible**: nunca se
     puede "desencriptar"; para verificar, se hashea el intento y se compara.
     Cada `encode()` del mismo texto produce un hash distinto (por el salt),
     pero `matches(raw, hash)` valida correctamente.
2. **`AuthenticationManager authenticationManager(...)`** → expone el manager
   que `AuthController` usa en el login para comparar la contraseña enviada
   contra el hash BCrypt guardado.
3. **`SecurityFilterChain securityFilterChain(HttpSecurity)`** → define:
   - `csrf.disable()`: necesario en APIs REST con JWT (no hay cookies/sesión
     que proteger contra CSRF).
   - Sesión `STATELESS`: el servidor **no crea sesiones HTTP**; cada request
     debe traer su JWT. Escalable y sin estado.
   - `addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)`:
     nuestro filtro corre primero.
   - Matriz de autorización (ver tabla en §5).
   - `@EnableMethodSecurity`: además permite usar `@PreAuthorize("hasRole('ADMIN')")`
     en métodos si en el futuro quieres reglas más finas.

### 3.5 `security/AuthController.java` — login y registro públicos

**Rutas:** prefijo `/api/auth` (público según `SecurityConfig`).

- **`POST /api/auth/login`** con `{ "correo", "contrasena" }`:
  1. Delega en `authenticationManager.authenticate(...)`, que internamente usa
     `CustomUserDetailsService` + `PasswordEncoder.matches(...)`.
     Si falla → `BadCredentialsException` → el handler responde **401**.
  2. Genera el JWT con `JwtService` y devuelve `AuthResponse`
     `{ token, correo, nombre, rol }`.
- **`POST /api/auth/register`** con `{ nombre, correo, contrasena, rol? }`:
  1. Rechaza correos duplicados (**400**).
  2. Solo permite auto-registro como `TURISTA` (defecto) o `PROPIETARIO`.
     Si piden `ADMIN` → **400**. Así nadie se auto-asciende a admin.
  3. Busca el `Rol` en BD, **encripta** la contraseña con
     `passwordEncoder.encode(...)` y guarda.
  4. Genera el JWT y responde **201** con el `AuthResponse`.

### 3.6 `security/dto/LoginRequest.java`, `RegisterRequest.java`, `AuthResponse.java`

- **`LoginRequest`**: `{ correo, contrasena }` con `@Email` y `@NotBlank`.
  Evita recibir un `Usuario` completo en el login (más claro y valida mejor).
- **`RegisterRequest`**: `{ nombre, correo, contrasena, rol? }`. El `rol` es
  opcional y restringido a `TURISTA`/`PROPIETARIO` en el controlador.
- **`AuthResponse`**: `{ token, correo, nombre, rol }`. Es lo que el frontend
  guarda (típicamente en memoria o `localStorage`) y reenvía como
  `Authorization: Bearer <token>`.

### 3.7 `config/DataInitializer.java` — roles + admin inicial

**Responsabilidad:** `CommandLineRunner` con `@Order(1)` que Spring Boot ejecuta
**automáticamente al arrancar** (primera vez y todas las siguientes).

1. **Roles:** `crearRolSiNoExiste("ADMIN" | "PROPIETARIO" | "TURISTA")`.
   Usa `findByNombre(...)` + `orElseGet(save)` → **idempotente**: si ya existen,
   no duplica (la columna `nombre` además es `unique`).
2. **Admin inicial:** solo si `!usuarioRepository.existsByCorreo(adminCorreo)`:
   crea `Usuario` con nombre/correo/clave de `application.properties`
   (`admin.*`), **encriptando** con `passwordEncoder.encode(...)` y rol `ADMIN`.
   Si ya existe, solo imprime `">>> Usuario ADMIN ya existe..."`.
   Por eso es seguro reiniciar: **nunca crea duplicados**.

Valores por defecto (cámbialos en `application.properties`):

```properties
admin.nombre=Administrador
admin.correo=admin@expora.com
admin.contrasena=admin123
```

---

## 4. Archivos MODIFICADOS

### 4.1 `services/UsuarioService.java` — encriptado al crear/actualizar

- Ahora inyecta `PasswordEncoder` por constructor (Spring lo provee desde
  `SecurityConfig`).
- `create(...)`: antes de `save`, aplica `encriptarSiEsNecesario(...)`.
- `update(...)`: si el request trae contraseña no vacía, la encripta antes
  de guardarla; si viene vacía/nula, conserva la anterior (permite editar
  perfil sin cambiar clave).
- `encriptarSiEsNecesario(...)`: si el texto ya parece hash BCrypt
  (`$2a$`/`$2b$`/`$2y$`), lo deja intacto (evita doble-hash del seed);
  si no, aplica `passwordEncoder.encode(...)`.
- Se eliminó el `TODO` que recordaba encriptar cuando llegara Security.

### 4.2 `entities/Usuario.java` — la contraseña ya no se expone

- Nuevo import `JsonProperty` y anotación en el campo:
  `@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)`.
  Efecto: el campo **se acepta** en `POST/PUT` (lectura) pero **nunca se
  serializa** en las respuestas `GET` (escritura). Antes, cualquier
  `GET /api/usuarios` devolvía el hash; ahora el JSON ya no incluye `contrasena`.

### 4.3 `controllers/GlobalExceptionHandler.java` — errores 401/403 claros

Se agregaron dos manejadores (antes login fallido o rol insuficiente daban
respuestas genéricas o el HTML por defecto):

- `BadCredentialsException` → **401** `{ "error": "Credenciales inválidas..." }`.
- `AuthorizationDeniedException` / `AccessDeniedException` → **403**
  `{ "error": "Acceso denegado: no tienes el rol necesario" }`.

### 4.4 `resources/application.properties` — clave JWT y admin

```properties
jwt.secret=ZXhwb3JhX21vY29hX3NlY3JldF9rZXlfbXlfXzMyX2J5dGVzXzEyMzQ1Ng==
jwt.expiration=86400000
admin.nombre=Administrador
admin.correo=admin@expora.com
admin.contrasena=admin123
```

- `jwt.secret`: clave **Base64** de ≥256 bits para firmar (HS256).
  ⚠️ Cambia este valor en producción y no lo subas a repos públicos.
  Puedes generar una con: `openssl rand -base64 32`.
- `jwt.expiration`: vida del token en ms (`86400000` = 24 h).
- `admin.*`: credenciales del seed (solo se usan si el correo no existe).

### 4.5 `pom.xml` — ver §2.

---

## 5. Matriz de autorización (`SecurityConfig`)

| Ruta | Método | Acceso |
|---|---|---|
| `/api/auth/**` | todos | 🌍 Público (login/registro) |
| `/api/lugares/**`, `/api/eventos/**`, `/api/comidas/**`, `/api/categorias/**`, `/api/servicios/**`, `/api/resenas/**` | `GET` | 🌍 Público (catálogo visible sin cuenta) |
| `/api/lugares/**`, `/api/eventos/**`, `/api/comidas/**`, `/api/categorias/**`, `/api/servicios/**` | `POST`, `PUT`, `DELETE` | 🔒 `PROPIETARIO` o `ADMIN` (quienes publican) |
| `/api/resenas/**` (escritura; el `GET` ya es público) | `POST`, `PUT`, `DELETE` | 🔒 Autenticado (cualquier rol) |
| `/api/ordenes/**`, `/api/items-orden/**` | todos | 🔒 Autenticado (cualquier rol) |
| `/api/usuarios/**`, `/api/roles/**` | todos | 🔒 Solo `ADMIN` |
| cualquier otra | — | 🔒 Autenticado |

Notas:

- `hasRole("ADMIN")` ≡ `hasAuthority("ROLE_ADMIN")`. En BD se guarda `ADMIN`;
  el prefijo `ROLE_` solo existe del lado de Spring.
- Sin header o con token inválido → **401** en rutas autenticadas;
  con token válido pero rol insuficiente → **403**.
- Los `@CrossOrigin(origins = "*")` de los controladores se mantienen;
  si el frontend está en otro origen y usa `Authorization`, con eso basta
  para desarrollo.

---

## 6. Cómo funciona el flujo completo (ejemplo)

### 6.1 Arranque por primera vez

```
1. Spring crea el PasswordEncoder (BCrypt) y el resto de beans.
2. DataInitializer.run():
   - INSERT roles ADMIN, PROPIETARIO, TURISTA (si faltan).
   - INSERT usuario admin@expora.com con hash BCrypt y rol ADMIN.
3. La app queda lista: ya puedes loguearte como admin.
```

### 6.2 Registro de turista

```http
POST /api/auth/register
Content-Type: application/json

{ "nombre": "Ana", "correo": "ana@mail.com", "contrasena": "clave123" }
```

```
→ 201 { "token": "eyJhbG...", "correo": "ana@mail.com",
        "nombre": "Ana", "rol": "TURISTA" }
```

La contraseña queda en MySQL como algo tipo
`$2a$10$X7q...` (nunca `clave123`).

### 6.3 Login

```http
POST /api/auth/login
Content-Type: application/json

{ "correo": "admin@expora.com", "contrasena": "admin123" }
```

```
→ 200 { "token": "eyJhbG...", "correo": "admin@expora.com",
        "nombre": "Administrador", "rol": "ADMIN" }
→ credenciales malas: 401 { "error": "Credenciales inválidas..." }
```

### 6.4 Consumir una ruta protegida

```http
GET /api/usuarios
Authorization: Bearer eyJhbG...
```

```
1. JwtAuthenticationFilter extrae el correo del token.
2. CustomUserDetailsService reconstruye principal + ROLE_ADMIN.
3. SecurityConfig ve hasRole("ADMIN") → permite.
4. Sin token → 401. Con token TURISTA → 403 + JSON de error.
```

---

## 7. Cómo probar rápidamente

1. Crea la BD si no existe: `CREATE DATABASE explora_mocoa;`
   (el nombre viene de `spring.datasource.url`).
2. Arranca: `./mvnw spring-boot:run` (o desde tu IDE).
   - Mira el log: `>>> Usuario ADMIN creado: admin@expora.com`
     (o `... ya existe ...` en reinicios).
3. Login admin:
   `POST http://localhost:8080/api/auth/login`
   body `{ "correo": "admin@expora.com", "contrasena": "admin123" }`.
4. Copia el `token` y úsalo:
   `GET http://localhost:8080/api/usuarios`
   con header `Authorization: Bearer <token>` → **200**.
5. Sin header → **401/403**. Con token de turista en `/api/usuarios` → **403**.
6. Registro: `POST /api/auth/register` como en §6.2 → **201** + token.
7. Verifica el hash en MySQL:
   `SELECT correo, contrasena FROM usuarios;` → verás `$2a$...`, jamás texto plano.

---

## 8. Decisiones de diseño y advertencias

1. **Un solo rol por usuario** (`ManyToOne` en `Usuario`): coincide con tu modelo
   actual. Si algún día necesitas múltiples roles, habría que migrar a
   `ManyToMany` y ajustar `CustomUserDetailsService` para mapear la lista.
2. **Registro nunca crea ADMIN**: es intencional. Los admin se crean vía seed
   o por otro admin (`POST /api/usuarios` con token ADMIN, que también
   encripta gracias a `UsuarioService`).
3. **Lectura pública del catálogo**: `GET` de lugares/eventos/comidas/etc. es
   abierto para que turistas sin cuenta exploren. Si prefieres cerrarlo todo,
   cambia esos `permitAll()` por `authenticated()` en `SecurityConfig`.
4. **`update` de usuario sin contraseña**: si `contrasena` viene vacía, se
   conserva el hash anterior. Correcto para "editar perfil", pero el frontend
   **no debe** enviar el hash de vuelta (ya no lo recibe gracias a WRITE_ONLY).
5. **Secreto en properties**: para un trabajo académico está bien; en producción
   usa variables de entorno (`JWT_SECRET`) o un vault, HTTPS obligatorio,
   expiración más corta y, si quieres, *refresh tokens*.
6. **Los tokens no se revocan** (stateless): un token robado vale hasta expirar.
   Mitigaciones: expiración corta, no guardarlos en `localStorage` si hay riesgo
   XSS, y a futuro una *blocklist* o refresh rotation.

---

## 9. Lista de archivos (para ubicarte)

**Creados (8):**

- `src/main/java/com/expora_mocoa/security/JwtService.java`
- `src/main/java/com/expora_mocoa/security/CustomUserDetailsService.java`
- `src/main/java/com/expora_mocoa/security/JwtAuthenticationFilter.java`
- `src/main/java/com/expora_mocoa/security/SecurityConfig.java`
- `src/main/java/com/expora_mocoa/security/AuthController.java`
- `src/main/java/com/expora_mocoa/security/dto/LoginRequest.java`
- `src/main/java/com/expora_mocoa/security/dto/RegisterRequest.java`
- `src/main/java/com/expora_mocoa/security/dto/AuthResponse.java`
- `src/main/java/com/expora_mocoa/config/DataInitializer.java`
- `SEGURIDAD.md` (este archivo)

**Modificados (5):**

- `pom.xml` (dependencias Security + JJWT)
- `src/main/java/com/expora_mocoa/services/UsuarioService.java` (BCrypt)
- `src/main/java/com/expora_mocoa/entities/Usuario.java` (`WRITE_ONLY`)
- `src/main/java/com/expora_mocoa/controllers/GlobalExceptionHandler.java` (401/403)
- `src/main/resources/application.properties` (`jwt.*`, `admin.*`)
