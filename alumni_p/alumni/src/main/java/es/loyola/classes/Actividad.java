package es.loyola.classes;

import es.loyola.enums.ExperienciaHobbies;
import java.util.Date;
import java.util.List;

public interface Actividad {

    String ESTADO_PUBLICADA = "PUBLICADA";
    String ESTADO_CANCELADA = "CANCELADA";
    String ESTADO_ELIMINADA = "ELIMINADA";
    String ESTADO_BORRADOR = "BORRADOR";

    Integer getId();
    void setId(Integer id);
    String getLugarActividad();
    void setLugarActividad(String lugar);
    String getNombreActividad();
    void setNombreActividad(String nombre);
    String getDescripcion();
    void setDescripcion(String descripcion);
    Date getFecha();
    void setFecha(Date fecha);
    String getOrganizador();
    void setOrganizador(String organizador);
    Hobbie getHobbiePracticado();
    void setHobbiePracticado(Hobbie hobbie);
    ExperienciaHobbies getNivelParticipacion();
    void setNivelParticipacion(ExperienciaHobbies experiencia);
    String getNivelMedioParticipacion();
    void setNivelMedioParticipacion(String nivel);
    Date getFechaApertura();
    void setFechaApertura(Date fechaApertura);
    Date getFechaAperturaInscripcion();
    void setFechaAperturaInscripcion(Date fechaApertura);
    List<Alumni> getAlumniInscritos();
    void setAlumniInscritos(List<Alumni> lista);
    Date getFechaLimiteActividad();
    void setFechaLimiteActividad(Date fechaLimiteActividad);
    Date getFechaLimiteInscripcion();
    void setFechaLimiteInscripcion(Date fechaLimiteInscripcion);
    Integer getMaxPlazas();
    void setMaxPlazas(Integer plazas);
    Integer getPlazasLimites();
    void setPlazasLimites(Integer plazas);
    Organizador getOrganizadorActividad();
    void setOrganizadorActividad(Organizador organizador);
    String getEstado();
    void setEstado(String estado);
    String getPropietario();
    void setPropietario(String propietario);

    void publicar();
    void modificar(String nombre, String descripcion, String lugar);
    void eliminar();
    void cancelar();
}
