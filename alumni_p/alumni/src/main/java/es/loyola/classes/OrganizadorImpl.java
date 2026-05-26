package es.loyola.classes;

public class OrganizadorImpl implements Organizador {
    private String nombre;
    private String id;

    public OrganizadorImpl(String nombre, String id) {
        this.nombre = nombre;
        this.id = id;
    }

    public String getNombreOrganizador() {
        return nombre;
    }

    public void setNombreOrganizador(String nombre) {
        this.nombre = nombre;
    }

    public String getIdentificador() {
        return id;
    }

    public void setIdentificador(String id) {
        this.id = id;
    }
}
