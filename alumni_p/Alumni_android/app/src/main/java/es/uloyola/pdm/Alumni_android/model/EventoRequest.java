package es.uloyola.pdm.Alumni_android.model;

/** Body para POST /EventoServlet o /ActividadServlet. */
public class EventoRequest {
    public String nombre;
    public String descripcion;
    public String lugar;
    public String ponente;
    public String fechaEvento;   // eventos
    public String fecha;         // actividades
    public String fechaApertura;
    public String fechaLimite;
    public Integer capacidadMaxima;
    public Integer maxPlazas;
}
