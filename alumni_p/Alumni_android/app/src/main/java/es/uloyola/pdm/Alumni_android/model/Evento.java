package es.uloyola.pdm.Alumni_android.model;

import java.util.List;

/** POJO de evento (vale tambien para actividades; los campos extra se ignoran si no aplican). */
public class Evento {
    private Integer id;
    private String nombre;
    private String descripcion;
    private String lugar;
    private String ponente;
    private String estado;
    private String propietario;
    private String fechaApertura;
    private String fechaLimite;
    private String fechaEvento;     // eventos
    private String fecha;           // actividades
    private Integer capacidadMaxima;
    private Integer maxPlazas;      // actividades
    private Organizador organizador;
    private List<String> inscritos;
    private String hobbie;
    private String nivelParticipacion;

    public Integer getId() { return id; }
    public void setId(Integer v) { this.id = v; }
    public String getNombre() { return nombre; }
    public void setNombre(String v) { this.nombre = v; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String v) { this.descripcion = v; }
    public String getLugar() { return lugar; }
    public void setLugar(String v) { this.lugar = v; }
    public String getPonente() { return ponente; }
    public void setPonente(String v) { this.ponente = v; }
    public String getEstado() { return estado; }
    public void setEstado(String v) { this.estado = v; }
    public String getPropietario() { return propietario; }
    public void setPropietario(String v) { this.propietario = v; }
    public String getFechaApertura() { return fechaApertura; }
    public void setFechaApertura(String v) { this.fechaApertura = v; }
    public String getFechaLimite() { return fechaLimite; }
    public void setFechaLimite(String v) { this.fechaLimite = v; }
    public String getFechaEvento() { return fechaEvento != null ? fechaEvento : fecha; }
    public void setFechaEvento(String v) { this.fechaEvento = v; }
    public String getFecha() { return fecha; }
    public void setFecha(String v) { this.fecha = v; }
    public Integer getCapacidadMaxima() { return capacidadMaxima != null ? capacidadMaxima : maxPlazas; }
    public void setCapacidadMaxima(Integer v) { this.capacidadMaxima = v; }
    public Integer getMaxPlazas() { return maxPlazas; }
    public void setMaxPlazas(Integer v) { this.maxPlazas = v; }
    public Organizador getOrganizador() { return organizador; }
    public void setOrganizador(Organizador v) { this.organizador = v; }
    public List<String> getInscritos() { return inscritos; }
    public void setInscritos(List<String> v) { this.inscritos = v; }
    public String getHobbie() { return hobbie; }
    public void setHobbie(String v) { this.hobbie = v; }
    public String getNivelParticipacion() { return nivelParticipacion; }
    public void setNivelParticipacion(String v) { this.nivelParticipacion = v; }

    public int getNumInscritos() { return inscritos == null ? 0 : inscritos.size(); }
}
