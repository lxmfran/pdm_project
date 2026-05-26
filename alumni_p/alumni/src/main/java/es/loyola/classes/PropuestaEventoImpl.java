package es.loyola.classes;

import java.util.Date;

public class PropuestaEventoImpl implements PropuestaEvento {
    private Integer id;
    private String tipo;
    private String solicitante;
    private String rolSolicitante;
    private String nombre;
    private String descripcion;
    private String lugar;
    private Date fechaEvento;
    private Date fechaAperturaInscripcion;
    private Date fechaLimiteInscripcion;
    private Integer capacidadMaxima;
    private String ponente;
    private String estado;
    private String motivoDecision;
    private String evaluador;
    private Date fechaEnvio;
    private Date fechaDecision;
    private Integer recursoPublicadoId;

    public PropuestaEventoImpl() {
        this.estado = ESTADO_PENDIENTE;
        this.fechaEnvio = new Date();
        this.tipo = TIPO_EVENTO;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getTipo() { return tipo == null ? TIPO_EVENTO : tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getSolicitante() { return solicitante; }
    public void setSolicitante(String solicitante) { this.solicitante = solicitante; }

    public String getRolSolicitante() { return rolSolicitante; }
    public void setRolSolicitante(String rol) { this.rolSolicitante = rol; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getLugar() { return lugar; }
    public void setLugar(String lugar) { this.lugar = lugar; }

    public Date getFechaEvento() { return fechaEvento; }
    public void setFechaEvento(Date fechaEvento) { this.fechaEvento = fechaEvento; }

    public Date getFechaAperturaInscripcion() { return fechaAperturaInscripcion; }
    public void setFechaAperturaInscripcion(Date fecha) { this.fechaAperturaInscripcion = fecha; }

    public Date getFechaLimiteInscripcion() { return fechaLimiteInscripcion; }
    public void setFechaLimiteInscripcion(Date fecha) { this.fechaLimiteInscripcion = fecha; }

    public Integer getCapacidadMaxima() { return capacidadMaxima; }
    public void setCapacidadMaxima(Integer capacidadMaxima) { this.capacidadMaxima = capacidadMaxima; }

    public String getPonente() { return ponente; }
    public void setPonente(String ponente) { this.ponente = ponente; }

    public String getEstado() { return estado == null ? ESTADO_PENDIENTE : estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getMotivoDecision() { return motivoDecision; }
    public void setMotivoDecision(String motivo) { this.motivoDecision = motivo; }

    public String getEvaluador() { return evaluador; }
    public void setEvaluador(String evaluador) { this.evaluador = evaluador; }

    public Date getFechaEnvio() { return fechaEnvio; }
    public void setFechaEnvio(Date fechaEnvio) { this.fechaEnvio = fechaEnvio; }

    public Date getFechaDecision() { return fechaDecision; }
    public void setFechaDecision(Date fechaDecision) { this.fechaDecision = fechaDecision; }

    public Integer getRecursoPublicadoId() { return recursoPublicadoId; }
    public void setRecursoPublicadoId(Integer id) { this.recursoPublicadoId = id; }
}
