package es.uloyola.pdm.Alumni_android.model;

import java.util.List;

public class EventosResponse extends GenericResponse {
    private List<Evento> eventos;
    private Integer total;
    public List<Evento> getEventos() { return eventos; }
    public void setEventos(List<Evento> v) { this.eventos = v; }
    public Integer getTotal() { return total; }
    public void setTotal(Integer v) { this.total = v; }
}
