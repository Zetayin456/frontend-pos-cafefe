
package sv.edu.ues.occ.ing.ppi115_2026.pos.cafefe.entity;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "tipo_producto", schema = "public")
@NamedQueries({
    @NamedQuery(
        name = "TipoProducto.findAll",
        query = "SELECT t FROM TipoProducto t"
    ),
    @NamedQuery(
        name = "TipoProducto.findByNombre",
        query = "SELECT t FROM TipoProducto t WHERE t.nombre = :nombre"
    ),
    @NamedQuery(
        name = "TipoProducto.findByActivo",
        query = "SELECT t FROM TipoProducto t WHERE t.activo = :activo"
    ),
    @NamedQuery(
        name = "TipoProducto.findByObservaciones",
        query = "SELECT t FROM TipoProducto t WHERE t.observaciones = :observaciones"
    )
})
public class TipoProducto implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Basic(optional = false)
    @NotNull
    @Column(name = "id_tipo_producto")
    private UUID idTipoProducto;

    @Size(max = 155)
    @Column(name = "nombre")
    private String nombre;

    @Size(max = 2147483647)
    @Column(name = "observaciones")
    private String observaciones;

    @Column(name = "activo")
    private Boolean activo;

    @OneToMany(mappedBy = "idTipoProducto")
    private List<ProductoTipoProducto> productoTipoProductoList;

    public TipoProducto() {
    }

    public TipoProducto(UUID idTipoProducto) {
        this.idTipoProducto = idTipoProducto;
    }

    public UUID getIdTipoProducto() {
        return idTipoProducto;
    }

    public void setIdTipoProducto(UUID idTipoProducto) {
        this.idTipoProducto = idTipoProducto;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public List<ProductoTipoProducto> getProductoTipoProductoList() {
        return productoTipoProductoList;
    }

    public void setProductoTipoProductoList(
            List<ProductoTipoProducto> productoTipoProductoList) {
        this.productoTipoProductoList = productoTipoProductoList;
    }

    @Override
    public int hashCode() {
        return idTipoProducto != null ? idTipoProducto.hashCode() : 0;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }

        if (!(object instanceof TipoProducto)) {
            return false;
        }

        TipoProducto other = (TipoProducto) object;

        return idTipoProducto != null
                && idTipoProducto.equals(other.idTipoProducto);
    }

    @Override
    public String toString() {
        return "TipoProducto[idTipoProducto=" + idTipoProducto + "]";
    }
}
