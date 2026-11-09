package sv.edu.ues.occ.ing.ppi115_2026.pos.cafefe.control;

import java.util.List;
import java.util.UUID;
import sv.edu.ues.occ.ing.ppi115_2026.pos.cafefe.entity.ProductoTipoProducto;

/** Domain repository contract for ProductoTipoProducto. */
public interface ProductoTipoProductoRepository extends Repository<ProductoTipoProducto> {
    List<ProductoTipoProducto> findAllWithRelations();
    List<ProductoTipoProducto> findByProducto(UUID idProducto);
    List<ProductoTipoProducto> buscarPorIdProducto(UUID idProducto);
}
