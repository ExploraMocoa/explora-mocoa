package com.expora_mocoa.security;

import com.expora_mocoa.entities.Rol;
import com.expora_mocoa.entities.Usuario;
import com.expora_mocoa.repositories.RolRepository;
import com.expora_mocoa.repositories.UsuarioRepository;
import com.expora_mocoa.security.dto.AuthResponse;
import com.expora_mocoa.security.dto.LoginRequest;
import com.expora_mocoa.security.dto.RegisterRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

/**
 * Endpoints públicos de autenticación.
 *
 * <ul>
 *   <li>{@code POST /api/auth/login} — valida credenciales y devuelve JWT.</li>
 *   <li>{@code POST /api/auth/register} — crea un usuario TURISTA (o PROPIETARIO
 *       si lo pide) con contraseña encriptada y devuelve JWT.</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;
    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthController(AuthenticationManager authenticationManager,
            CustomUserDetailsService userDetailsService,
            UsuarioRepository usuarioRepository,
            RolRepository rolRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        String correo = request.getCorreo().trim();

        // 1. Delegar la verificación al AuthenticationManager:
        //    compara el hash BCrypt guardado con la contraseña enviada.
        //    Si falla, lanza BadCredentialsException -> 401 (ver GlobalExceptionHandler).
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(correo, request.getContrasena()));

        // 2. Generar el JWT con los datos del principal autenticado.
        UserDetails userDetails = (UserDetails) auth.getPrincipal();
        String token = jwtService.generateToken(userDetails);

        Usuario usuario = usuarioRepository.findByCorreo(correo).orElseThrow();
        String rol = usuario.getRol() != null ? usuario.getRol().getNombre() : "TURISTA";

        return ResponseEntity.ok(new AuthResponse(token, usuario.getCorreo(), usuario.getNombre(), rol));
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        String correo = request.getCorreo().trim();

        if (usuarioRepository.existsByCorreo(correo)) {
            throw new IllegalArgumentException("El correo ya está registrado");
        }

        // Solo se permite auto-registro como TURISTA o PROPIETARIO.
        // ADMIN únicamente vía DataInitializer o un admin existente.
        String rolPedido = request.getRol() != null ? request.getRol().trim().toUpperCase() : "TURISTA";
        if (!rolPedido.equals("TURISTA") && !rolPedido.equals("PROPIETARIO")) {
            throw new IllegalArgumentException("Solo puedes registrarte como TURISTA o PROPIETARIO");
        }

        Rol rol = rolRepository.findByNombre(rolPedido)
                .orElseThrow(() -> new IllegalStateException("Rol no configurado: " + rolPedido));

        Usuario usuario = new Usuario();
        usuario.setNombre(request.getNombre().trim());
        usuario.setCorreo(correo);
        // ¡Encriptado BCrypt! Nunca se guarda en texto plano.
        usuario.setContrasena(passwordEncoder.encode(request.getContrasena()));
        usuario.setRol(rol);
        usuario = usuarioRepository.save(usuario);

        UserDetails userDetails = userDetailsService.loadUserByUsername(usuario.getCorreo());
        String token = jwtService.generateToken(userDetails);

        return new ResponseEntity<>(
                new AuthResponse(token, usuario.getCorreo(), usuario.getNombre(), rol.getNombre()),
                HttpStatus.CREATED);
    }
}
