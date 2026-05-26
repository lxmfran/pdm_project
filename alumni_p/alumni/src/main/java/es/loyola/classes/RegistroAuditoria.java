package es.loyola.classes;

import java.util.Date;

/**
 * Entrada del registro de auditoría (RI-8, RN-11).
 * Conserva quién hizo qué, sobre qué entidad, cuándo y con qué resultado.
 */
public class RegistroAuditoria {
    private final Integer id;
    private final Date fecha;
    private final String actor;
    private final String rol;
    private final String accion;
    private final String entidad;
    private final String entidadId;
    private final String resultado;
    private final String detalle;

    /** Constructor para registros nuevos: la fecha es la actual. */
    public RegistroAuditoria(Integer id, String actor, String rol, String accion,
                             String entidad, String entidadId, String resultado, String detalle) {
        this(id, new Date(), actor, rol, accion, entidad, entidadId, resultado, detalle);
    }

    /** Constructor completo: usado al hidratar registros existentes desde la BD. */
    public RegistroAuditoria(Integer id, Date fecha, String actor, String rol, String accion,
                             String entidad, String entidadId, String resultado, String detalle) {
        this.id = id;
        this.fecha = fecha == null ? new Date() : fecha;
        this.actor = actor;
        this.rol = rol;
        this.accion = accion;
        this.entidad = entidad;
        this.entidadId = entidadId;
        this.resultado = resultado;
        this.detalle = detalle;
    }

    public Integer getId() { return id; }
    public Date getFecha() { return fecha; }
    public String getActor() { return actor; }
    public String getRol() { return rol; }
    public String getAccion() { return accion; }
    public String getEntidad() { return entidad; }
    public String getEntidadId() { return entidadId; }
    public String getResultado() { return resultado; }
    public String getDetalle() { return detalle; }
}
