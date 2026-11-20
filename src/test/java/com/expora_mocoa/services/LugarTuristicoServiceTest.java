package com.expora_mocoa.services;
 
import com.expora_mocoa.entities.Categoria;
import com.expora_mocoa.entities.LugarTuristico;
import com.expora_mocoa.entities.Usuario;
import com.expora_mocoa.repositories.CategoriaRepository;
import com.expora_mocoa.repositories.LugarTuristicoRepository;
import com.expora_mocoa.repositories.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
 
import java.util.Optional;
 
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
 
// Prueba unitaria de LugarTuristicoService (capa Service). Autora: Valery
@ExtendWith(MockitoExtension.class)
class LugarTuristicoServiceTest {
 
    @Mock
    private LugarTuristicoRepository lugarRepository;
 
    @Mock
    private CategoriaRepository categoriaRepository;
 
    @Mock
    private UsuarioRepository usuarioRepository;
 
    @InjectMocks
    private LugarTuristicoService lugarService;
 
    // Prueba 2: crea el lugar cuando la categoría y el usuario existen
    @Test
    void crear_conCategoriaYUsuario_guardaElLugar() {
        Categoria categoria = new Categoria("Naturaleza", "Ríos");
        categoria.setId(1L);
        Usuario propietario = new Usuario();
        propietario.setId(2L);
 
        LugarTuristico lugar = new LugarTuristico();
        lugar.setNombre("Fin del Mundo");
        lugar.setCategoria(categoria);
        lugar.setUsuario(propietario);
 
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(propietario));
        when(lugarRepository.save(any(LugarTuristico.class))).thenAnswer(inv -> inv.getArgument(0));
 
        LugarTuristico guardado = lugarService.create(lugar);
 
        assertEquals("Fin del Mundo", guardado.getNombre());
        assertEquals("Naturaleza", guardado.getCategoria().getNombre());
        verify(lugarRepository).save(lugar);
    }
}
