package es.uloyola.pdm.Alumni_android.model;

public class Propuesta {
    private Integer id;
    private String tipo;           // EVENTO / ACTIVIDAD
    private String solicitante;
    private String rolSolicitante;
    private String nombre;
    private String descripcion;
    private String lugar;
    private String ponente;
    private Integer capacidadMaxima;
    private String fechaEvento;
    private String fechaApertura;
    private String fechaLimite;
    private String estado;         // PENDIENTE / APROBADA / RECHAZADA / PUBLICADA
    private String motivoDecision;
    private String evaluador;
    private String fechaEnvio;
    private String fechaDecision;
    private Integer recursoPublicadoId;

    public Integer getId() { return id; }
    public void setId(Integer v) { this.id = v; }
    public String getTipo() { return tipo; }
    public void setTipo(String v) { this.tipo = v; }
    public String getSolicitante() { return solicitante; }
    public void setSolicitante(String v) { this.solicitante = v; }
    public String getRolSolicitante() { return rolSolicitante; }
    public void setRolSolicitante(String v) { this.rolSolicitante = v; }
    public String getNombre() { return nombre; }
    public void setNombre(String v) { this.nombre = v; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String v) { this.descripcion = v; }
    public String getLugar() { return lugar; }
    public void setLugar(String v) { this.lugar = v; }
    public String getPonente() { return ponente; }
    public void setPonente(String v) { this.ponente = v; }
    public Integer getCapacidadMaxima() { return capacidadMaxima; }
    public void setCapacidadMaxima(Integer v) { this.capacidadMaxima = v; }
    public String getFechaEvento() { return fechaEvento; }
    public void setFechaEvento(String v) { this.fechaEvento = v; }
    public String getFechaApertura() { return fechaApertura; }
    public void setFechaApertura(String v) { this.fechaApertura = v; }
    public String getFechaLimite() { return fechaLimite; }
    public void setFechaLimite(String v) { this.fechaLimite = v; }
    public String getEstado() { return estado; }
    public void setEstado(String v) { this.estado = v; }
    public String getMotivoDecision() { return motivoDecision; }
    public void setMotivoDecision(String v) { this.motivoDecision = v; }
    public String getEvaluador() { return evaluador; }
    public void setEvaluador(String v) { this.evaluador = v; }
    public String getFechaEnvio() { return fechaEnvio; }
    public void setFechaEnvio(String v) { this.fechaEnvio = v; }
    public String getFechaDecision() { return fechaDecision; }
    public void setFechaDecision(String v) { this.fechaDecision = v; }
    public Integer getRecursoPublicadoId() { return recursoPublicadoId; }
    public void setRecursoPublicadoId(Integer v) { this.recursoPublicadoId = v; }
}
