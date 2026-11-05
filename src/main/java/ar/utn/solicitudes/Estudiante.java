package ar.utn.solicitudes;

public class Estudiante {
    private String legajo;
    private String nombre;
    private String email;

    public Estudiante(String legajo, String nombre, String email) {
        this.legajo = legajo;
        this.nombre = nombre;
        this.email = email;
    }

    public String getLegajo() {
        return legajo;
    }

    public String getNombre() {
        return nombre;
    }

    public String getEmail() {
        return email;
    }
}
