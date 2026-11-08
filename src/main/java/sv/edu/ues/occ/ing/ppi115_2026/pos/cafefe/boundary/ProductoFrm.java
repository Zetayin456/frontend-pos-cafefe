
package sv.edu.ues.occ.ing.ppi115_2026.pos.cafefe.boundary;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import sv.edu.ues.occ.ing.ppi115_2026.pos.cafefe.control.ProductoRepository;
import sv.edu.ues.occ.ing.ppi115_2026.pos.cafefe.entity.Producto;

@Named(value = "productoFrm")
@ViewScoped
public class ProductoFrm implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER =
            Logger.getLogger(ProductoFrm.class.getName());

    @Inject
    private ProductoRepository productoRepository;

    private List<Producto> listaProductos;
    private Producto registroSeleccionado;

    @PostConstruct
    public void init() {
        nuevoRegistro();
        cargarDatos();
    }

    public void nuevoRegistro() {
        registroSeleccionado = new Producto();
        registroSeleccionado.setActivo(true);
    }

    public void cargarDatos() {
        try {
            List<Producto> productos = productoRepository.findRange(0, 1000);

            listaProductos = productos != null
                    ? productos : new ArrayList<>();

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al cargar productos", e);
            mensaje(FacesMessage.SEVERITY_ERROR,
                    "Error", "No se pudieron cargar los productos.");
        }
    }

    public void seleccionar(Producto producto) {
        if (producto == null) {
            return;
        }

        // Copiamos los datos para editar el registro.
        registroSeleccionado = new Producto();
        registroSeleccionado.setIdProducto(producto.getIdProducto());
        registroSeleccionado.setNombre(producto.getNombre());
        registroSeleccionado.setPrecioSugerido(producto.getPrecioSugerido());
        registroSeleccionado.setActivo(producto.getActivo());
        registroSeleccionado.setComentarios(producto.getComentarios());
    }

    public void guardar() {
        try {
            if (registroSeleccionado == null) {
                mensaje(FacesMessage.SEVERITY_WARN,
                        "Aviso", "No hay un producto seleccionado.");
                return;
            }

            String nombre = registroSeleccionado.getNombre();
            BigDecimal precio = registroSeleccionado.getPrecioSugerido();

            if (nombre == null || nombre.isBlank()) {
                mensaje(FacesMessage.SEVERITY_WARN,
                        "Validación", "Escribe el nombre del producto.");
                return;
            }

            if (nombre.trim().length() > 155) {
                mensaje(FacesMessage.SEVERITY_WARN,
                        "Validación",
                        "El nombre no puede superar los 155 caracteres.");
                return;
            }

            if (precio == null || precio.compareTo(BigDecimal.ZERO) <= 0) {
                mensaje(FacesMessage.SEVERITY_WARN,
                        "Validación", "El precio debe ser mayor que cero.");
                return;
            }

            registroSeleccionado.setNombre(nombre.trim());

            if (registroSeleccionado.getActivo() == null) {
                registroSeleccionado.setActivo(true);
            }

            if (registroSeleccionado.getIdProducto() == null) {
                registroSeleccionado.setIdProducto(UUID.randomUUID());
                productoRepository.create(registroSeleccionado);

                mensaje(FacesMessage.SEVERITY_INFO,
                        "Éxito", "Producto creado correctamente.");
            } else {
                productoRepository.edit(registroSeleccionado);

                mensaje(FacesMessage.SEVERITY_INFO,
                        "Éxito", "Producto actualizado correctamente.");
            }

            nuevoRegistro();
            cargarDatos();

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al guardar producto", e);
            mensaje(FacesMessage.SEVERITY_ERROR,
                    "Error", "No se pudo guardar el producto.");
        }
    }

    public void eliminar(Producto producto) {
        if (producto == null || producto.getIdProducto() == null) {
            mensaje(FacesMessage.SEVERITY_WARN,
                    "Aviso", "Selecciona un producto válido.");
            return;
        }

        try {
            productoRepository.remove(producto);

            if (registroSeleccionado != null
                    && producto.getIdProducto().equals(
                            registroSeleccionado.getIdProducto())) {
                nuevoRegistro();
            }

            cargarDatos();

            mensaje(FacesMessage.SEVERITY_INFO,
                    "Éxito", "Producto eliminado correctamente.");

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al eliminar producto", e);
            mensaje(FacesMessage.SEVERITY_ERROR,
                    "Error", "No se pudo eliminar el producto. Puede estar asociado a otros registros.");
        }
    }

    private void mensaje(FacesMessage.Severity severidad,
                         String titulo, String detalle) {
        FacesContext.getCurrentInstance().addMessage(
                null, new FacesMessage(severidad, titulo, detalle));
    }

    public List<Producto> getListaProductos() {
        return listaProductos;
    }

    public void setListaProductos(List<Producto> listaProductos) {
        this.listaProductos = listaProductos;
    }

    public Producto getRegistroSeleccionado() {
        return registroSeleccionado;
    }

    public void setRegistroSeleccionado(Producto registroSeleccionado) {
        this.registroSeleccionado = registroSeleccionado;
    }
}
