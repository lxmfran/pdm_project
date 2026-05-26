package es.loyola.classes;

import java.util.Date;

/**
 * Propuesta de evento o actividad enviada por Alumni/PDI.
 * Cubre RF-4, RI-6, RN-5: queda en estado PENDIENTE hasta ser revisada
 * por PTGAS/Admin (CU-007, CU-010).
 */
public interface PropuestaEvento {

    String ESTADO_PENDIENTE = "PENDIENTE";
    String ESTADO_APROBADA = "APROBADA";
    String ESTADO_RECHAZADA = "RECHAZADA";
    String ESTADO_PUBLICADA = "PUBLICADA";

    String TIPO_EVENTO = "EVENTO";
    String TIPO_ACTIVIDAD = "ACTIVIDAD";

    Integer getId();
    void setId(Integer id);

    /** EVENTO o ACTIVIDAD */
    String getTipo();
    void setTipo(String tipo);

    String getSolicitante();
    void setSolicitante(String solicitante);

    String getRolSolicitante();
    void setRolSolicitante(String rol);

    String getNombre();
    void setNombre(String nombre);

    String getDescripcion();
    void setDescripcion(String descripcion);

    String getLugar();
    void setLugar(String lugar);

    Date getFechaEvento();
    void setFechaEvento(Date fechaEvento);

    Date getFechaAperturaInscripcion();
    void setFechaAperturaInscripcion(Date fecha);

    Date getFechaLimiteInscripcion();
    void setFechaLimiteInscripcion(Date fecha);

    Integer getCapacidadMaxima();
    void setCapacidadMaxima(Integer capacidadMaxima);

    String getPonente();
    void setPonente(String ponente);

    String getEstado();
    void setEstado(String estado);

    String getMotivoDecision();
    void setMotivoDecision(String motivo);

    String getEvaluador();
    void setEvaluador(String evaluador);

    Date getFechaEnvio();
    void setFechaEnvio(Date fechaEnvio);

    Date getFechaDecision();
    void setFechaDecision(Date fechaDecision);

    Integer getRecursoPublicadoId();
    void setRecursoPublicadoId(Integer id);
}
