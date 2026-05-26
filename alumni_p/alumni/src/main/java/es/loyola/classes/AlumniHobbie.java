package es.loyola.classes;

public class AlumniHobbie {
    private Integer frecuenciaSemanal;
    private String nivelExperiencia;

    public AlumniHobbie(Integer frecuenciaSemanal, String nivelExperiencia) {
        this.frecuenciaSemanal = frecuenciaSemanal;
        this.nivelExperiencia = nivelExperiencia;
    }

    public Integer getFrecuenciaSemanal() {
        return frecuenciaSemanal;
    }

    public void setFrecuenciaSemanal(Integer frecuenciaSemanal) {
        this.frecuenciaSemanal = frecuenciaSemanal;
    }

    public String getNivelExperiencia() {
        return nivelExperiencia;
    }

    public void setNivelExperiencia(String nivelExperiencia) {
        this.nivelExperiencia = nivelExperiencia;
    }
}
