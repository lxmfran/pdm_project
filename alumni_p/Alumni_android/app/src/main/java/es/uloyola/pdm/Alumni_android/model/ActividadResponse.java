package es.uloyola.pdm.Alumni_android.model;

public class ActividadResponse extends GenericResponse {
    private Evento actividad;
    private Object propuesta;
    private String mensaje;
    public Evento getActividad() { return actividad; }
    public void setActividad(Evento v) { this.actividad = v; }
    public Object getPropuesta() { return propuesta; }
    public void setPropuesta(Object v) { this.propuesta = v; }
    public String getMensaje() { return mensaje; }
    public void setMensaje(String v) { this.mensaje = v; }
}
