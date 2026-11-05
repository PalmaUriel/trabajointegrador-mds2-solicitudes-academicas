package ar.utn.solicitudes;

import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        SolicitudManager m = new SolicitudManager();
        boolean flag = true;
        while (flag) {
            System.out.println();
            System.out.println("=== Sistema de Gestion de Solicitudes Academicas ===");
            System.out.println("1. Registrar solicitud");
            System.out.println("2. Asignar responsable");
            System.out.println("3. Cambiar estado");
            System.out.println("4. Buscar solicitudes");
            System.out.println("5. Reporte");
            System.out.println("6. Exportar CSV");
            System.out.println("0. Salir");
            System.out.print("Opcion: ");
            String op = sc.nextLine();
            switch (op) {
                case "1":
                    System.out.print("Legajo: ");
                    String leg = sc.nextLine();
                    System.out.print("Nombre: ");
                    String nom = sc.nextLine();
                    System.out.print("Email: ");
                    String mail = sc.nextLine();
                    System.out.print("Tipo (1=Certificado, 2=Equivalencia, 3=Cambio de comision, 4=Reclamo de nota): ");
                    String t = sc.nextLine();
                    String tipo;
                    if (t.equals("1")) tipo = "CERTIFICADO";
                    else if (t.equals("2")) tipo = "EQUIVALENCIA";
                    else if (t.equals("3")) tipo = "CAMBIO_COMISION";
                    else if (t.equals("4")) tipo = "RECLAMO_NOTA";
                    else tipo = "X";
                    System.out.print("Descripcion: ");
                    String d = sc.nextLine();
                    System.out.print("Es urgente? (s/n): ");
                    boolean urg = sc.nextLine().equalsIgnoreCase("s");
                    System.out.print("Fecha (dd/mm/aaaa): ");
                    String fe = sc.nextLine();
                    int id = m.registrar(leg, nom, mail, tipo, d, urg, fe);
                    if (id != -1) {
                        System.out.println("Solicitud registrada con id " + id);
                    }
                    break;
                case "2":
                    System.out.print("Id de solicitud: ");
                    int id2 = Integer.parseInt(sc.nextLine());
                    System.out.print("Nombre del responsable: ");
                    String r = sc.nextLine();
                    System.out.print("Area: ");
                    String a = sc.nextLine();
                    if (m.asignarResponsable(id2, r, a)) {
                        System.out.println("Responsable asignado.");
                    }
                    break;
                case "3":
                    System.out.print("Id de solicitud: ");
                    int id3 = Integer.parseInt(sc.nextLine());
                    System.out.print("Nuevo estado (1=En proceso, 2=Resuelta, 3=Rechazada): ");
                    String e = sc.nextLine();
                    String est;
                    if (e.equals("1")) est = "EN_PROCESO";
                    else if (e.equals("2")) est = "RESUELTA";
                    else if (e.equals("3")) est = "RECHAZADA";
                    else est = "X";
                    System.out.print("Observacion: ");
                    String o = sc.nextLine();
                    if (m.cambiarEstado(id3, est, o)) {
                        System.out.println("Estado actualizado.");
                    }
                    break;
                case "4":
                    System.out.print("Buscar por (1=Estado, 2=Tipo, 3=Legajo): ");
                    String b = sc.nextLine();
                    System.out.print("Valor: ");
                    String v = sc.nextLine();
                    if (b.equals("1")) m.imprimirLista(m.buscarPorEstado(v));
                    else if (b.equals("2")) m.imprimirLista(m.buscarPorTipo(v));
                    else if (b.equals("3")) m.imprimirLista(m.buscarPorLegajo(v));
                    else System.out.println("Opcion invalida");
                    break;
                case "5":
                    System.out.print("Fecha de referencia (dd/mm/aaaa): ");
                    m.reporte(sc.nextLine());
                    break;
                case "6":
                    System.out.print("Archivo destino: ");
                    m.exportarCsv(sc.nextLine());
                    break;
                case "0":
                    flag = false;
                    System.out.println("Hasta luego!");
                    break;
                default:
                    System.out.println("Opcion invalida");
            }
        }
    }
}
