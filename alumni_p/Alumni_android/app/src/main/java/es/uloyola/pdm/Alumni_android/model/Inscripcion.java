package es.uloyola.pdm.Alumni_android.model;

public class Inscripcion {
    private Integer id;
    private String tipo;       // EVENTO / ACTIVIDAD
    private Integer recursoId;
    private String usuario;
    private String rol;
    private String ticket;
    private String estado;     // CONFIRMADA / CANCELADA
    private String fechaInscripcion;

    public Integer getId() { return id; }
    public void setId(Integer v) { this.id = v; }
    public String getTipo() { return tipo; }
    public void setTipo(String v) { this.tipo = v; }
    public Integer getRecursoId() { return recursoId; }
    public void setRecursoId(Integer v) { this.recursoId = v; }
    public String getUsuario() { return usuario; }
    public void setUsuario(String v) { this.usuario = v; }
    public String getRol() { return rol; }
    public void setRol(String v) { this.rol = v; }
    public String getTicket() { return ticket; }
    public void setTicket(String v) { this.ticket = v; }
    public String getEstado() { return estado; }
    public void setEstado(String v) { this.estado = v; }
    public String getFechaInscripcion() { return fechaInscripcion; }
    public void setFechaInscripcion(String v) { this.fechaInscripcion = v; }

    public boolean estaActiva() { return "CONFIRMADA".equalsIgnoreCase(estado); }
}
