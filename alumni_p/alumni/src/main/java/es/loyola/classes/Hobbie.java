package es.loyola.classes;

import es.loyola.enums.ExperienciaHobbies;

public interface Hobbie {
    String getNombreHobbie();
    void setNombreHobbie(String nombre);
    Integer getVecesXSemana();
    void setVecesXSemana(Integer veces);
    ExperienciaHobbies getExperiencia();
    void setExperiencia(ExperienciaHobbies experiencia);
    void agregar();
    void actualizar(String nombre);
    void eliminar();
}
