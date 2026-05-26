package es.loyola.classes;

public class LugarTrabajo {
    private String nombre;
    private String direccion;
    private String ciudad;

    public LugarTrabajo(String nombre, String direccion, String ciudad) {
        this.nombre = nombre;
        this.direccion = direccion;
        this.ciudad = ciudad;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getCiudad() {
        return ciudad;
    }

    public void setCiudad(String ciudad) {
        this.ciudad = ciudad;
    }
}
