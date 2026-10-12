package com.expora_mocoa.security;

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
 * Configuración principal de Spring Security.
 *
 * <p>Ideas clave:</p>
 * <ul>
 *   <li>Sesión <b>STATELESS</b>: el servidor no guarda sesiones; cada request
 *       se autentica con el JWT del header {@code Authorization}.</li>
 *   <li>CSRF desactivado: es necesario para APIs REST con JWT (no hay cookies).</li>
 *   <li>El {@link JwtAuthenticationFilter} corre antes del filtro estándar de
 *       usuario/contraseña.</li>
 *   <li>{@code @EnableMethodSecurity} permite usar {@code @PreAuthorize} en
 *       controladores/servicios si quieres reglas más finas.</li>
 * </ul>
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    /**
     * BCrypt: algoritmo de hash con "salt" aleatoria y factor de costo.
     * Es irreversible y resistente a fuerza bruta / rainbow tables.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /** Expone el AuthenticationManager que usa AuthController para el login. */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * Define qué rutas son públicas y cuáles exigen cada rol.
     * OJO: {@code hasRole("ADMIN")} equivale a {@code hasAuthority("ROLE_ADMIN")}.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Auth siempre público
                        .requestMatchers("/api/auth/**").permitAll()

                        // Lectura pública del catálogo turístico (para visitantes sin cuenta)
                        .requestMatchers(HttpMethod.GET,
                                "/api/lugares/**",
                                "/api/eventos/**",
                                "/api/comidas/**",
                                "/api/categorias/**",
                                "/api/servicios/**",
                                "/api/resenas/**").permitAll()

                        // Escritura del catálogo: solo quienes publican contenido
                        .requestMatchers(HttpMethod.POST, "/api/lugares/**", "/api/eventos/**",
                                "/api/comidas/**", "/api/categorias/**", "/api/servicios/**")
                        .hasAnyRole("PROPIETARIO", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/lugares/**", "/api/eventos/**",
                                "/api/comidas/**", "/api/categorias/**", "/api/servicios/**")
                        .hasAnyRole("PROPIETARIO", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/lugares/**", "/api/eventos/**",
                                "/api/comidas/**", "/api/categorias/**", "/api/servicios/**")
                        .hasAnyRole("PROPIETARIO", "ADMIN")

                        // Reseñas y órdenes: cualquier usuario logueado
                        .requestMatchers("/api/resenas/**").authenticated()
                        .requestMatchers("/api/ordenes/**", "/api/items-orden/**")
                        .authenticated()

                        // Administración total: solo ADMIN
                        .requestMatchers("/api/usuarios/**", "/api/roles/**").hasRole("ADMIN")

                        // Todo lo demás exige estar autenticado
                        .anyRequest().authenticated())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
