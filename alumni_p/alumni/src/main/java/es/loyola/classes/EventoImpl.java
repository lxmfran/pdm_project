package es.loyola.classes;

import java.util.Date;
import java.util.List;

public class EventoImpl implements Evento {
    private Integer id;
    private String nombre;
    private String descripcion;
    private String lugar;
    private String ponente;
    private Organizador organizador;
    private Date fechaAperturaInscripcion;
    private Date fechaLimiteInscripcion;
    private Date fechaEvento;
    private Integer capacidadMaxima;
    private List<Alumni> lista;
    private String estado;
    private String propietario;

    public EventoImpl(Integer id, String nombre, String descripcion, String lugar, String ponente,
                      Organizador organizador, Date fechaAperturaInscripcion, Date fechaLimiteInscripcion,
                      Date fechaEvento, List<Alumni> lista) {
        this(id, nombre, descripcion, lugar, ponente, organizador, fechaAperturaInscripcion,
                fechaLimiteInscripcion, fechaEvento, 0, lista);
    }

    public EventoImpl(Integer id, String nombre, String descripcion, String lugar, String ponente,
                      Organizador organizador, Date fechaAperturaInscripcion, Date fechaLimiteInscripcion,
                      Date fechaEvento, Integer capacidadMaxima, List<Alumni> lista) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.lugar = lugar;
        this.ponente = ponente;
        this.organizador = organizador;
        this.fechaAperturaInscripcion = fechaAperturaInscripcion;
        this.fechaLimiteInscripcion = fechaLimiteInscripcion;
        this.fechaEvento = fechaEvento;
        this.capacidadMaxima = capacidadMaxima;
        this.lista = lista;
        this.estado = "PUBLICADO";
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNombreEvento() {
        return nombre;
    }

    public void setNombreEvento(String nombre) {
        this.nombre = nombre;
    }

    public Date getFechaAperturaInscripcion() {
        return fechaAperturaInscripcion;
    }

    public void setFechaAperturaInscripcion(Date fecha) {
        this.fechaAperturaInscripcion = fecha;
    }

    public Date getFechaLimiteInscripcion() {
        return fechaLimiteInscripcion;
    }

    public void setFechaLimiteInscripcion(Date fecha) {
        this.fechaLimiteInscripcion = fecha;
    }

    public String getDescripcionEvento() {
        return descripcion;
    }

    public void setDescripcionEvento(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getLugarEvento() {
        return lugar;
    }

    public void setLugarEvento(String lugar) {
        this.lugar = lugar;
    }

    public List<Alumni> getListaAlumni() {
        return lista;
    }

    public void setListaAlumni(List<Alumni> lista) {
        this.lista = lista;
    }

    public Date getFechaLimite() {
        return fechaLimiteInscripcion;
    }

    public void setFechaLimite(Date fechaLimite) {
        this.fechaLimiteInscripcion = fechaLimite;
    }

    public Date getFechaEvento() {
        return fechaEvento;
    }

    public void setFechaEvento(Date fechaEvento) {
        this.fechaEvento = fechaEvento;
    }

    public String getPonente() {
        return ponente;
    }

    public void setPonente(String ponente) {
        this.ponente = ponente;
    }

    public Organizador getOrganizador() {
        return organizador;
    }

    public void setOrganizador(Organizador organizador) {
        this.organizador = organizador;
    }

    public Integer getCapacidadMaxima() {
        return capacidadMaxima;
    }

    public void setCapacidadMaxima(Integer capacidadMaxima) {
        this.capacidadMaxima = capacidadMaxima;
    }

    public void publicar() {
        this.estado = "PUBLICADO";
    }

    public void modificar(String nombre, String descripcion, String lugar) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.lugar = lugar;
    }

    public void eliminar() {
        this.estado = "ELIMINADO";
    }

    public void cancelarEvento() {
        this.estado = "CANCELADO";
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getPropietario() {
        return propietario;
    }

    public void setPropietario(String propietario) {
        this.propietario = propietario;
    }
}
