package es.uloyola.pdm.Alumni_android.model;

import java.util.List;

public class ActividadesResponse extends GenericResponse {
    private List<Evento> actividades;
    private Integer total;
    public List<Evento> getActividades() { return actividades; }
    public void setActividades(List<Evento> v) { this.actividades = v; }
    public Integer getTotal() { return total; }
    public void setTotal(Integer v) { this.total = v; }
}
