package es.uloyola.pdm.Alumni_android.model;

import java.util.List;

public class BuscarAlumniResponse extends GenericResponse {
    private List<AlumniResumen> resultados;
    private Integer total;
    private Integer page;
    private Integer size;

    public List<AlumniResumen> getResultados() { return resultados; }
    public void setResultados(List<AlumniResumen> v) { this.resultados = v; }
    public Integer getTotal() { return total; }
    public void setTotal(Integer v) { this.total = v; }
    public Integer getPage() { return page; }
    public void setPage(Integer v) { this.page = v; }
    public Integer getSize() { return size; }
    public void setSize(Integer v) { this.size = v; }

    public boolean hayMasPaginas() {
        if (page == null || size == null || total == null) return false;
        return page * size < total;
    }
}
