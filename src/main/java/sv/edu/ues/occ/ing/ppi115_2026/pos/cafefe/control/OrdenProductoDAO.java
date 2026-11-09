package sv.edu.ues.occ.ing.ppi115_2026.pos.cafefe.control;

import java.util.List;
import java.util.UUID;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import sv.edu.ues.occ.ing.ppi115_2026.pos.cafefe.entity.OrdenProducto;

@Stateless
public class OrdenProductoDAO extends DefaultDAO<OrdenProducto> implements OrdenProductoDAOInterface {

    @PersistenceContext(unitName = "cafefePU")
    private EntityManager em;

    public OrdenProductoDAO() { super(OrdenProducto.class); }

    @Override
    protected EntityManager getEntityManager() { return em; }

    @Override
    public void crear(OrdenProducto entity) {
        if (entity != null) {
            create(entity);
        }
    }

    @Override
    public void modificar(OrdenProducto entity) {
        if (entity != null) {
            edit(entity);
        }
    }

    @Override
    public void eliminar(OrdenProducto entity) {
        if (entity == null || entity.getIdOrdenProducto() == null) {
            throw new IllegalArgumentException("El registro a eliminar no es válido.");
        }
        OrdenProducto gestionado = find(entity.getIdOrdenProducto());
        if (gestionado != null) {
            em.remove(gestionado);
        }
    }

    @Override
    public OrdenProducto findById(Object id) {
        if (id == null) {
            return null;
        }
        UUID parsedId = parseId(id);
        return parsedId != null ? find(parsedId) : null;
    }

    @Override
    public OrdenProducto buscarPorId(Object id) {
        if (id == null) {
            return null;
        }
        UUID parsedId = parseId(id);
        return parsedId != null ? find(parsedId) : null;
    }

    private UUID parseId(Object id) {
        if (id instanceof UUID uuid) return uuid;
        try { return UUID.fromString(id.toString()); }
        catch (IllegalArgumentException e) { return null; }
    }

    @Override
    public List<OrdenProducto> findAll() {
        return em.createNamedQuery("OrdenProducto.findAll", OrdenProducto.class).getResultList();
    }

    @Override
    public List<OrdenProducto> findRange(int first, int pageSize) {
        return em.createQuery("SELECT op FROM OrdenProducto op ORDER BY op.idOrdenProducto", OrdenProducto.class)
                 .setFirstResult(first)
                 .setMaxResults(pageSize)
                 .getResultList();
    }

    @Override
    public int count() {
        return em.createQuery("SELECT COUNT(op) FROM OrdenProducto op", Long.class)
                 .getSingleResult()
                 .intValue();
    }

    @Override
    public List<OrdenProducto> buscarPorIdOrden(UUID idOrden) {
        if (idOrden == null) return List.of();
        return em.createQuery("SELECT op FROM OrdenProducto op "
                + "JOIN FETCH op.idProducto WHERE op.idOrden.idOrden = :idOrden", OrdenProducto.class)
                .setParameter("idOrden", idOrden)
                .getResultList();
    }
}
