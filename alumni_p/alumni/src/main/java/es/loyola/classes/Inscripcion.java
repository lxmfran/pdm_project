package es.loyola.classes;

import java.util.Date;

public interface Inscripcion {

    String ESTADO_CONFIRMADA = "CONFIRMADA";
    String ESTADO_CANCELADA = "CANCELADA";

    String TIPO_EVENTO = "EVENTO";
    String TIPO_ACTIVIDAD = "ACTIVIDAD";

    Integer getId();
    void setId(Integer id);

    String getTipoRecurso();
    void setTipoRecurso(String tipoRecurso);

    Integer getRecursoId();
    void setRecursoId(Integer recursoId);

    String getUsuario();
    void setUsuario(String usuario);

    String getRolUsuario();
    void setRolUsuario(String rol);

    String getTicket();
    void setTicket(String ticket);

    Date getFechaInscripcion();
    void setFechaInscripcion(Date fechaInscripcion);

    String getEstado();
    void setEstado(String estado);

    String generarTicket();
    void cancelar();
}
