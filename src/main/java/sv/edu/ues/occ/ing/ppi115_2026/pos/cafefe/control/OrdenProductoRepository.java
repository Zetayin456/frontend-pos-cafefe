package sv.edu.ues.occ.ing.ppi115_2026.pos.cafefe.control;

import java.util.List;
import java.util.UUID;
import sv.edu.ues.occ.ing.ppi115_2026.pos.cafefe.entity.OrdenProducto;

/** Domain repository contract for OrdenProducto. */
public interface OrdenProductoRepository extends Repository<OrdenProducto> {
    void crear(OrdenProducto entity);
    void modificar(OrdenProducto entity);
    void eliminar(OrdenProducto entity);
    OrdenProducto findById(Object id);
    OrdenProducto buscarPorId(Object id);
    List<OrdenProducto> findAll();
    List<OrdenProducto> buscarPorIdOrden(UUID idOrden);
}
