package ar.utn.solicitudes;

// Tipos de solicitud con sus reglas de negocio: plazo en dias y prioridad.
// Antes estas reglas estaban en un switch dentro de SolicitudManager.registrar(...).
public enum TipoSolicitud {
    CERTIFICADO(3, "MEDIA", "ALTA"),
    EQUIVALENCIA(15, "BAJA", "MEDIA"),
    CAMBIO_COMISION(5, "MEDIA", "ALTA"),
    RECLAMO_NOTA(7, "ALTA", "ALTA");

    private final int diasPlazo;
    private final String prioridadNormal;
    private final String prioridadUrgente;

    TipoSolicitud(int diasPlazo, String prioridadNormal, String prioridadUrgente) {
        this.diasPlazo = diasPlazo;
        this.prioridadNormal = prioridadNormal;
        this.prioridadUrgente = prioridadUrgente;
    }

    public int getDiasPlazo() {
        return diasPlazo;
    }

    public String prioridadPara(boolean urgente) {
        return urgente ? prioridadUrgente : prioridadNormal;
    }

    // Indica si el texto corresponde a alguno de los tipos validos.
    public static boolean existe(String nombre) {
        for (TipoSolicitud tipo : values()) {
            if (tipo.name().equals(nombre)) {
                return true;
            }
        }
        return false;
    }
}