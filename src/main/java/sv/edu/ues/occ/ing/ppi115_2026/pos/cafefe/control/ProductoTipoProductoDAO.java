package sv.edu.ues.occ.ing.ppi115_2026.pos.cafefe.control;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import sv.edu.ues.occ.ing.ppi115_2026.pos.cafefe.entity.ProductoTipoProducto;

@Stateless
public class ProductoTipoProductoDAO extends DefaultDAO<ProductoTipoProducto> implements ProductoTipoProductoDAOInterface, Serializable {

    private static final long serialVersionUID = 1L;

    @PersistenceContext(unitName = "cafefePU")
    private EntityManager em;

    public ProductoTipoProductoDAO() {
        super(ProductoTipoProducto.class);
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    @Override
    public List<ProductoTipoProducto> findAllWithRelations() {
        return em.createQuery("SELECT DISTINCT ptp FROM ProductoTipoProducto ptp "
                + "JOIN FETCH ptp.idProducto JOIN FETCH ptp.idTipoProducto "
                + "ORDER BY ptp.fechaCreacion DESC", ProductoTipoProducto.class).getResultList();
    }

    @Override
    public List<ProductoTipoProducto> findByProducto(UUID idProducto) {
        if (idProducto == null) return Collections.emptyList();
        return em.createQuery("SELECT DISTINCT ptp FROM ProductoTipoProducto ptp "
                + "JOIN FETCH ptp.idProducto JOIN FETCH ptp.idTipoProducto "
                + "WHERE ptp.idProducto.idProducto = :idProducto", ProductoTipoProducto.class)
                .setParameter("idProducto", idProducto).getResultList();
    }

    @Override
    public List<ProductoTipoProducto> buscarPorIdProducto(UUID idProducto) {
        return findByProducto(idProducto);
    }
}
