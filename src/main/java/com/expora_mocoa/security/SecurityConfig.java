package com.expora_mocoa.security;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Configuración principal de Spring Security (el "corazón" de la seguridad).
 *
 * <p>Aquí se define:</p>
 * <ul>
 *   <li>Cómo se encriptan las contraseñas (BCrypt).</li>
 *   <li>El AuthenticationManager que usa el login.</li>
 *   <li>Qué rutas son públicas y qué rol necesita cada una.</li>
 *   <li>Qué responder cuando no hay token (401) o no hay permiso (403).</li>
 * </ul>
 */
@Configuration          // Clase de configuración de Spring
@EnableWebSecurity      // Activa Spring Security en la aplicación web
@EnableMethodSecurity   // Permite usar @PreAuthorize en métodos si se necesita
public class SecurityConfig {

    // Filtro que lee y valida el JWT en cada petición
    private final JwtAuthenticationFilter jwtAuthFilter;

    // Spring inyecta el filtro automáticamente por constructor
    public SecurityConfig(JwtAuthenticationFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    /**
     * Encriptador de contraseñas: BCrypt.
     *
     * <p>BCrypt es un hash con "salt" aleatoria y es intencionalmente lento.
     * Es irreversible: nunca se puede "desencriptar". Para verificar una clave,
     * se hashea lo que escribe el usuario y se compara con el hash guardado.</p>
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * AuthenticationManager: lo usa AuthController en el login para comprobar
     * que el correo exista y que la contraseña coincida con el hash BCrypt.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * Cadena de filtros de seguridad: reglas globales y matriz de acceso.
     *
     * <p>OJO: {@code hasRole("ADMIN")} equivale a {@code hasAuthority("ROLE_ADMIN")}.
     * En la BD el rol se guarda como "ADMIN"; Spring le agrega el prefijo "ROLE_".</p>
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 1. CSRF desactivado: en una API REST con JWT no hay cookies de sesión que proteger
                .csrf(AbstractHttpConfigurer::disable)

                // 2. STATELESS: el servidor no guarda sesiones, cada petición trae su propio token
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 3. Manejo de errores de seguridad
                .exceptionHandling(ex -> ex
                        // 401: no hay token, o el token es inválido o expiró ("¿quién eres?")
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType("application/json");
                            response.setCharacterEncoding("UTF-8");
                            response.getWriter().write(
                                    "{\"error\": \"No autenticado: falta el token o es inválido\"}");
                        })
                        // 403: el token es válido pero el rol no tiene permiso ("sé quién eres, pero no puedes")
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            response.setContentType("application/json");
                            response.setCharacterEncoding("UTF-8");
                            response.getWriter().write(
                                    "{\"error\": \"Acceso denegado: no tienes el rol necesario\"}");
                        }))

                // 4. Matriz de autorización: qué ruta exige qué rol (se evalúa de arriba hacia abajo)
                .authorizeHttpRequests(auth -> auth

                        // Login y registro: públicos (todavía no se tiene token)
                        .requestMatchers("/api/auth/**").permitAll()

                        // Página interna de errores de Spring: pública para ver el error real
                        .requestMatchers("/error").permitAll()

                        // Lectura del catálogo turístico: pública (visitantes sin cuenta)
                        .requestMatchers(HttpMethod.GET,
                                "/api/lugares/**",
                                "/api/eventos/**",
                                "/api/comidas/**",
                                "/api/categorias/**",
                                "/api/servicios/**",
                                "/api/resenas/**").permitAll()

                        // Crear contenido del catálogo: solo PROPIETARIO o ADMIN
                        .requestMatchers(HttpMethod.POST,
                                "/api/lugares/**",
                                "/api/eventos/**",
                                "/api/comidas/**",
                                "/api/categorias/**",
                                "/api/servicios/**").hasAnyRole("PROPIETARIO", "ADMIN")

                        // Editar contenido del catálogo: solo PROPIETARIO o ADMIN
                        .requestMatchers(HttpMethod.PUT,
                                "/api/lugares/**",
                                "/api/eventos/**",
                                "/api/comidas/**",
                                "/api/categorias/**",
                                "/api/servicios/**").hasAnyRole("PROPIETARIO", "ADMIN")

                        // Eliminar contenido del catálogo: solo PROPIETARIO o ADMIN
                        .requestMatchers(HttpMethod.DELETE,
                                "/api/lugares/**",
                                "/api/eventos/**",
                                "/api/comidas/**",
                                "/api/categorias/**",
                                "/api/servicios/**").hasAnyRole("PROPIETARIO", "ADMIN")

                        // Escribir reseñas: cualquier usuario autenticado (el GET ya es público arriba)
                        .requestMatchers("/api/resenas/**").authenticated()

                        // Órdenes y carrito: cualquier usuario autenticado
                        .requestMatchers("/api/ordenes/**", "/api/items-orden/**").authenticated()

                        // Gestión de usuarios y roles: solo ADMIN
                        .requestMatchers("/api/usuarios/**", "/api/roles/**").hasRole("ADMIN")

                        // Cualquier otra ruta: exige estar autenticado
                        .anyRequest().authenticated())

                // 5. Nuestro filtro JWT se ejecuta ANTES del filtro estándar de usuario/contraseña
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
}
}
