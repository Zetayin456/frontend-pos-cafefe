package sv.edu.ues.occ.ing.ppi115_2026.pos.cafefe.control;

import java.util.List;
import java.util.UUID;
import java.util.Date;
import sv.edu.ues.occ.ing.ppi115_2026.pos.cafefe.entity.DescuentoProducto;

/** Domain repository contract for DescuentoProducto. */
public interface DescuentoProductoRepository extends Repository<DescuentoProducto> {
    List<DescuentoProducto> findAllWithRelations();
    List<DescuentoProducto> findByProductoWithRelations(UUID idProducto);
    List<DescuentoProducto> buscarPorIdProducto(UUID idProducto);
    List<DescuentoProducto> buscarVigentesPorProducto(UUID idProducto, Date fecha);
}
