package ar.utn.solicitudes;

import java.io.FileWriter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

// Clase principal que maneja todo lo de las solicitudes
public class SolicitudManager {

    private ArrayList<Solicitud> lista = new ArrayList<>();
    private int cont = 1;
    private DateTimeFormatter f = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // registra una solicitud nueva, devuelve el id o -1 si hubo error
    public int registrar(String legajo, String nombre, String email, String tipo, String descripcion,
                         boolean urgente, String fecha) {
        // validaciones
        if (legajo == null || legajo.trim().equals("")) {
            System.out.println("Error: el legajo es obligatorio");
            return -1;
        }
        if (!legajo.matches("\\d{5,6}")) {
            System.out.println("Error: legajo invalido");
            return -1;
        }
        if (nombre == null || nombre.trim().equals("")) {
            System.out.println("Error: el nombre es obligatorio");
            return -1;
        }
        if (email == null || !email.contains("@")) {
            System.out.println("Error: email invalido");
            return -1;
        }
        if (!tipo.equals("CERTIFICADO") && !tipo.equals("EQUIVALENCIA") && !tipo.equals("CAMBIO_COMISION")
                && !tipo.equals("RECLAMO_NOTA")) {
            System.out.println("Error: tipo de solicitud invalido");
            return -1;
        }
        if (descripcion == null || descripcion.trim().length() < 10) {
            System.out.println("Error: la descripcion debe tener al menos 10 caracteres");
            return -1;
        }
        LocalDate fec;
        try {
            fec = LocalDate.parse(fecha, f);
        } catch (Exception e) {
            System.out.println("Error: fecha invalida");
            return -1;
        }

        // calcular prioridad y vencimiento segun el tipo
        String p;
        int dias;
        switch (tipo) {
            case "CERTIFICADO":
                dias = 3;
                if (urgente) p = "ALTA";
                else p = "MEDIA";
                break;
            case "EQUIVALENCIA":
                dias = 15;
                if (urgente) p = "MEDIA";
                else p = "BAJA";
                break;
            case "CAMBIO_COMISION":
                dias = 5;
                if (urgente) p = "ALTA";
                else p = "MEDIA";
                break;
            case "RECLAMO_NOTA":
                dias = 7;
                p = "ALTA";
                break;
            default:
                dias = 10;
                p = "BAJA";
        }

        // buscar si el estudiante ya existe para reutilizarlo
        Estudiante e = null;
        for (Solicitud s : lista) {
            if (s.est.getLegajo().equals(legajo)) {
                e = s.est;
                break;
            }
        }
        if (e == null) {
            e = new Estudiante(legajo, nombre, email);
        }

        Solicitud s = new Solicitud();
        s.id = cont;
        cont++;
        s.est = e;
        s.tipo = tipo;
        s.desc = descripcion;
        s.estado = "PENDIENTE";
        s.prioridad = p;
        s.fecha = fec;
        s.venc = fec.plusDays(dias);
        lista.add(s);
        return s.id;
    }

    public boolean asignarResponsable(int id, String nombreResp, String area) {
                Solicitud sol = buscarPorId(id);
    
        if (sol == null) {
            System.out.println("Error: solicitud no encontrada");
            return false;
        }
        if (sol.estado.equals("RESUELTA") || sol.estado.equals("RECHAZADA")) {
            System.out.println("Error: la solicitud esta cerrada");
            return false;
        }
        if (nombreResp == null || nombreResp.trim().isEmpty()) {
            System.out.println("Error: el responsable es obligatorio");
            return false;
        }
        sol.resp = new Responsable(nombreResp, area);
        return true;
    }

    // cambia el estado de la solicitud validando las transiciones
    public boolean cambiarEstado(int id, String nuevo, String obs) {
                Solicitud sol = buscarPorId(id);
                
        if (sol == null) {
            System.out.println("Error: solicitud no encontrada");
            return false;
        }
        if (sol.estado.equals("PENDIENTE")) {
            if (nuevo.equals("EN_PROCESO")) {
                if (sol.resp == null) {
                    System.out.println("Error: debe asignar un responsable antes de iniciar el tramite");
                    return false;
                }
                sol.estado = nuevo;
            } else if (nuevo.equals("RECHAZADA")) {
                if (obs == null || obs.trim().isEmpty()) {
                    System.out.println("Error: debe indicar el motivo del rechazo");
                    return false;
                }
                sol.estado = nuevo;
                sol.obs = obs;
            } else {
                System.out.println("Error: transicion de estado no permitida");
                return false;
            }
        } else if (sol.estado.equals("EN_PROCESO")) {
            if (nuevo.equals("RESUELTA")) {
                sol.estado = nuevo;
                sol.obs = obs;
            } else if (nuevo.equals("RECHAZADA")) {
                if (obs == null || obs.trim().isEmpty()) {
                    System.out.println("Error: debe indicar el motivo del rechazo");
                    return false;
                }
                sol.estado = nuevo;
                sol.obs = obs;
            } else {
                System.out.println("Error: transicion de estado no permitida");
                return false;
            }
        } else {
            System.out.println("Error: transicion de estado no permitida");
            return false;
        }
        return true;
    }

    public ArrayList<Solicitud> buscarPorEstado(String estado) {
        ArrayList<Solicitud> res = new ArrayList<>();
        for (Solicitud s : lista) {
            if (s.estado.equals(estado.toUpperCase())) {
                res.add(s);
            }
        }
        return res;
    }

    private Solicitud buscarPorId(int id) {
        for (Solicitud s : lista) {
            if (s.id == id) {
                return s;
            }
        }
        return null;
    }
    
    public ArrayList<Solicitud> buscarPorTipo(String tipo) {
        ArrayList<Solicitud> res = new ArrayList<>();
        for (Solicitud s : lista) {
            if (s.tipo.equals(tipo.toUpperCase())) {
                res.add(s);
            }
        }
        return res;
    }

    public ArrayList<Solicitud> buscarPorLegajo(String legajo) {
        ArrayList<Solicitud> res = new ArrayList<>();
        for (Solicitud s : lista) {
            if (s.est.getLegajo().equals(legajo)) {
                res.add(s);
            }
        }
        return res;
    }

    public void imprimirLista(ArrayList<Solicitud> l) {
        if (l.size() == 0) {
            System.out.println("No se encontraron solicitudes.");
            return;
        }
        for (Solicitud s : l) {
            String r;
            if (s.resp == null) r = "(sin asignar)";
            else r = s.resp.getNombre() + " (" + s.resp.getArea() + ")";
            System.out.println("#" + s.id + " [" + s.estado + "] " + s.tipo + " - " + s.prioridad + " - "
                    + s.est.getLegajo() + " " + s.est.getNombre() + " - vence " + s.venc.format(f)
                    + " - resp: " + r);
        }
    }

    // genera el reporte general
    public void reporte(String fechaRef) {
        LocalDate ref;
        try {
            ref = LocalDate.parse(fechaRef, f);
        } catch (Exception e) {
            System.out.println("Error: fecha invalida");
            return;
        }
        if (lista.size() == 0) {
            System.out.println("No hay solicitudes registradas.");
            return;
        }
        int pend = 0, proc = 0, res = 0, rech = 0;
        int cert = 0, equiv = 0, com = 0, recl = 0;
        ArrayList<Solicitud> vencidas = new ArrayList<>();
        for (Solicitud s : lista) {
            if (s.estado.equals("PENDIENTE")) pend++;
            else if (s.estado.equals("EN_PROCESO")) proc++;
            else if (s.estado.equals("RESUELTA")) res++;
            else if (s.estado.equals("RECHAZADA")) rech++;

            if (s.tipo.equals("CERTIFICADO")) cert++;
            else if (s.tipo.equals("EQUIVALENCIA")) equiv++;
            else if (s.tipo.equals("CAMBIO_COMISION")) com++;
            else if (s.tipo.equals("RECLAMO_NOTA")) recl++;

            if ((s.estado.equals("PENDIENTE") || s.estado.equals("EN_PROCESO")) && ref.isAfter(s.venc)) {
                vencidas.add(s);
            }
        }
        System.out.println("=== Reporte al " + ref.format(f) + " ===");
        System.out.println("Total de solicitudes: " + lista.size());
        System.out.println("Por estado:");
        System.out.println("  PENDIENTE: " + pend);
        System.out.println("  EN_PROCESO: " + proc);
        System.out.println("  RESUELTA: " + res);
        System.out.println("  RECHAZADA: " + rech);
        System.out.println("Por tipo:");
        System.out.println("  CERTIFICADO: " + cert);
        System.out.println("  EQUIVALENCIA: " + equiv);
        System.out.println("  CAMBIO_COMISION: " + com);
        System.out.println("  RECLAMO_NOTA: " + recl);
        System.out.println("Vencidas sin resolver: " + vencidas.size());
        for (Solicitud s : vencidas) {
            System.out.println("  #" + s.id + " " + s.tipo + " - vencio el " + s.venc.format(f));
        }
        double x = Math.round(res * 1000.0 / lista.size()) / 10.0;
        System.out.println("Porcentaje resueltas: " + x + "%");
    }

    public void exportarCsv(String ruta) {
        try {
            FileWriter fw = new FileWriter(ruta);
            fw.write("id,legajo,nombre,tipo,estado,prioridad,fecha,vencimiento,responsable,observacion\n");
            for (Solicitud s : lista) {
                fw.write(s.id + "," + s.est.getLegajo() + "," + s.est.getNombre() + "," + s.tipo + "," + s.estado
                        + "," + s.prioridad + "," + s.fecha.format(f) + "," + s.venc.format(f) + ","
                        + (s.resp == null ? "" : s.resp.getNombre()) + "," + (s.obs == null ? "" : s.obs) + "\n");
            }
            fw.close();
            System.out.println("Se exportaron " + lista.size() + " solicitudes.");
        } catch (Exception e) {
            // TODO: manejar error
        }
    }
}
