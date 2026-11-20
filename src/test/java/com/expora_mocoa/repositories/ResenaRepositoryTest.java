package com.expora_mocoa.repositories;
 
import com.expora_mocoa.entities.Categoria;
import com.expora_mocoa.entities.LugarTuristico;
import com.expora_mocoa.entities.Resena;
import com.expora_mocoa.entities.Usuario;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
 
import java.time.LocalDateTime;
import java.util.List;
 
import static org.junit.jupiter.api.Assertions.*;
 
// Prueba unitaria de ResenaRepository (capa Repository, base H2 en memoria). Autora: Valery
@DataJpaTest
class ResenaRepositoryTest {
 
    @Autowired
    private ResenaRepository resenaRepository;
 
    @Autowired
    private LugarTuristicoRepository lugarRepository;
 
    @Autowired
    private CategoriaRepository categoriaRepository;
 
    @Autowired
    private UsuarioRepository usuarioRepository;
 
    // Prueba 4: findByLugarTuristicoId trae solo las reseñas de ese lugar
    @Test
    void findByLugarTuristicoId_traeLasResenasDelLugar() {
        Usuario usuario = new Usuario();
        usuario.setNombre("Valery");
        usuario.setCorreo("valery@test.com");
        usuario.setContrasena("$2a$10$hashFalso");
        usuarioRepository.save(usuario);
 
        Categoria categoria = categoriaRepository.save(new Categoria("Naturaleza", "Ríos"));
 
        LugarTuristico lugar = new LugarTuristico();
        lugar.setNombre("Fin del Mundo");
        lugar.setCategoria(categoria);
        lugar.setUsuario(usuario);
        lugarRepository.save(lugar);
 
        Resena resena = new Resena();
        resena.setCalificacion(5);
        resena.setComentario("Hermoso");
        resena.setFecha(LocalDateTime.now());
        resena.setUsuario(usuario);
        resena.setLugarTuristico(lugar);
        resenaRepository.save(resena);
 
        List<Resena> resenas = resenaRepository.findByLugarTuristicoId(lugar.getId());
 
        assertEquals(1, resenas.size());
        assertEquals("Hermoso", resenas.get(0).getComentario());
    }
}
