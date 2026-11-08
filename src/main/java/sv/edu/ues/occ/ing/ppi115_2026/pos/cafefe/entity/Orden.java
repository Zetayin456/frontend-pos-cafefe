package sv.edu.ues.occ.ing.ppi115_2026.pos.cafefe.entity;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "orden", schema = "public")
@NamedQueries({
    @NamedQuery(name = "Orden.findAll", query = "SELECT o FROM Orden o")})
public class Orden implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Basic(optional = false)
    @NotNull
    @Column(name = "id_orden")
    private UUID idOrden;

    @Column(name = "fecha_creacion")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaCreacion;

    @JoinColumn(name = "id_empleado_rol", referencedColumnName = "id_empleado_rol")
    @ManyToOne(fetch = FetchType.LAZY)
    private EmpleadoRol idEmpleadoRol;

    @OneToMany(mappedBy = "idOrden", fetch = FetchType.LAZY)
private List<OrdenProducto> ordenProductoList;

    public Orden() {
    }

    public Orden(UUID idOrden) {
        this.idOrden = idOrden;
    }

    public UUID getIdOrden() {
        return idOrden;
    }

    public void setIdOrden(UUID idOrden) {
        this.idOrden = idOrden;
    }

    public Date getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(Date fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public EmpleadoRol getIdEmpleadoRol() {
        return idEmpleadoRol;
    }

    public void setIdEmpleadoRol(EmpleadoRol idEmpleadoRol) {
        this.idEmpleadoRol = idEmpleadoRol;
    }

    public List<OrdenProducto> getOrdenProductoList() {
        return ordenProductoList;
    }

    public void setOrdenProductoList(List<OrdenProducto> ordenProductoList) {
        this.ordenProductoList = ordenProductoList;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (idOrden != null ? idOrden.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Orden)) {
            return false;
        }
        Orden other = (Orden) object;
        if ((this.idOrden == null && other.idOrden != null) || (this.idOrden != null && !this.idOrden.equals(other.idOrden))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "sv.edu.ues.occ.ing.ppi115_2026.pos.cafefe.entity.Orden[ idOrden=" + idOrden + " ]";
    }
}
