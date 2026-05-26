package es.loyola.classes;

import java.util.Date;
import java.util.List;

public interface Administrador extends Usuario {
    List<String> getPermisos();
    void setPermisos(List<String> permisos);
    Date getFechaUltima();
    void setFechaUltima(Date fecha);
    Date getFechaUltimoAcceso();
    void setFechaUltimoAcceso(Date fecha);
    void crearUsuario();
    void eliminarUsuario();
    void crearEvento();
    void gestionarEvento();
    void modificarEvento();
    void accederPanelControl();
}
