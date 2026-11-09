package sv.edu.ues.occ.ing.ppi115_2026.pos.cafefe.control;

import java.util.List;
import java.util.UUID;
import jakarta.ejb.Local;
import sv.edu.ues.occ.ing.ppi115_2026.pos.cafefe.entity.OrdenProducto;

@Local
public interface OrdenProductoDAOInterface extends OrdenProductoRepository {
    void crear(OrdenProducto entity);
    void modificar(OrdenProducto entity);
    void eliminar(OrdenProducto entity);
    OrdenProducto findById(Object id);
    OrdenProducto buscarPorId(Object id); // Mantenido para retrocompatibilidad con las pruebas
    List<OrdenProducto> findAll();
    List<OrdenProducto> findRange(int first, int pageSize);
    int count();
    List<OrdenProducto> buscarPorIdOrden(UUID idOrden);
}
