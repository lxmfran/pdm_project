package es.loyola.classes;

import java.util.Date;

public interface Trabajo {
    String getDescripcion();
    void setDescripcion(String descripcion);
    String getPosicion();
    void setPosicion(String posicion);
    String getLugar();
    void setLugar(String lugar);
    LugarTrabajo getLugarTrabajo();
    void setLugarTrabajo(LugarTrabajo lugarTrabajo);
    String getCiudadTrabajo();
    void setCiudadTrabajo(String ciudad);
    Date getFechaInicio();
    void setFechaInicio(Date fechaInicio);
    Date getFechaFin();
    void setFechaFin(Date fechaFin);
    Date getFechaFinal();
    void setFechaFinal(Date fechaFinal);
    void actualizar(String descripcion, String posicion, String ciudad);
    void eliminar();
}
