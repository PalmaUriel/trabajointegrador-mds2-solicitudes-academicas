# Etapa 1 — Descripción del sistema base

> Documento de la Etapa 1 del TPI de Metodología de Sistemas II.
> Redactado en primera persona del plural, para que coincida con lo que el equipo va a defender oralmente.

---

## 1. Dominio y problema

El sistema pertenece al dominio de **gestión académica**. Resuelve un problema acotado: cuando un estudiante necesita hacer un trámite (certificado, equivalencia, cambio de comisión o reclamo de nota), hoy no existe un canal único que registre el pedido, lo asigne a un responsable y permita seguir su estado. Nuestro sistema base propone un **registro centralizado de solicitudes académicas**, con la posibilidad de asignar un responsable y consultar el avance.

El sistema está pensado para **una oficina de alumnos** que recibe y clasifica pedidos, y para el **estudiante** que necesita hacer el seguimiento. No está pensado para el estudiante final como usuario directo (no hay login), sino como un sistema operado por el personal administrativo a partir de los datos que el estudiante aporta.

---

## 2. Alcance

**Qué hace el sistema (versión inicial):**

- Registra solicitudes académicas con datos del estudiante, tipo, descripción y fecha.
- Calcula automáticamente **prioridad** y **fecha de vencimiento** según el tipo y el carácter urgente.
- Permite asignar un **responsable** (nombre y área) a una solicitud abierta.
- Permite **cambiar el estado** de una solicitud respetando ciertas transiciones.
- Permite **buscar** solicitudes por estado, tipo o legajo.
- Genera un **reporte** por estado, por tipo, con cantidad de vencidas y porcentaje de resueltas.
- **Exporta** todas las solicitudes a un archivo CSV.

**Qué NO hace (y conviene dejar explícito para no sobredimensionar el alcance):**

- **No tiene base de datos.** Los datos viven en una `ArrayList` en memoria y se pierden al cerrar el programa.
- **No tiene login ni usuarios.** No hay autenticación, ni roles, ni permisos. Cualquier persona que ejecute el programa opera como administrador.
- **No edita ni elimina solicitudes** una vez registradas. Solo se puede cambiar el estado y asignar responsable.
- **No guarda historial de estados.** Solo se conserva la última observación cargada al cambiar de estado.
- **No valida el área** del responsable contra un catálogo; cualquier texto es aceptado.
- **No notifica** al estudiante ni al responsable por ningún canal (no hay mail, ni evento, ni alerta).
- **No tiene tests automatizados** en la versión inicial, aunque el `pom.xml` declara JUnit 5.
- **No separa capas**: la lógica de negocio, la validación, el reporte y la exportación conviven en una sola clase (`SolicitudManager`).

Estas ausencias no son “falencias” a corregir en esta etapa: son el alcance real del sistema base del TPI. Lo que sí vamos a diagnosticar y corregir en etapas posteriores es **cómo está construido**, no cuántas funcionalidades tiene.

---

## 3. Actores

| Actor | Rol en el sistema | Qué hace en la versión inicial |
|---|---|---|
| **Estudiante** | Origen del pedido | No interactúa con el sistema. Sus datos (legajo, nombre, email) los ingresa el operador al registrar la solicitud. |
| **Personal de la oficina de alumnos (operador)** | Usuario principal | Ejecuta el menú: registra, asigna, cambia estados, busca, genera reportes y exporta. |
| **Responsable / Área** | Quien resuelve la solicitud | No interactúa con el sistema. Se lo asigna a una solicitud y queda como dato. |
| **Administrador del repositorio** | Rol técnico (fuera del alcance funcional) | Ejecuta el sistema desde consola con Maven. |

Importante: en el estado inicial **no hay distinción de permisos**. El “operador” y el “administrador” son la misma persona desde el punto de vista del código.

---

## 4. Entidades del modelo

| Clase | Qué representa | Atributos principales |
|---|---|---|
| `Estudiante` | Persona que origina la solicitud. | `legajo`, `nombre`, `email`. |
| `Responsable` | Persona o área que atiende la solicitud. | `nombre`, `area`. |
| `Solicitud` | El pedido académico en sí. Es la entidad central. | `id`, `est`, `tipo`, `desc`, `estado`, `prioridad`, `fecha`, `venc`, `resp`, `obs`. |

**Observación honesta sobre el modelo:** en la versión inicial, `Solicitud` tiene **todos sus campos públicos** (`public int id; public Estudiante est; …`). Esto significa que cualquier clase puede modificar el estado, la prioridad o el vencimiento de una solicitud sin pasar por ninguna validación. Lo dejamos registrado como hallazgo para la etapa de diagnóstico; en esta etapa solo lo describimos.

Además, `tipo`, `estado` y `prioridad` son `String`, no enums. Los valores válidos (`CERTIFICADO`, `EQUIVALENCIA`, `CAMBIO_COMISION`, `RECLAMO_NOTA` / `PENDIENTE`, `EN_PROCESO`, `RESUELTA`, `RECHAZADA` / `ALTA`, `MEDIA`, `BAJA`) están **hardcodeados** en varios lugares.

---

## 5. Funcionalidades (las 6 opciones del menú)

### 5.1. Registrar solicitud

Pide: legajo, nombre, email, tipo (1 a 4), descripción, si es urgente (s/n) y fecha en `dd/mm/aaaa`.

Reglas que descubrimos al usarla:

- **Legajo:** obligatorio, y debe tener **5 o 6 dígitos** (`\\d{5,6}`). Si tiene letras o menos dígitos, rechaza.
- **Nombre:** obligatorio, no vacío.
- **Email:** obligatorio, y debe contener al menos un `@`. (No valida formato completo.)
- **Descripción:** mínimo **10 caracteres**.
- **Fecha:** debe respetar el formato `dd/MM/yyyy`. Si no, rechaza con “fecha inválida”.
- **Tipo:** los valores 1 a 4 se mapean a los cuatro tipos válidos. **Si el operador ingresa cualquier otro valor, el sistema no rechaza la solicitud: la crea con tipo `"X"`.** Esto lo anotamos como hallazgo, porque a simple vista parece un caso borde no contemplado.
- **Prioridad y vencimiento:** se calculan automáticamente (ver sección 6).
- **Reutilización de estudiante:** si ya existe una solicitud con el mismo legajo, **reutiliza el objeto `Estudiante`** en lugar de crear uno nuevo. Si no, lo crea.
- **Estado inicial:** toda solicitud nueva arranca en `PENDIENTE`.

### 5.2. Asignar responsable

Pide id de solicitud, nombre del responsable y área.

Reglas:

- Si el id no existe → error “solicitud no encontrada”.
- Si la solicitud está `RESUELTA` o `RECHAZADA` → error “la solicitud está cerrada”.
- El nombre del responsable es obligatorio. El área no se valida.
- Se crea un `Responsable(nombre, area)` y se asigna a la solicitud.

### 5.3. Cambiar estado

Pide id de solicitud, nuevo estado (1 = En proceso, 2 = Resuelta, 3 = Rechazada) y una observación.

Reglas que descubrimos (son las más importantes del sistema):

- **Desde `PENDIENTE`:**
    - Solo se puede pasar a `EN_PROCESO` si la solicitud **ya tiene responsable**. Si no, error: *“debe asignar un responsable antes de iniciar el trámite”*.
    - Se puede pasar a `RECHAZADA` **solo si se carga una observación** (motivo del rechazo).
    - Cualquier otra transición → error.
- **Desde `EN_PROCESO`:**
    - Se puede pasar a `RESUELTA` (la observación es opcional).
    - Se puede pasar a `RECHAZADA` **solo con observación**.
    - Cualquier otra transición → error.
- **Desde `RESUELTA` o `RECHAZADA`:** cualquier cambio → error (la solicitud está cerrada).
- Si el operador ingresa un estado distinto de 1, 2 o 3, el `Main` lo mapea a `"X"` y el manager rechaza por transición no permitida.

Este es el punto donde más se nota el acoplamiento entre `Main` (que traduce strings) y `SolicitudManager` (que decide las reglas).

### 5.4. Buscar solicitudes

Pide criterio (1 = Estado, 2 = Tipo, 3 = Legajo) y valor.

Reglas:

- Por estado: se compara contra el estado en mayúsculas (`estado.toUpperCase()`). Si no hay coincidencias → “No se encontraron solicitudes”.
- Por tipo: idem, contra el tipo en mayúsculas.
- Por legajo: compara string exacto.
- Si el criterio es inválido → “Opción inválida”.
- **Limitación conocida:** la búsqueda por legajo compara exacto, así que si el operador ingresa `12345` y la solicitud tiene `012345`, no la encuentra.

### 5.5. Reporte

Pide una fecha de referencia en `dd/mm/aaaa`.

Reglas:

- Cuenta solicitudes por estado (`PENDIENTE`, `EN_PROCESO`, `RESUELTA`, `RECHAZADA`).
- Cuenta solicitudes por tipo.
- Lista **vencidas sin resolver**: solicitudes en `PENDIENTE` o `EN_PROCESO` cuya fecha de vencimiento sea **anterior** a la fecha de referencia.
- Calcula el **porcentaje de resueltas** con un decimal.
- Si no hay solicitudes → mensaje controlado, sin error.
- Si la fecha es inválida → error controlado.

### 5.6. Exportar CSV

Pide una ruta de archivo destino.

Reglas:

- Escribe una cabecera con las columnas: `id, legajo, nombre, tipo, estado, prioridad, fecha, vencimiento, responsable, observacion`.
- Recorre todas las solicitudes y escribe una fila por cada una.
- **Problema detectado al usarla:** si algún campo contiene comas (por ejemplo, un nombre `Pérez, Juan` o una observación con coma), el CSV se corrompe porque no escapa valores. Además, **el `catch` está vacío**: si el archivo no se puede escribir, el sistema no informa nada. Esto último lo dejamos anotado como hallazgo.

---

## 6. Reglas de plazos y prioridades por tipo (el `switch` de `registrar`)

Esta es la parte que más nos sirvió para entender el sistema. Está en `SolicitudManager.registrar(...)` y define, para cada tipo, el plazo en días y la prioridad según urgencia:

| Tipo | Días de plazo | Prioridad si es urgente | Prioridad si no es urgente |
|---|---|---|---|
| `CERTIFICADO` | 3 | `ALTA` | `MEDIA` |
| `EQUIVALENCIA` | 15 | `MEDIA` | `BAJA` |
| `CAMBIO_COMISION` | 5 | `ALTA` | `MEDIA` |
| `RECLAMO_NOTA` | 7 | `ALTA` | `ALTA` (siempre) |
| `X` (tipo inválido) | 10 | — | `BAJA` |

El vencimiento se calcula como `fecha + días`, con `LocalDate.plusDays`.

**Comentario del equipo:** este `switch` es, en sí mismo, un smell. Mezcla tres cosas en un solo bloque: (a) traducción de tipo a plazo, (b) cálculo de prioridad según urgencia, y (c) un caso `default` que **no debería existir** porque el tipo inválido nunca debería haber llegado hasta acá. En la etapa de refactorización lo vamos a reemplazar por un `enum TipoSolicitud` que encapsule plazo y prioridad.

---

## 7. Tecnología

- **Lenguaje:** Java 17.
- **Build:** Maven (con `maven-compiler-release` 17 y `maven-surefire` 3.2.5 declarado).
- **Testing:** JUnit 5 (5.10.2) declarado en `pom.xml`, pero **sin tests escritos** en la versión inicial.
- **Interfaz:** consola, con `Scanner` y `System.out`. Toda la interacción es por `stdin/stdout`.
- **Persistencia:** **en memoria**, con `ArrayList<Solicitud>`. Solo la exportación CSV toca disco, y lo hace con `FileWriter`.
- **Ejecución:** `mvn compile exec:java` (configurado en `pom.xml` con `exec-maven-plugin` 3.2.0, main class `ar.utn.solicitudes.Main`).

---

## 8. Repositorio y tag

- **Repositorio:** `https://github.com/PalmaUriel/trabajointegrador-mds2-solicitudes-academicas.git`
- **Tag de la versión base:** `estado-inicial`
- **Commit de referencia:** `<completar con el hash del commit asociado al tag>`

> Antes de la entrega: verificar que `git tag -l` muestre `estado-inicial` y que `git checkout estado-inicial` levante el sistema con el menú funcionando.

---

## 9. Evidencia

Capturas que respaldan esta descripción (guardar en `docs/capturas/` y referenciar acá):

- `01-menu-principal.png` — Menú con las 6 opciones.
- `02-registrar-solicitud.png` — Alta exitosa de una solicitud `CERTIFICADO` no urgente (prioridad `MEDIA`, vencimiento a 3 días).
- `03-registrar-solicitud-invalida.png` — Rechazo por legajo inválido y por descripción corta.
- `04-asignar-responsable.png` — Asignación exitosa y caso de solicitud cerrada.
- `05-cambiar-estado-sin-responsable.png` — Error al intentar pasar a `EN_PROCESO` sin responsable.
- `06-cambiar-estado-con-observacion.png` — Paso a `RECHAZADA` con observación.
- `07-buscar-solicitudes.png` — Búsqueda por estado y por legajo.
- `08-reporte.png` — Reporte con conteos, vencidas y porcentaje de resueltas.
- `09-exportar-csv.png` — Archivo CSV abierto, mostrando la cabecera y una fila.
- `10-exportar-csv-error.png` — Caso de ruta inválida: el sistema no informa nada (evidencia del `catch` vacío).

---

## 10. Observaciones iniciales (insumos para la próxima etapa)

Al describir el sistema detectamos, sin corregirlos todavía, varios puntos que vamos a diagnosticar formalmente en la **Etapa 3 (Deuda técnica y code smells)**:

1. `SolicitudManager` concentra validación, cálculo, asignación, cambio de estado, búsqueda, reporte y exportación (clase “Dios”).
2. `Solicitud` tiene todos sus campos `public` (sin encapsulamiento).
3. `tipo`, `estado` y `prioridad` son `String` en lugar de `enum` (Primitive Obsession / Magic Strings).
4. El `switch` de `registrar` mezcla plazos, prioridades y un caso `default` inalcanzable en un sistema bien validado.
5. `buscarPorEstado`, `buscarPorTipo` y `buscarPorLegajo` repiten el mismo patrón de bucle.
6. `cambiarEstado` repite la búsqueda por id y anida varios `if/else` por transición.
7. `exportarCsv` no escapa comas y tiene un `catch` vacío con un `TODO`.
8. La lógica de negocio imprime por consola y devuelve `-1`/`false`, lo que impide testearla de forma aislada.
9. El `Main` traduce strings a mayúsculas y decide cosas que son reglas de dominio.

Estos puntos se abordan en las etapas siguientes. **En esta etapa no se corrige nada: solo se describe el sistema y se deja constancia de que existen.**

---

### Anexo rápido: comandos sugeridos para preparar la entrega

```bash
# Verificar tag
git tag -l
# Crear el tag si aún no existe
git tag -a estado-inicial -m "Versión base del sistema de solicitudes académicas"
git push origin estado-inicial

# Compilar y ejecutar
mvn compile exec:java