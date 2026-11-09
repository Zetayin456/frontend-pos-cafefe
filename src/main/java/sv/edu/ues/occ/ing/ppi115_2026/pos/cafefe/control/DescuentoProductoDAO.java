package sv.edu.ues.occ.ing.ppi115_2026.pos.cafefe.control;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.io.Serializable;
import java.util.List;
import java.util.Date;
import java.util.UUID;
import sv.edu.ues.occ.ing.ppi115_2026.pos.cafefe.entity.DescuentoProducto;

@Stateless
public class DescuentoProductoDAO extends DefaultDAO<DescuentoProducto> implements DescuentoProductoDAOInterface, Serializable {

    private static final long serialVersionUID = 1L;

    @PersistenceContext(unitName = "cafefePU")
    private EntityManager em;

    public DescuentoProductoDAO() {
        super(DescuentoProducto.class);
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    @Override
    public List<DescuentoProducto> findAllWithRelations() {
        return em.createQuery("SELECT DISTINCT dp FROM DescuentoProducto dp "
                + "LEFT JOIN FETCH dp.idProducto "
                + "LEFT JOIN FETCH dp.idDescuento d "
                + "LEFT JOIN FETCH d.idTipoDescuento ORDER BY dp.fechaDesde DESC", DescuentoProducto.class)
                .getResultList();
    }

    @Override
    public List<DescuentoProducto> findByProductoWithRelations(UUID idProducto) {
        if (idProducto == null) {
            return List.of();
        }
        return em.createQuery("SELECT DISTINCT dp FROM DescuentoProducto dp "
                + "JOIN FETCH dp.idProducto "
                + "JOIN FETCH dp.idDescuento d "
                + "JOIN FETCH d.idTipoDescuento "
                + "WHERE dp.idProducto.idProducto = :idProducto "
                + "ORDER BY dp.fechaDesde DESC", DescuentoProducto.class)
                .setParameter("idProducto", idProducto)
                .getResultList();
    }

    @Override
    public List<DescuentoProducto> buscarPorIdProducto(UUID idProducto) {
        return findByProductoWithRelations(idProducto);
    }

    @Override
    public List<DescuentoProducto> buscarVigentesPorProducto(UUID idProducto, Date fecha) {
        if (idProducto == null || fecha == null) {
            return List.of();
        }
        return em.createQuery("SELECT DISTINCT dp FROM DescuentoProducto dp "
                + "JOIN FETCH dp.idProducto p "
                + "JOIN FETCH dp.idDescuento d "
                + "JOIN FETCH d.idTipoDescuento t "
                + "WHERE p.idProducto = :idProducto AND t.activo = TRUE "
                + "AND (dp.fechaDesde IS NULL OR dp.fechaDesde <= :fecha) "
                + "AND (dp.fechaHasta IS NULL OR dp.fechaHasta >= :fecha) "
                + "AND (d.fechaDesde IS NULL OR d.fechaDesde <= :fecha) "
                + "AND (d.fechaHasta IS NULL OR d.fechaHasta >= :fecha) "
                + "AND (t.descuentoMaximo IS NULL OR dp.valor <= t.descuentoMaximo) "
                + "ORDER BY dp.fechaDesde DESC", DescuentoProducto.class)
                .setParameter("idProducto", idProducto)
                .setParameter("fecha", fecha)
                .getResultList();
    }
}
