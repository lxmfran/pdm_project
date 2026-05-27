package es.uloyola.pdm.Alumni_android.model;

/** Body para POST/DELETE /InscripcionServlet. */
public class InscripcionRequest {
    public String tipo;   // "evento" | "actividad"
    public Integer id;

    public InscripcionRequest() {}
    public InscripcionRequest(String tipo, Integer id) {
        this.tipo = tipo;
        this.id = id;
    }
}
