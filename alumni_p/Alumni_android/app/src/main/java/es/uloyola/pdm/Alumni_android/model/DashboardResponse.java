package es.uloyola.pdm.Alumni_android.model;

import java.util.Map;

public class DashboardResponse extends GenericResponse {
    private Map<String, Integer> metricas;
    public Map<String, Integer> getMetricas() { return metricas; }
    public void setMetricas(Map<String, Integer> v) { this.metricas = v; }
}
