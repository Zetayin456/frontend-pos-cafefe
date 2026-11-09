package sv.edu.ues.occ.ing.ppi115_2026.pos.cafefe.control;

import java.util.List;
import java.util.UUID;
import sv.edu.ues.occ.ing.ppi115_2026.pos.cafefe.entity.ProductoCaracteristica;

/** Domain repository contract for ProductoCaracteristica. */
public interface ProductoCaracteristicaRepository extends Repository<ProductoCaracteristica> {
    List<ProductoCaracteristica> findAllWithRelations();
    List<ProductoCaracteristica> buscarPorIdProducto(UUID idProducto);
}
