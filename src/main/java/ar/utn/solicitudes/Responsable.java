package ar.utn.solicitudes;

public class Responsable {
    private String nombre;
    private String area;

    public Responsable(String nombre, String area) {
        this.nombre = nombre;
        this.area = area;
    }

    public String getNombre() {
        return nombre;
    }

    public String getArea() {
        return area;
    }
}
