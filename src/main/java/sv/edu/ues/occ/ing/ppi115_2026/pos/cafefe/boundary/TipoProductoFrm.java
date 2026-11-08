
package sv.edu.ues.occ.ing.ppi115_2026.pos.cafefe.boundary;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import sv.edu.ues.occ.ing.ppi115_2026.pos.cafefe.control.TipoProductoRepository;
import sv.edu.ues.occ.ing.ppi115_2026.pos.cafefe.entity.TipoProducto;

@Named(value = "tipoProductoFrm")
@ViewScoped
public class TipoProductoFrm implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private TipoProductoRepository tipoProductoRepository;

    private List<TipoProducto> listaTipoProducto;
    private TipoProducto registroSeleccionado;

    @PostConstruct
    public void init() {
        nuevoRegistro();
        cargarDatos();
    }

    public void nuevoRegistro() {
        registroSeleccionado = new TipoProducto();
        registroSeleccionado.setActivo(true);
    }

    public void cargarDatos() {
        try {
            List<TipoProducto> datos = tipoProductoRepository.findRange(0, 1000);

            listaTipoProducto = datos != null
                    ? datos : new ArrayList<>();

        } catch (Exception e) {
            mostrarMensaje(
                    FacesMessage.SEVERITY_ERROR,
                    "Error",
                    "No se pudieron cargar los tipos de producto."
            );
        }
    }

    public void seleccionarRegistro(TipoProducto tipoProducto) {
        if (tipoProducto == null) {
            return;
        }

        // Copia los datos para no modificar la fila antes de guardar.
        registroSeleccionado = new TipoProducto();
        registroSeleccionado.setIdTipoProducto(
                tipoProducto.getIdTipoProducto());
        registroSeleccionado.setNombre(tipoProducto.getNombre());
        registroSeleccionado.setObservaciones(
                tipoProducto.getObservaciones());
        registroSeleccionado.setActivo(tipoProducto.getActivo());
    }

    public void guardar() {
        try {
            if (registroSeleccionado == null) {
                mostrarMensaje(
                        FacesMessage.SEVERITY_ERROR,
                        "Error",
                        "No hay un tipo de producto seleccionado."
                );
                return;
            }

            String nombre = registroSeleccionado.getNombre();

            if (nombre == null || nombre.trim().isEmpty()) {
                mostrarMensaje(
                        FacesMessage.SEVERITY_WARN,
                        "Validación",
                        "El nombre es obligatorio."
                );
                return;
            }

            registroSeleccionado.setNombre(nombre.trim());

            boolean esNuevo =
                    registroSeleccionado.getIdTipoProducto() == null;

            if (esNuevo) {
                registroSeleccionado.setIdTipoProducto(UUID.randomUUID());
                tipoProductoRepository.create(registroSeleccionado);

                mostrarMensaje(
                        FacesMessage.SEVERITY_INFO,
                        "Éxito",
                        "Tipo de producto creado correctamente."
                );
            } else {
                tipoProductoRepository.edit(registroSeleccionado);

                mostrarMensaje(
                        FacesMessage.SEVERITY_INFO,
                        "Éxito",
                        "Tipo de producto actualizado correctamente."
                );
            }

            nuevoRegistro();
            cargarDatos();

        } catch (Exception e) {
            mostrarMensaje(
                    FacesMessage.SEVERITY_ERROR,
                    "Error",
                    "No se pudieron guardar los datos. Verifica los valores."
            );
        }
    }

    public void eliminar(TipoProducto tipoProducto) {
        if (tipoProducto == null
                || tipoProducto.getIdTipoProducto() == null) {
            mostrarMensaje(
                    FacesMessage.SEVERITY_WARN,
                    "Aviso",
                    "No se seleccionó un tipo de producto válido."
            );
            return;
        }

        try {
            tipoProductoRepository.remove(tipoProducto);

            mostrarMensaje(
                    FacesMessage.SEVERITY_INFO,
                    "Éxito",
                    "Tipo de producto eliminado correctamente."
            );

            if (registroSeleccionado != null
                    && tipoProducto.getIdTipoProducto().equals(
                            registroSeleccionado.getIdTipoProducto())) {
                nuevoRegistro();
            }

            cargarDatos();

        } catch (Exception e) {
            mostrarMensaje(
                    FacesMessage.SEVERITY_ERROR,
                    "Error",
                    "No se pudo eliminar el tipo de producto. Puede estar asociado a productos."
            );
        }
    }

    private void mostrarMensaje(
            FacesMessage.Severity severidad,
            String titulo,
            String detalle) {

        FacesContext.getCurrentInstance().addMessage(
                null,
                new FacesMessage(severidad, titulo, detalle)
        );
    }

    public List<TipoProducto> getListaTipoProducto() {
        return listaTipoProducto;
    }

    public void setListaTipoProducto(
            List<TipoProducto> listaTipoProducto) {
        this.listaTipoProducto = listaTipoProducto;
    }

    public TipoProducto getRegistroSeleccionado() {
        return registroSeleccionado;
    }

    public void setRegistroSeleccionado(
            TipoProducto registroSeleccionado) {
        this.registroSeleccionado = registroSeleccionado;
    }
}
