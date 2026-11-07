# Etapas 4 y 5 — Aplicación de SOLID, Clean Code y refactoring seguro

> Versión inicial: tag `estado-inicial`. Versión refactorizada: tag `etapa-refactoring` (commit `5e6d9e3`).

## 1. Criterio de trabajo

A partir del diagnóstico de la Etapa 3, elegimos cuatro refactorings que resuelven smells concretos e incluyen las tres técnicas exigidas por la consigna (Extract Method, Move Method e Introduce Parameter Object).

Para que cada cambio fuera **seguro**, seguimos estas reglas:

- **Un refactoring por rama y por Pull Request**, hechos en orden (R1 → R2 → R3 → R4) para no generar conflictos.
- **Pasos chicos:** cada refactoring cambia una sola cosa.
- **Verificación antes de aprobar:** después de cada cambio, el autor probó por consola las funcionalidades afectadas (capturas en `docs/evidencias/refactory/`).
- **No corregir bugs mientras se refactoriza:** los defectos E1 a E4 se conservaron a propósito, para que cualquier diferencia de comportamiento indicara un error del refactoring.

## 2. Refactorings aplicados

| # | Técnica | Smell que resuelve | Autor | Commit |
|---|---|---|---|---|
| R1 | **Extract Method** | CS-04 Duplicated Code | Uriel Palma | `c58664e` (PR #4) |
| R2 | **Move Method** | CS-10 Feature Envy | Lorenzo Pattini | `afec233` |
| R3 | **Introduce Parameter Object** | CS-03 Long Parameter List | Federico | `ff5fb74` (PR #5) |
| R4 | Replace Conditional with Polymorphism (enum) | CS-11 Switch Statements, CS-13 Magic Numbers y código muerto | Uriel Palma | `5e6d9e3` (PR #6) |

---

### R1 — Extract Method: `buscarPorId`

**Problema (CS-04):** `asignarResponsable()` y `cambiarEstado()` repetían el mismo bucle para encontrar una solicitud por su id.

**Antes** (repetido en los dos métodos):
```java
Solicitud sol = null;
for (Solicitud s : lista) {
    if (s.id == id) {
        sol = s;
        break;
    }
}
```

**Después:**
```java
private Solicitud buscarPorId(int id) {
    for (Solicitud s : lista) {
        if (s.id == id) {
            return s;
        }
    }
    return null;
}
```
```java
// en asignarResponsable() y en cambiarEstado():
Solicitud sol = buscarPorId(id);
```

**Por qué mejora:** la búsqueda queda en un solo lugar. Si cambia la forma de buscar (por ejemplo, al pasar a una base de datos), se modifica un único método. Además, `buscarPorId(id)` comunica la intención mejor que 7 líneas de bucle.

**Verificación:** asignar y cambiar estado con un id existente y uno inexistente (`r1-verificacion.png`).

---

### R2 — Move Method: `estaCerrada` y `estaVencidaAl`

**Problema (CS-10):** `SolicitudManager` consultaba los datos internos de `Solicitud` para decidir cosas que le corresponden a la propia solicitud.

**Antes** (en `SolicitudManager`):
```java
// asignarResponsable()
if (sol.estado.equals("RESUELTA") || sol.estado.equals("RECHAZADA")) {

// reporte()
if ((s.estado.equals("PENDIENTE") || s.estado.equals("EN_PROCESO")) && ref.isAfter(s.venc)) {
```

**Después** (en `Solicitud`):
```java
public boolean estaCerrada() {
    return estado.equals("RESUELTA") || estado.equals("RECHAZADA");
}

public boolean estaVencidaAl(LocalDate fecha) {
    return !estaCerrada() && fecha.isAfter(venc);
}
```
```java
// en SolicitudManager:
if (sol.estaCerrada()) {
...
if (s.estaVencidaAl(ref)) {
```

**Por qué mejora:** las reglas "qué es una solicitud cerrada" y "cuándo está vencida" quedan junto a los datos que usan. Si se agrega un estado nuevo (por ejemplo, `ANULADA`), se cambia solo `estaCerrada()`, y `estaVencidaAl()` se adapta sola porque la reutiliza.

**Por qué es seguro:** "sigue abierta" (no está cerrada) es equivalente a "está pendiente o en proceso", porque el sistema solo tiene esos cuatro estados.

**Verificación:** asignar a una solicitud cerrada y generar el reporte el día del vencimiento y después (`r2-solicitud de ejemplo generada.png`, `r2-verificacion de logica implementada.png`).

---

### R3 — Introduce Parameter Object: `DatosRegistroSolicitud`

**Problema (CS-03):** `registrar()` recibía 7 parámetros, 5 de ellos `String`, lo que facilitaba confundir el orden de los argumentos.

**Antes:**
```java
public int registrar(String legajo, String nombre, String email, String tipo,
                     String descripcion, boolean urgente, String fecha) {
```
```java
// Main
int id = m.registrar(leg, nom, mail, tipo, d, urg, fe);
```

**Después:**
```java
public record DatosRegistroSolicitud(
        String legajo,
        String nombre,
        String email,
        String tipo,
        String descripcion,
        boolean urgente,
        String fecha) {
}
```
```java
public int registrar(DatosRegistroSolicitud datos) {
    String legajo = datos.legajo();
    String nombre = datos.nombre();
    // ... el resto del metodo no cambia
```
```java
// Main
int id = m.registrar(new DatosRegistroSolicitud(leg, nom, mail, tipo, d, urg, fe));
```

**Por qué mejora:** los datos que siempre viajan juntos quedan agrupados en un objeto con nombre. Si mañana se agrega un dato (por ejemplo, un teléfono), se agrega al objeto sin cambiar la firma del método. Usamos un `record` porque es una clase de solo datos: Java genera el constructor y los métodos de acceso.

**Por qué es seguro:** el cuerpo del método no cambió; solo cambió la forma en que recibe los datos.

**Verificación:** registro válido y búsqueda por legajo, para comprobar que cada dato llegó a su lugar, más las validaciones de legajo, email, tipo y fecha (`r3-solicitudregistrada.png`, `r3-consultaporlegajo.png`, `r3-legajoinvalido.png`, `r3-emailinvalido.png`, `r3-tiposolicitudinvalido.png`, `r3-fechainvalida.png`).

---

### R4 — Replace Conditional con enum: `TipoSolicitud`

**Problema (CS-11 y CS-13):** las reglas de cada tipo estaban en un `switch` con números sin nombre, y el caso `default` nunca se ejecutaba.

**Antes:**
```java
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
        // ... dos casos mas ...
    default:
        dias = 10;      // nunca se ejecuta
        p = "BAJA";
}
```

**Después:**
```java
public enum TipoSolicitud {
    CERTIFICADO(3, "MEDIA", "ALTA"),
    EQUIVALENCIA(15, "BAJA", "MEDIA"),
    CAMBIO_COMISION(5, "MEDIA", "ALTA"),
    RECLAMO_NOTA(7, "ALTA", "ALTA");
    // diasPlazo, prioridadNormal, prioridadUrgente
    // getDiasPlazo(), prioridadPara(urgente), existe(nombre)
}
```
```java
// en registrar():
if (!TipoSolicitud.existe(tipo)) { ... }

TipoSolicitud tipoSolicitud = TipoSolicitud.valueOf(tipo);
int dias = tipoSolicitud.getDiasPlazo();
String p = tipoSolicitud.prioridadPara(urgente);
```

**Por qué mejora:** cada tipo "sabe" su plazo y su prioridad, así que no hace falta preguntar con un `switch`. Las reglas de negocio quedan en una tabla fácil de leer y se elimina el código muerto. La validación del tipo también usa el enum, en lugar de repetir los cuatro textos.

**Por qué es seguro:** `valueOf(tipo)` nunca falla porque la validación anterior ya rechazó cualquier tipo inexistente. El listado de las 4 solicitudes de prueba muestra las mismas prioridades y vencimientos que en la versión inicial.

**Verificación:** registro de los 4 tipos comparado con la captura `3 - listado-pendientes.png` de la versión inicial, y un tipo inválido (`r4-verificacion.png`).

## 3. Principios SOLID y Clean Code aplicados

| Principio | Cómo se aplicó | Refactoring |
|---|---|---|
| **SRP** (responsabilidad única) | `Solicitud` pasa a responder por sus propias reglas de estado y vencimiento, y `TipoSolicitud` por las reglas de cada tipo. `SolicitudManager` delega esas decisiones en lugar de resolverlas él. | R2, R4 |
| **OCP** (abierto/cerrado) | Para cambiar el plazo o la prioridad de un tipo ya no se modifica la lógica de `registrar()`: se cambia la constante del enum. | R4 |
| **DRY** (no repetirse) | Se eliminó el bucle de búsqueda duplicado y la lista de tipos repetida en la validación. | R1, R4 |
| **Nombres que revelan intención** | `buscarPorId`, `estaCerrada`, `estaVencidaAl`, `prioridadPara`, `getDiasPlazo` explican qué hacen sin leer su código. | R1, R2, R4 |
| **Funciones con menos parámetros** | `registrar()` pasó de 7 parámetros a 1. | R3 |
| **Sin magic numbers ni código muerto** | Los plazos 3, 15, 5 y 7 tienen un lugar y un significado; se eliminó el `default` inalcanzable. | R4 |

## 4. Resultado y deuda pendiente

| Smell | Estado |
|---|---|
| CS-03 Long Parameter List | ✅ Resuelto (R3) |
| CS-04 Duplicated Code | 🟡 Parcial: se eliminó la búsqueda por id duplicada (R1); los tres métodos de búsqueda siguen repitiendo su bucle |
| CS-10 Feature Envy | 🟡 Parcial: estado cerrado y vencimiento se movieron a `Solicitud` (R2); las transiciones de estado siguen en el manager |
| CS-11 Switch Statements | 🟡 Parcial: se eliminó el `switch` (R4); el menú de `Main` y los contadores del reporte todavía listan los tipos a mano, así que agregar un tipo requiere 3 cambios en lugar de 5 |
| CS-13 Magic Numbers y código muerto | ✅ Resuelto para los plazos y el `default` (R4) |
| CS-01, CS-02, CS-05, CS-06, CS-07, CS-08, CS-09, CS-12 | ⏳ Pendientes: quedan como mejoras futuras |
| CS-14 Ausencia de pruebas | 🟡 Parcial: se definieron casos de prueba manuales (Etapa 6); falta automatizarlos |

Elegimos refactorings **chicos y verificables** antes que una reestructuración completa: cada uno mejora un aspecto concreto sin poner en riesgo el comportamiento. Los smells pendientes requieren cambios más grandes, como separar `SolicitudManager` en servicios o sacar los `System.out` de la lógica de negocio. Quedan priorizados para una próxima iteración, siguiendo el orden de la Etapa 3.
