package com.expora_mocoa.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filtro que se ejecuta UNA vez por cada petición HTTP.
 *
 * <p>Flujo:</p>
 * <ol>
 *   <li>Lee el header {@code Authorization: Bearer &lt;token&gt;}.</li>
 *   <li>Si no hay token, deja pasar la cadena (los endpoints públicos
 *       lo permiten; los protegidos responderán 401/403).</li>
 *   <li>Si hay token válido, carga el usuario y lo coloca en el
 *       {@code SecurityContext}, quedando "autenticado" para el resto
 *       del request.</li>
 * </ol>
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtService jwtService,
                                   CustomUserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        // Sin header o sin prefijo Bearer -> no es un request autenticado, seguir.
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7); // quita "Bearer "
        try {
            String correo = jwtService.extractUsername(token);

            // Solo autentica si aún nadie lo hizo en este request
            if (correo != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(correo);

                if (jwtService.isTokenValid(token, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails, null, userDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
       } catch (Exception ex) {
    System.err.println(">>> ERROR EN EL FILTRO JWT <<<");
    ex.printStackTrace(); // Esto mostrará el fallo exacto en tu consola
    SecurityContextHolder.clearContext();
}

        filterChain.doFilter(request, response);
    }
}
