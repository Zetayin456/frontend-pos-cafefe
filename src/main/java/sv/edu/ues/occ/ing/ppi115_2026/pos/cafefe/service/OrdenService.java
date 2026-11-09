package sv.edu.ues.occ.ing.ppi115_2026.pos.cafefe.service;

import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import sv.edu.ues.occ.ing.ppi115_2026.pos.cafefe.control.EmpleadoRolRepository;
import sv.edu.ues.occ.ing.ppi115_2026.pos.cafefe.control.OrdenRepository;
import sv.edu.ues.occ.ing.ppi115_2026.pos.cafefe.control.OrdenProductoRepository;
import sv.edu.ues.occ.ing.ppi115_2026.pos.cafefe.control.ProductoRepository;
import sv.edu.ues.occ.ing.ppi115_2026.pos.cafefe.entity.EmpleadoRol;
import sv.edu.ues.occ.ing.ppi115_2026.pos.cafefe.entity.Orden;
import sv.edu.ues.occ.ing.ppi115_2026.pos.cafefe.entity.OrdenProducto;
import sv.edu.ues.occ.ing.ppi115_2026.pos.cafefe.entity.Producto;

/** Reglas y operaciones de aplicación para el flujo de órdenes. */
@Stateless
public class OrdenService {

    @Inject
    private OrdenRepository ordenRepository;

    @Inject
    private OrdenProductoRepository ordenProductoRepository;

    @Inject
    private EmpleadoRolRepository empleadoRolRepository;

    @Inject
    private ProductoRepository productoRepository;

    public List<Orden> listar() {
        List<Orden> ordenes = ordenRepository.findAllWithDetails();
        Map<UUID, Producto> productosConDescuentos = new HashMap<>();
        for (Producto producto : productoRepository.findActivosConDescuentos()) {
            productosConDescuentos.put(producto.getIdProducto(), producto);
        }
        for (Orden orden : ordenes) {
            if (orden.getOrdenProductoList() == null) continue;
            for (OrdenProducto detalle : orden.getOrdenProductoList()) {
                if (detalle.getIdProducto() == null) continue;
                Producto producto = productosConDescuentos.get(detalle.getIdProducto().getIdProducto());
                if (producto != null) detalle.setIdProducto(producto);
            }
        }
        return ordenes;
    }

    public List<Producto> listarProductosDisponibles() {
        return productoRepository.findActivosConDescuentos();
    }

    public List<EmpleadoRol> listarCamarerosDisponibles() {
        List<EmpleadoRol> camareros = new ArrayList<>();
        for (EmpleadoRol asignacion : empleadoRolRepository.findAllWithRelations()) {
            if (asignacion != null && Boolean.TRUE.equals(asignacion.getActivo())
                    && asignacion.getIdRol() != null
                    && Boolean.TRUE.equals(asignacion.getIdRol().getActivo())
                    && "CAMARERO".equalsIgnoreCase(asignacion.getIdRol().getNombre())) {
                camareros.add(asignacion);
            }
        }
        return camareros;
    }

    public void guardar(Orden orden, List<OrdenProducto> productos) {
        if (orden == null) throw new IllegalArgumentException("La orden es obligatoria.");
        if (orden.getFechaCreacion() == null) throw new IllegalArgumentException("La fecha de la orden es obligatoria.");
        if (orden.getIdEmpleadoRol() == null || orden.getIdEmpleadoRol().getIdEmpleadoRol() == null) {
            throw new IllegalArgumentException("Selecciona una asignación activa del rol Camarero.");
        }
        EmpleadoRol asignacion = empleadoRolRepository.find(orden.getIdEmpleadoRol().getIdEmpleadoRol());
        if (asignacion == null || !Boolean.TRUE.equals(asignacion.getActivo())
                || asignacion.getIdRol() == null
                || !Boolean.TRUE.equals(asignacion.getIdRol().getActivo())
                || !"CAMARERO".equalsIgnoreCase(asignacion.getIdRol().getNombre())) {
            throw new IllegalArgumentException("Selecciona una asignación activa del rol Camarero.");
        }
        orden.setIdEmpleadoRol(asignacion);
        if (productos == null || productos.isEmpty()) throw new IllegalArgumentException("Agrega al menos un producto.");

        boolean nueva = orden.getIdOrden() == null;
        if (nueva) orden.setIdOrden(UUID.randomUUID());
        for (OrdenProducto detalle : productos) {
            if (detalle == null || detalle.getIdProducto() == null || detalle.getIdProducto().getIdProducto() == null) {
                throw new IllegalArgumentException("Todos los productos deben estar activos y seleccionados.");
            }
            Producto producto = productoRepository.find(detalle.getIdProducto().getIdProducto());
            if (producto == null || !Boolean.TRUE.equals(producto.getActivo())) {
                throw new IllegalArgumentException("Todos los productos deben estar activos y seleccionados.");
            }
            if (detalle.getPrecio() == null || detalle.getPrecio().signum() < 0) {
                throw new IllegalArgumentException("El precio de cada producto debe ser mayor o igual a cero.");
            }
            if (detalle.getIdOrdenProducto() == null) detalle.setIdOrdenProducto(UUID.randomUUID());
            detalle.setIdProducto(producto);
            detalle.setIdOrden(orden);
        }
        List<OrdenProducto> anteriores = nueva
                ? List.of() : ordenProductoRepository.buscarPorIdOrden(orden.getIdOrden());
        Map<UUID, OrdenProducto> anterioresPorId = new HashMap<>();
        for (OrdenProducto anterior : anteriores) {
            anterioresPorId.put(anterior.getIdOrdenProducto(), anterior);
        }
        List<OrdenProducto> persistidos = new ArrayList<>();
        for (OrdenProducto detalle : productos) {
            OrdenProducto existente = anterioresPorId.remove(detalle.getIdOrdenProducto());
            if (existente != null) {
                existente.setIdProducto(detalle.getIdProducto());
                existente.setPrecio(detalle.getPrecio());
                existente.setObservaciones(detalle.getObservaciones());
                persistidos.add(existente);
            } else {
                detalle.setIdOrdenProducto(UUID.randomUUID());
                persistidos.add(detalle);
            }
        }
        orden.setOrdenProductoList(persistidos);
        Orden ordenPersistida;
        if (nueva) {
            ordenRepository.crear(orden);
            ordenPersistida = orden;
        } else {
            ordenPersistida = ordenRepository.modificar(orden);
        }
        for (OrdenProducto detalle : persistidos) {
            detalle.setIdOrden(ordenPersistida);
            if (anterioresPorId.containsKey(detalle.getIdOrdenProducto())) continue;
            if (anteriores.contains(detalle)) ordenProductoRepository.modificar(detalle);
            else ordenProductoRepository.crear(detalle);
        }
        for (OrdenProducto removido : anterioresPorId.values()) {
            ordenProductoRepository.eliminar(removido);
        }
    }

    public void eliminar(Orden orden) {
        if (orden == null || orden.getIdOrden() == null) throw new IllegalArgumentException("Selecciona una orden válida.");
        for (OrdenProducto detalle : ordenProductoRepository.buscarPorIdOrden(orden.getIdOrden())) {
            ordenProductoRepository.eliminar(detalle);
        }
        ordenRepository.eliminar(orden);
    }
}
