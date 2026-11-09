package sv.edu.ues.occ.ing.ppi115_2026.pos.cafefe.control;

import jakarta.ejb.Local;
import java.util.List;
import java.util.UUID;
import java.util.Date;
import sv.edu.ues.occ.ing.ppi115_2026.pos.cafefe.entity.DescuentoProducto;

public interface DescuentoProductoDAOInterface extends DescuentoProductoRepository {
    List<DescuentoProducto> findAllWithRelations();
    List<DescuentoProducto> findByProductoWithRelations(UUID idProducto);
    List<DescuentoProducto> buscarPorIdProducto(UUID idProducto);
    List<DescuentoProducto> buscarVigentesPorProducto(UUID idProducto, Date fecha);
}
