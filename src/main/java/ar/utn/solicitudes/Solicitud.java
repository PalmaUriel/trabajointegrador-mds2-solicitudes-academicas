package ar.utn.solicitudes;

import java.time.LocalDate;

// clase que representa una solicitud
public class Solicitud {
    public int id;
    public Estudiante est;
    public String tipo; // CERTIFICADO, EQUIVALENCIA, CAMBIO_COMISION, RECLAMO_NOTA
    public String desc;
    public String estado; // PENDIENTE, EN_PROCESO, RESUELTA, RECHAZADA
    public String prioridad; // ALTA, MEDIA, BAJA
    public LocalDate fecha;
    public LocalDate venc;
    public Responsable resp;
    public String obs;
}
