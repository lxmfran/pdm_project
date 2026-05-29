package es.loyola.classes;

import java.util.Date;

/**
 * Notificacion entregada a un usuario (RI-11).
 *
 * Cubre los tipos previstos por el PDS:
 *   - INACTIVIDAD          (RF-12, RN-12)
 *   - PROPUESTA_RESUELTA   (CU-010)
 *   - EVENTO_RECORDATORIO
 *   - SISTEMA              (mensajes administrativos)
 */
public class Notificacion {

    public static final String TIPO_INACTIVIDAD        = "INACTIVIDAD";
    public static final String TIPO_PROPUESTA_RESUELTA = "PROPUESTA_RESUELTA";
    public static final String TIPO_EVENTO_RECORDATORIO = "EVENTO_RECORDATORIO";
    public static final String TIPO_SISTEMA            = "SISTEMA";

    private final Integer id;
    private final String  destinatario;
    private final String  tipo;
    private final String  asunto;
    private final String  mensaje;
    private final Date    fechaEnvio;
    private boolean       leida;

    /** Constructor para notificaciones nuevas: fecha actual, no leida. */
    public Notificacion(String destinatario, String tipo, String asunto, String mensaje) {
        this(null, destinatario, tipo, asunto, mensaje, new Date(), false);
    }

    /** Constructor completo: usado al hidratar desde la BD. */
    public Notificacion(Integer id, String destinatario, String tipo, String asunto,
                        String mensaje, Date fechaEnvio, boolean leida) {
        this.id = id;
        this.destinatario = destinatario;
        this.tipo = tipo;
        this.asunto = asunto;
        this.mensaje = mensaje;
        this.fechaEnvio = fechaEnvio == null ? new Date() : fechaEnvio;
        this.leida = leida;
    }

    public Integer getId()           { return id; }
    public String  getDestinatario() { return destinatario; }
    public String  getTipo()         { return tipo; }
    public String  getAsunto()       { return asunto; }
    public String  getMensaje()      { return mensaje; }
    public Date    getFechaEnvio()   { return fechaEnvio; }
    public boolean isLeida()         { return leida; }

    public void marcarComoLeida() { this.leida = true; }
}
