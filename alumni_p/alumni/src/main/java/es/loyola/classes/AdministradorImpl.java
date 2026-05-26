package es.loyola.classes;

import java.util.Date;
import java.util.List;

public class AdministradorImpl extends UsuarioImpl implements Administrador {
    private List<String> permisos;
    private Date fechaUltimoAcceso;

    public AdministradorImpl(String nombre, String apellidos, String email, String telefono,
                             Credenciales credenciales, List<String> permisos, Date fecha) {
        super(nombre, apellidos, email, telefono, credenciales);
        this.permisos = permisos;
        this.fechaUltimoAcceso = fecha;
    }

    public List<String> getPermisos() {
        return permisos;
    }

    public void setPermisos(List<String> permisos) {
        this.permisos = permisos;
    }

    public Date getFechaUltima() {
        return fechaUltimoAcceso;
    }

    public void setFechaUltima(Date fecha) {
        this.fechaUltimoAcceso = fecha;
    }

    public Date getFechaUltimoAcceso() {
        return fechaUltimoAcceso;
    }

    public void setFechaUltimoAcceso(Date fecha) {
        this.fechaUltimoAcceso = fecha;
    }

    public void crearUsuario() {
    }

    public void eliminarUsuario() {
    }

    public void crearEvento() {
    }

    public void gestionarEvento() {
    }

    public void modificarEvento() {
    }

    public void accederPanelControl() {
    }
}
