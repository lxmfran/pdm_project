package es.uloyola.pdm.Alumni_android.model;

public class Trabajo {
    private String descripcion;
    private String posicion;
    private String lugar;
    private String ciudad;
    private String fechaInicio;
    private String fechaFin;

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String v) { this.descripcion = v; }
    public String getPosicion() { return posicion; }
    public void setPosicion(String v) { this.posicion = v; }
    public String getLugar() { return lugar; }
    public void setLugar(String v) { this.lugar = v; }
    public String getCiudad() { return ciudad; }
    public void setCiudad(String v) { this.ciudad = v; }
    public String getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(String v) { this.fechaInicio = v; }
    public String getFechaFin() { return fechaFin; }
    public void setFechaFin(String v) { this.fechaFin = v; }

    public String getResumen() {
        StringBuilder sb = new StringBuilder();
        if (posicion != null && !posicion.isEmpty()) sb.append(posicion);
        if (lugar != null && !lugar.isEmpty()) {
            if (sb.length() > 0) sb.append(" en ");
            sb.append(lugar);
        }
        if (ciudad != null && !ciudad.isEmpty()) sb.append(" (").append(ciudad).append(")");
        return sb.length() > 0 ? sb.toString() : null;
    }
}
