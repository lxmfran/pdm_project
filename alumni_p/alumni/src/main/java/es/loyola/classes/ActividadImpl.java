package es.loyola.classes;

import es.loyola.enums.ExperienciaHobbies;
import java.util.Date;
import java.util.List;

public class ActividadImpl implements Actividad {
    private Integer id;
    private String lugar;
    private String nombre;
    private String descripcion;
    private Date fecha;
    private Organizador organizadorActividad;
    private String organizador;
    private Hobbie hobbie;
    private ExperienciaHobbies experiencia;
    private String nivelMedioParticipacion;
    private Date fechaAperturaInscripcion;
    private Date fechaLimiteInscripcion;
    private Integer plazasLimites;
    private List<Alumni> lista;
    private String estado;
    private String propietario;

    public ActividadImpl(Integer id, String lugar, String nombre, Hobbie hobbie, ExperienciaHobbies experiencia,
                         Date fechaApertura, List<Alumni> lista, Date fechaLimiteActividad, Integer plazas,
                         Organizador organizador) {
        this.id = id;
        this.lugar = lugar;
        this.nombre = nombre;
        this.descripcion = "";
        this.fecha = fechaLimiteActividad;
        this.hobbie = hobbie;
        this.experiencia = experiencia;
        this.nivelMedioParticipacion = experiencia == null ? "" : experiencia.name();
        this.fechaAperturaInscripcion = fechaApertura;
        this.lista = lista;
        this.fechaLimiteInscripcion = fechaLimiteActividad;
        this.plazasLimites = plazas;
        this.organizadorActividad = organizador;
        this.organizador = organizador == null ? "" : organizador.getNombreOrganizador();
        this.estado = "PUBLICADA";
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getLugarActividad() {
        return lugar;
    }

    public void setLugarActividad(String lugar) {
        this.lugar = lugar;
    }

    public String getNombreActividad() {
        return nombre;
    }

    public void setNombreActividad(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public String getOrganizador() {
        return organizador;
    }

    public void setOrganizador(String organizador) {
        this.organizador = organizador;
    }

    public Hobbie getHobbiePracticado() {
        return hobbie;
    }

    public void setHobbiePracticado(Hobbie hobbie) {
        this.hobbie = hobbie;
    }

    public ExperienciaHobbies getNivelParticipacion() {
        return experiencia;
    }

    public void setNivelParticipacion(ExperienciaHobbies experiencia) {
        this.experiencia = experiencia;
        this.nivelMedioParticipacion = experiencia == null ? "" : experiencia.name();
    }

    public String getNivelMedioParticipacion() {
        return nivelMedioParticipacion;
    }

    public void setNivelMedioParticipacion(String nivel) {
        this.nivelMedioParticipacion = nivel;
    }

    public Date getFechaApertura() {
        return fechaAperturaInscripcion;
    }

    public void setFechaApertura(Date fechaApertura) {
        this.fechaAperturaInscripcion = fechaApertura;
    }

    public Date getFechaAperturaInscripcion() {
        return fechaAperturaInscripcion;
    }

    public void setFechaAperturaInscripcion(Date fechaApertura) {
        this.fechaAperturaInscripcion = fechaApertura;
    }

    public List<Alumni> getAlumniInscritos() {
        return lista;
    }

    public void setAlumniInscritos(List<Alumni> lista) {
        this.lista = lista;
    }

    public Date getFechaLimiteActividad() {
        return fechaLimiteInscripcion;
    }

    public void setFechaLimiteActividad(Date fechaLimiteActividad) {
        this.fechaLimiteInscripcion = fechaLimiteActividad;
    }

    public Date getFechaLimiteInscripcion() {
        return fechaLimiteInscripcion;
    }

    public void setFechaLimiteInscripcion(Date fechaLimiteInscripcion) {
        this.fechaLimiteInscripcion = fechaLimiteInscripcion;
    }

    public Integer getMaxPlazas() {
        return plazasLimites;
    }

    public void setMaxPlazas(Integer plazas) {
        this.plazasLimites = plazas;
    }

    public Integer getPlazasLimites() {
        return plazasLimites;
    }

    public void setPlazasLimites(Integer plazas) {
        this.plazasLimites = plazas;
    }

    public Organizador getOrganizadorActividad() {
        return organizadorActividad;
    }

    public void setOrganizadorActividad(Organizador organizador) {
        this.organizadorActividad = organizador;
        this.organizador = organizador == null ? "" : organizador.getNombreOrganizador();
    }

    public void publicar() {
        this.estado = "PUBLICADA";
    }

    public void modificar(String nombre, String descripcion, String lugar) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.lugar = lugar;
    }

    public void eliminar() {
        this.estado = "ELIMINADA";
    }

    public void cancelar() {
        this.estado = "CANCELADA";
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
