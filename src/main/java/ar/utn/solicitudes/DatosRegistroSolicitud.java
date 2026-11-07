package ar.utn.solicitudes;

public record DatosRegistroSolicitud(
            String legajo,
            String nombre,
            String email,
            String tipo,
            String descripcion,
            boolean urgente,
            String fecha){
            }

