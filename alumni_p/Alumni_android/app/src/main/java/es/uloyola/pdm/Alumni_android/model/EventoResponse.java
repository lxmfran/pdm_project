package es.uloyola.pdm.Alumni_android.model;

public class EventoResponse extends GenericResponse {
    private Evento evento;
    private Object propuesta;
    private String mensaje;
    public Evento getEvento() { return evento; }
    public void setEvento(Evento v) { this.evento = v; }
    public Object getPropuesta() { return propuesta; }
    public void setPropuesta(Object v) { this.propuesta = v; }
    public String getMensaje() { return mensaje; }
    public void setMensaje(String v) { this.mensaje = v; }
}
