package sv.edu.ues.occ.ing.ppi115_2026.pos.cafefe.entity;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "producto", schema = "public")
@NamedQueries({
    @NamedQuery(name = "Producto.findAll", query = "SELECT p FROM Producto p"),
    @NamedQuery(name = "Producto.findByNombre", query = "SELECT p FROM Producto p WHERE p.nombre = :nombre"),
    @NamedQuery(name = "Producto.findByActivo", query = "SELECT p FROM Producto p WHERE p.activo = :activo"),
    @NamedQuery(name = "Producto.findByComentarios", query = "SELECT p FROM Producto p WHERE p.comentarios = :comentarios")})
public class Producto implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Basic(optional = false)
    @NotNull
    @Column(name = "id_producto")
    private UUID idProducto;

    @Size(max = 155)
    @Column(name = "nombre")
    private String nombre;

 @NotNull
@Column(name = "precio_sugerido", nullable = false, precision = 8, scale = 2)
private BigDecimal precioSugerido;

    @Column(name = "activo")
    private Boolean activo;

    @Size(max = 2147483647)
    @Column(name = "comentarios")
    private String comentarios;

    @OneToMany(mappedBy = "idProducto", fetch = FetchType.LAZY)
    private List<ProductoCaracteristica> productoCaracteristicaList;

    @OneToMany(mappedBy = "idProducto", fetch = FetchType.LAZY)
    private List<OrdenProducto> ordenProductoList;

    @OneToMany(mappedBy = "idProducto", fetch = FetchType.LAZY)
    private List<DescuentoProducto> descuentoProductoList;

  @OneToMany(mappedBy = "idProducto")
private List<ProductoTipoProducto> productoTipoProductoList;

    public Producto() {
    }

    public Producto(UUID idProducto) {
        this.idProducto = idProducto;
    }

    public UUID getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(UUID idProducto) {
        this.idProducto = idProducto;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

   public BigDecimal getPrecioSugerido() {
    return precioSugerido;
}

public void setPrecioSugerido(BigDecimal precioSugerido) {
    this.precioSugerido = precioSugerido;
}

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public String getComentarios() {
        return comentarios;
    }

    public void setComentarios(String comentarios) {
        this.comentarios = comentarios;
    }

    public List<ProductoCaracteristica> getProductoCaracteristicaList() {
        return productoCaracteristicaList;
    }

    public void setProductoCaracteristicaList(List<ProductoCaracteristica> productoCaracteristicaList) {
        this.productoCaracteristicaList = productoCaracteristicaList;
    }

    public List<OrdenProducto> getOrdenProductoList() {
        return ordenProductoList;
    }

    public void setOrdenProductoList(List<OrdenProducto> ordenProductoList) {
        this.ordenProductoList = ordenProductoList;
    }

    public List<DescuentoProducto> getDescuentoProductoList() {
        return descuentoProductoList;
    }

    public void setDescuentoProductoList(List<DescuentoProducto> descuentoProductoList) {
        this.descuentoProductoList = descuentoProductoList;
    }

    public List<ProductoTipoProducto> getProductoTipoProductoList() {
        return productoTipoProductoList;
    }

    public void setProductoTipoProductoList(List<ProductoTipoProducto> productoTipoProductoList) {
        this.productoTipoProductoList = productoTipoProductoList;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (idProducto != null ? idProducto.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Producto)) {
            return false;
        }
        Producto other = (Producto) object;
        if ((this.idProducto == null && other.idProducto != null) || (this.idProducto != null && !this.idProducto.equals(other.idProducto))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "sv.edu.ues.occ.ing.ppi115_2026.pos.cafefe.entity.Producto[ idProducto=" + idProducto + " ]";
    }
}
