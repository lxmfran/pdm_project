package es.uloyola.pdm.Alumni_android.model;

import java.util.List;

public class PropuestasResponse extends GenericResponse {
    private List<Propuesta> propuestas;
    private Integer total;
    public List<Propuesta> getPropuestas() { return propuestas; }
    public void setPropuestas(List<Propuesta> v) { this.propuestas = v; }
    public Integer getTotal() { return total; }
    public void setTotal(Integer v) { this.total = v; }
}
