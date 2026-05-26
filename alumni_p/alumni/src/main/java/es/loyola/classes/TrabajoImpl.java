package es.loyola.classes;

import java.util.Date;

public class TrabajoImpl implements Trabajo {
    private String descripcion;
    private String posicion;
    private String ciudad;
    private Date fechaInicio;
    private Date fechaFinal;
    private LugarTrabajo lugarTrabajo;

    public TrabajoImpl(String descripcion, String lugar, String ciudad, Date fechaInicio, Date fechaFin) {
        this.descripcion = descripcion;
        this.posicion = descripcion;
        this.ciudad = ciudad;
        this.fechaInicio = fechaInicio;
        this.fechaFinal = fechaFin;
        this.lugarTrabajo = new LugarTrabajo(lugar, "", ciudad);
    }

    public TrabajoImpl(String descripcion, String posicion, LugarTrabajo lugarTrabajo, Date fechaInicio, Date fechaFinal) {
        this.descripcion = descripcion;
        this.posicion = posicion;
        this.lugarTrabajo = lugarTrabajo;
        this.ciudad = lugarTrabajo == null ? "" : lugarTrabajo.getCiudad();
        this.fechaInicio = fechaInicio;
        this.fechaFinal = fechaFinal;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getPosicion() {
        return posicion;
    }

    public void setPosicion(String posicion) {
        this.posicion = posicion;
    }

    public String getLugar() {
        return lugarTrabajo == null ? "" : lugarTrabajo.getNombre();
    }

    public void setLugar(String lugar) {
        if (lugarTrabajo == null) {
            lugarTrabajo = new LugarTrabajo(lugar, "", ciudad);
        } else {
            lugarTrabajo.setNombre(lugar);
        }
    }

    public LugarTrabajo getLugarTrabajo() {
        return lugarTrabajo;
    }

    public void setLugarTrabajo(LugarTrabajo lugarTrabajo) {
        this.lugarTrabajo = lugarTrabajo;
        this.ciudad = lugarTrabajo == null ? ciudad : lugarTrabajo.getCiudad();
    }

    public String getCiudadTrabajo() {
        return ciudad;
    }

    public void setCiudadTrabajo(String ciudad) {
        this.ciudad = ciudad;
        if (lugarTrabajo != null) {
            lugarTrabajo.setCiudad(ciudad);
        }
    }

    public Date getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(Date fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public Date getFechaFin() {
        return fechaFinal;
    }

    public void setFechaFin(Date fechaFin) {
        this.fechaFinal = fechaFin;
    }

    public Date getFechaFinal() {
        return fechaFinal;
    }

    public void setFechaFinal(Date fechaFinal) {
        this.fechaFinal = fechaFinal;
    }

    public void actualizar(String descripcion, String posicion, String ciudad) {
        this.descripcion = descripcion;
        this.posicion = posicion;
        setCiudadTrabajo(ciudad);
    }

    public void eliminar() {
        this.descripcion = "";
        this.posicion = "";
        this.lugarTrabajo = null;
        this.ciudad = "";
        this.fechaInicio = null;
        this.fechaFinal = null;
    }
}
