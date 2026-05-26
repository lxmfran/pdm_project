package es.loyola.classes;

import es.loyola.enums.Campus;
import es.loyola.enums.Facultad;

public interface Alumni extends Usuario {
    String getTitulacion();
    void setTitulacion(String titulacion);
    Integer getAnioGraduacion();
    void setAnioGraduacion(Integer anio);
    Integer getPromocion();
    void setPromocion(Integer anio);
    void setUsuario(String usuario);
    void setContrasenia(String contrasenia);
    Facultad getFacultad();
    void setFacultad(Facultad facultad);
    Campus getCampus();
    void setCampus(Campus campus);
    String getCiudadResidencia();
    void setCiudadResidencia(String ciudad);
    Trabajo getTrabajo();
    void setTrabajo(Trabajo trabajo);
    String getTrabajoActual();
    void setTrabajoActual(String trabajoActual);
    String getCiudad();
    void setCiudad(String ciudad);
    String getHobbies();
    void setHobbies(String hobbies);
    String getFotoPerfil();
    void setFotoPerfil(String fotoPerfil);
    Boolean getMostrarContacto();
    void setMostrarContacto(Boolean mostrarContacto);

    // Privacidad granular por campo (RF-11, RF-13, RN-2, RN-3, RN-4)
    Boolean getMostrarEmail();
    void setMostrarEmail(Boolean valor);
    Boolean getMostrarTelefono();
    void setMostrarTelefono(Boolean valor);
    Boolean getMostrarCiudad();
    void setMostrarCiudad(Boolean valor);
    Boolean getMostrarTrabajo();
    void setMostrarTrabajo(Boolean valor);
    Boolean getMostrarHobbies();
    void setMostrarHobbies(Boolean valor);

    void buscarAlumni();
    void inscribirseEvento();
    void modificarDatos();
}
