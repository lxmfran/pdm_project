package es.loyola.classes;

import java.util.Date;
import java.util.List;

public interface Evento {

    String ESTADO_PUBLICADO = "PUBLICADO";
    String ESTADO_CANCELADO = "CANCELADO";
    String ESTADO_ELIMINADO = "ELIMINADO";
    String ESTADO_BORRADOR = "BORRADOR";

    Integer getId();
    void setId(Integer id);
    String getNombreEvento();
    void setNombreEvento(String nombre);
    Date getFechaAperturaInscripcion();
    void setFechaAperturaInscripcion(Date fecha);
    Date getFechaLimiteInscripcion();
    void setFechaLimiteInscripcion(Date fecha);
    String getDescripcionEvento();
    void setDescripcionEvento(String descripcion);
    String getLugarEvento();
    void setLugarEvento(String lugar);
    List<Alumni> getListaAlumni();
    void setListaAlumni(List<Alumni> lista);
    Date getFechaLimite();
    void setFechaLimite(Date fechaLimite);
    Date getFechaEvento();
    void setFechaEvento(Date fechaEvento);
    String getPonente();
    void setPonente(String ponente);
    Organizador getOrganizador();
    void setOrganizador(Organizador organizador);
    Integer getCapacidadMaxima();
    void setCapacidadMaxima(Integer capacidadMaxima);

    String getEstado();
    void setEstado(String estado);

    /** Identificador del propietario que publicó el evento (RF-8). */
    String getPropietario();
    void setPropietario(String propietario);

    void publicar();
    void modificar(String nombre, String descripcion, String lugar);
    void eliminar();
    void cancelarEvento();
}
