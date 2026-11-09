package sv.edu.ues.occ.ing.ppi115_2026.pos.cafefe.service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import sv.edu.ues.occ.ing.ppi115_2026.pos.cafefe.control.EmpleadoRolDAOInterface;
import sv.edu.ues.occ.ing.ppi115_2026.pos.cafefe.control.OrdenDAOInterface;
import sv.edu.ues.occ.ing.ppi115_2026.pos.cafefe.control.OrdenProductoDAOInterface;
import sv.edu.ues.occ.ing.ppi115_2026.pos.cafefe.control.ProductoDAOInterface;
import sv.edu.ues.occ.ing.ppi115_2026.pos.cafefe.entity.EmpleadoRol;
import sv.edu.ues.occ.ing.ppi115_2026.pos.cafefe.entity.Orden;
import sv.edu.ues.occ.ing.ppi115_2026.pos.cafefe.entity.OrdenProducto;
import sv.edu.ues.occ.ing.ppi115_2026.pos.cafefe.entity.Producto;
import sv.edu.ues.occ.ing.ppi115_2026.pos.cafefe.entity.Rol;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OrdenServiceTest {

    @Mock private OrdenDAOInterface ordenDAO;
    @Mock private OrdenProductoDAOInterface ordenProductoDAO;
    @Mock private EmpleadoRolDAOInterface empleadoRolDAO;
    @Mock private ProductoDAOInterface productoDAO;
    @InjectMocks private OrdenService service;

    @BeforeEach
    void setUp() { MockitoAnnotations.openMocks(this); }

    @Test
    void guardarCreaOrdenYAsociaDetalles() {
        Orden orden = ordenValida();
        OrdenProducto detalle = detalleValido();
        conectarRelaciones(orden, detalle);

        service.guardar(orden, List.of(detalle));

        assertNotNull(orden.getIdOrden());
        assertNotNull(detalle.getIdOrdenProducto());
        assertSame(orden, detalle.getIdOrden());
        assertEquals(List.of(detalle), orden.getOrdenProductoList());
        verify(ordenDAO).crear(orden);
        verify(ordenProductoDAO).crear(detalle);
        verify(ordenDAO, never()).modificar(any());
    }

    @Test
    void guardarRechazaProductoInactivoSinPersistir() {
        Orden orden = ordenValida();
        OrdenProducto detalle = detalleValido();
        detalle.getIdProducto().setActivo(false);
        conectarRelaciones(orden, detalle);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.guardar(orden, List.of(detalle)));

        assertTrue(ex.getMessage().contains("activos"));
        verifyNoInteractions(ordenDAO);
    }

    @Test
    void listarCamarerosSoloIncluyeAsignacionesActivasConRolCamarero() {
        EmpleadoRol camarero = asignacion("Camarero", true);
        EmpleadoRol inactivo = asignacion("Camarero", false);
        EmpleadoRol cocinero = asignacion("Cocinero", true);
        when(empleadoRolDAO.findAllWithRelations()).thenReturn(List.of(camarero, inactivo, cocinero));

        assertEquals(List.of(camarero), service.listarCamarerosDisponibles());
        verify(empleadoRolDAO).findAllWithRelations();
    }

    private Orden ordenValida() {
        Orden orden = new Orden();
        orden.setFechaCreacion(new Date());
        orden.setIdEmpleadoRol(asignacion("Camarero", true));
        return orden;
    }

    private OrdenProducto detalleValido() {
        OrdenProducto detalle = new OrdenProducto();
        Producto producto = new Producto(UUID.randomUUID());
        producto.setActivo(true);
        producto.setPrecioSugerido(new BigDecimal("3.25"));
        detalle.setIdProducto(producto);
        detalle.setPrecio(new BigDecimal("3.25"));
        return detalle;
    }

    private void conectarRelaciones(Orden orden, OrdenProducto detalle) {
        when(empleadoRolDAO.find(orden.getIdEmpleadoRol().getIdEmpleadoRol()))
                .thenReturn(orden.getIdEmpleadoRol());
        when(productoDAO.find(detalle.getIdProducto().getIdProducto()))
                .thenReturn(detalle.getIdProducto());
    }

    private EmpleadoRol asignacion(String nombreRol, boolean activa) {
        Rol rol = new Rol();
        rol.setNombre(nombreRol);
        rol.setActivo(true);
        EmpleadoRol asignacion = new EmpleadoRol();
        asignacion.setIdEmpleadoRol(UUID.randomUUID());
        asignacion.setIdRol(rol);
        asignacion.setActivo(activa);
        return asignacion;
    }
}
