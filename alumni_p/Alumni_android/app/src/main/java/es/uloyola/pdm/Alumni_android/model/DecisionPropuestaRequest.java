package es.uloyola.pdm.Alumni_android.model;

/** Body para PUT /PropuestaServlet: aprobar, rechazar o publicar una propuesta. */
public class DecisionPropuestaRequest {
    public Integer id;
    public String decision;  // APROBAR / RECHAZAR / PUBLICAR
    public String motivo;

    public DecisionPropuestaRequest() {}
    public DecisionPropuestaRequest(Integer id, String decision, String motivo) {
        this.id = id;
        this.decision = decision;
        this.motivo = motivo;
    }
}
