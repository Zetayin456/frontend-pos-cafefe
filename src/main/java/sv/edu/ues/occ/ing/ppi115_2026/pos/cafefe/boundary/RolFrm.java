
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
import sv.edu.ues.occ.ing.ppi115_2026.pos.cafefe.control.RolRepository;
import sv.edu.ues.occ.ing.ppi115_2026.pos.cafefe.entity.Rol;

@Named(value = "rolFrm")
@ViewScoped
public class RolFrm implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private RolRepository rolRepository;

    private List<Rol> listaRol;
    private Rol registroSeleccionado;

    @PostConstruct
    public void init() {
        nuevoRegistro();
        cargarDatos();
    }

    public void nuevoRegistro() {
        registroSeleccionado = new Rol();
        registroSeleccionado.setActivo(true);
    }

    public void cargarDatos() {
        try {
            List<Rol> datos = rolRepository.findRange(0, 1000);
            listaRol = datos != null ? datos : new ArrayList<>();
        } catch (Exception e) {
            mostrarMensaje(
                    FacesMessage.SEVERITY_ERROR,
                    "Error",
                    "No se pudieron cargar los roles."
            );
        }
    }

    public void seleccionar(Rol rol) {
        if (rol == null) {
            return;
        }

        // Copiamos los datos para no modificar la fila directamente.
        registroSeleccionado = new Rol();
        registroSeleccionado.setIdRol(rol.getIdRol());
        registroSeleccionado.setNombre(rol.getNombre());
        registroSeleccionado.setObservaciones(rol.getObservaciones());
        registroSeleccionado.setActivo(rol.getActivo());
    }

    public void guardar() {
        try {
            if (registroSeleccionado == null) {
                mostrarMensaje(
                        FacesMessage.SEVERITY_ERROR,
                        "Error",
                        "No hay un rol seleccionado."
                );
                return;
            }

            String nombre = registroSeleccionado.getNombre();

            if (nombre == null || nombre.trim().isEmpty()) {
                mostrarMensaje(
                        FacesMessage.SEVERITY_WARN,
                        "Validación",
                        "El nombre del rol es obligatorio."
                );
                return;
            }

            registroSeleccionado.setNombre(nombre.trim());

            if (registroSeleccionado.getIdRol() == null) {
                registroSeleccionado.setIdRol(UUID.randomUUID());
                rolRepository.create(registroSeleccionado);

                mostrarMensaje(
                        FacesMessage.SEVERITY_INFO,
                        "Éxito",
                        "Rol registrado correctamente."
                );
            } else {
                rolRepository.edit(registroSeleccionado);

                mostrarMensaje(
                        FacesMessage.SEVERITY_INFO,
                        "Éxito",
                        "Rol actualizado correctamente."
                );
            }

            nuevoRegistro();
            cargarDatos();

        } catch (Exception e) {
            mostrarMensaje(
                    FacesMessage.SEVERITY_ERROR,
                    "Error",
                    "No se pudo guardar el rol. Verifica los datos."
            );
        }
    }

    public void eliminar(Rol rol) {
        if (rol == null || rol.getIdRol() == null) {
            mostrarMensaje(
                    FacesMessage.SEVERITY_WARN,
                    "Aviso",
                    "No se seleccionó un rol válido."
            );
            return;
        }

        try {
            rolRepository.remove(rol);

            if (registroSeleccionado != null
                    && rol.getIdRol().equals(registroSeleccionado.getIdRol())) {
                nuevoRegistro();
            }

            cargarDatos();

            mostrarMensaje(
                    FacesMessage.SEVERITY_INFO,
                    "Éxito",
                    "Rol eliminado correctamente."
            );

        } catch (Exception e) {
            mostrarMensaje(
                    FacesMessage.SEVERITY_ERROR,
                    "Error",
                    "No se pudo eliminar el rol. Puede estar asociado a otros registros."
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

    public List<Rol> getListaRol() {
        return listaRol;
    }

    public void setListaRol(List<Rol> listaRol) {
        this.listaRol = listaRol;
    }

    public Rol getRegistroSeleccionado() {
        return registroSeleccionado;
    }

    public void setRegistroSeleccionado(Rol registroSeleccionado) {
        this.registroSeleccionado = registroSeleccionado;
    }
}
