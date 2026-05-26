package es.loyola.classes;

import es.loyola.enums.ExperienciaHobbies;

public class HobbieImpl implements Hobbie {
    private String nombre;
    private Integer veces;
    private ExperienciaHobbies experiencia;

    public HobbieImpl(String nombre, Integer veces, ExperienciaHobbies experiencia) {
        this.nombre = nombre;
        this.veces = veces;
        this.experiencia = experiencia;
    }

    public String getNombreHobbie() {
        return nombre;
    }

    public void setNombreHobbie(String nombre) {
        this.nombre = nombre;
    }

    public Integer getVecesXSemana() {
        return veces;
    }

    public void setVecesXSemana(Integer veces) {
        this.veces = veces;
    }

    public ExperienciaHobbies getExperiencia() {
        return experiencia;
    }

    public void setExperiencia(ExperienciaHobbies experiencia) {
        this.experiencia = experiencia;
    }

    public void agregar() {
    }

    public void actualizar(String nombre) {
        this.nombre = nombre;
    }

    public void eliminar() {
        this.nombre = "";
        this.veces = 0;
        this.experiencia = null;
    }
}
