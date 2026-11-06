# Etapa 1 — Descripción del sistema base

> Versión descrita: tag `estado-inicial`.

## 1. Dominio y problema

El sistema pertenece al dominio de la gestión académica. Resuelve un problema acotado: cuando un estudiante necesita hacer un trámite (certificado, equivalencia, cambio de comisión o reclamo de nota), no existe un canal único que registre el pedido, lo asigne a un responsable y permita seguir su estado. Nuestro sistema base propone un registro centralizado de solicitudes académicas, con la posibilidad de asignar un responsable y consultar el avance.

El sistema está pensado para la oficina de alumnos que recibe y clasifica los pedidos. El estudiante no lo usa directamente (no hay login): el personal administrativo opera el sistema con los datos que el estudiante aporta y es quien puede informarle el estado de su trámite.

## 2. Alcance

**Qué hace el sistema (versión inicial):**

- Registra solicitudes académicas con datos del estudiante, tipo, descripción y fecha.
- Calcula automáticamente la prioridad y la fecha de vencimiento según el tipo y el carácter urgente.
- Permite asignar un responsable (nombre y área) a una solicitud abierta.
- Permite cambiar el estado de una solicitud respetando reglas de transición.
- Permite buscar solicitudes por estado, tipo o legajo.
- Genera un reporte por estado y por tipo, con la cantidad de vencidas y el porcentaje de resueltas.
- Exporta todas las solicitudes a un archivo CSV.

**Qué no hace** (lo dejamos explícito para no sobredimensionar el alcance):

- No tiene base de datos. Los datos viven en un `ArrayList` en memoria y se pierden al cerrar el programa.
- No tiene login ni usuarios: no hay autenticación, roles ni permisos.
- No edita ni elimina solicitudes una vez registradas. Solo se puede cambiar el estado y asignar responsable.
- No guarda historial de estados. Solo se conserva la observación cargada al cerrar la solicitud.
- No valida el área del responsable contra un catálogo; cualquier texto es aceptado.
- No notifica al estudiante ni al responsable por ningún canal.

Estas ausencias no son "falencias" a corregir: son el alcance real del sistema base del TPI. Lo que vamos a diagnosticar y mejorar en las etapas siguientes es **cómo está construido**, no cuántas funcionalidades tiene.

## 3. Actores

| Actor | Rol en el sistema | Qué hace en la versión inicial |
|---|---|---|
| Estudiante | Origen del pedido | No interactúa con el sistema. Sus datos (legajo, nombre, email) los ingresa el operador al registrar la solicitud. |
| Personal de la oficina de alumnos (operador) | Usuario principal | Usa el menú: registra, asigna, cambia estados, busca, genera reportes y exporta. |
| Responsable / Área | Quien resuelve la solicitud | No interactúa con el sistema. Se lo asigna a una solicitud y queda registrado como dato. |

## 4. Entidades del modelo

| Clase | Qué representa | Atributos principales |
|---|---|---|
| `Estudiante` | Persona que origina la solicitud. | `legajo`, `nombre`, `email` |
| `Responsable` | Persona o área que atiende la solicitud. | `nombre`, `area` |
| `Solicitud` | El pedido académico. Es la entidad central. | `id`, `est`, `tipo`, `desc`, `estado`, `prioridad`, `fecha`, `venc`, `resp`, `obs` |

Además existen `SolicitudManager`, que contiene toda la lógica del sistema, y `Main`, que implementa el menú por consola.

**Observación sobre el modelo:** en la versión inicial, `Solicitud` tiene todos sus campos públicos (`public int id; public Estudiante est; …`). Cualquier clase puede modificar el estado, la prioridad o el vencimiento de una solicitud sin pasar por ninguna validación. Además, `tipo`, `estado` y `prioridad` son `String`, no `enum`: los valores válidos están escritos como texto en varios lugares del código. En esta etapa solo lo describimos; se analiza en la Etapa 3 (CS-05 y CS-06).

## 5. Funcionalidades (las 6 opciones del menú)

> Los mensajes del sistema se citan textualmente, tal como aparecen en pantalla (sin tildes).

### 5.1. Registrar solicitud

Pide: legajo, nombre, email, tipo (1 a 4), descripción, si es urgente (s/n) y fecha en formato dd/mm/aaaa.

Reglas que descubrimos al usarla:

- **Legajo:** obligatorio y de 5 o 6 dígitos. Si tiene letras o menos dígitos, muestra `Error: legajo invalido`.
- **Nombre:** obligatorio.
- **Email:** obligatorio; debe contener al menos un `@` (no valida el formato completo).
- **Tipo:** las opciones 1 a 4 corresponden a los cuatro tipos. Ante cualquier otro valor, `Main` lo traduce internamente al texto `"X"` y `SolicitudManager` lo rechaza con `Error: tipo de solicitud invalido`.
- **Descripción:** mínimo 10 caracteres.
- **Fecha:** debe respetar el formato dd/mm/aaaa; si no, muestra `Error: fecha invalida`. Sin embargo, una fecha con formato correcto pero inexistente, como `31/02/2026`, **se acepta sin aviso** y se registra como `28/02/2026` (defecto E2).
- **Prioridad y vencimiento:** se calculan automáticamente (ver sección 6).
- **Reutilización de estudiante:** si ya existe una solicitud con el mismo legajo, reutiliza ese estudiante. Si el operador ingresó otro nombre o email, **los datos nuevos se ignoran sin aviso** (defecto E4).
- **Estado inicial:** toda solicitud nueva arranca en `PENDIENTE`.
- Si todo es válido, muestra `Solicitud registrada con id N`.

### 5.2. Asignar responsable

Pide el id de la solicitud, el nombre del responsable y el área.

Reglas:

- Si el id no existe: `Error: solicitud no encontrada`.
- Si la solicitud está `RESUELTA` o `RECHAZADA`: `Error: la solicitud esta cerrada`.
- El nombre del responsable es obligatorio (`Error: el responsable es obligatorio`). El área no se valida.
- Se puede reasignar el responsable mientras la solicitud esté abierta.
- Si se ingresa un id no numérico, el programa **se cierra** con una excepción (defecto E1).

### 5.3. Cambiar estado

Pide el id de la solicitud, el nuevo estado (1 = En proceso, 2 = Resuelta, 3 = Rechazada) y una observación.

Reglas que descubrimos (son las más importantes del sistema):

| Estado actual | Puede pasar a | Condición |
|---|---|---|
| `PENDIENTE` | `EN_PROCESO` | Debe tener responsable; si no: `Error: debe asignar un responsable antes de iniciar el tramite` |
| `PENDIENTE` | `RECHAZADA` | Debe tener observación (motivo); si no: `Error: debe indicar el motivo del rechazo` |
| `EN_PROCESO` | `RESUELTA` | La observación es opcional |
| `EN_PROCESO` | `RECHAZADA` | Debe tener observación (motivo) |
| `RESUELTA` / `RECHAZADA` | — | Ningún cambio permitido |

Cualquier transición que no esté en la tabla (por ejemplo, de `PENDIENTE` a `RESUELTA`, o desde un estado cerrado) muestra `Error: transicion de estado no permitida`. Lo mismo ocurre si el operador elige una opción distinta de 1, 2 o 3: `Main` la traduce a `"X"` y el manager la rechaza por transición no permitida.

La observación solo se guarda al pasar a `RESUELTA` o `RECHAZADA`. Al igual que en 5.2, un id no numérico cierra el programa (E1).

En este punto se nota el acoplamiento entre `Main`, que traduce las opciones numéricas a textos, y `SolicitudManager`, que decide las reglas comparando esos textos.

### 5.4. Buscar solicitudes

Pide el criterio (1 = Estado, 2 = Tipo, 3 = Legajo) y el valor.

Reglas:

- **Por estado y por tipo:** el valor ingresado se convierte a mayúsculas antes de comparar, así que `pendiente` y `PENDIENTE` dan el mismo resultado.
- **Por legajo:** compara el texto exacto.
- Si no hay coincidencias: `No se encontraron solicitudes.`
- Si el criterio es inválido: `Opcion invalida` (después de pedir el valor).

Cada resultado se muestra en una línea con id, estado, tipo, prioridad, estudiante, vencimiento y responsable.

### 5.5. Reporte

Pide una fecha de referencia en formato dd/mm/aaaa.

Reglas:

- Cuenta las solicitudes por estado (`PENDIENTE`, `EN_PROCESO`, `RESUELTA`, `RECHAZADA`).
- Cuenta las solicitudes por tipo.
- Lista las **vencidas sin resolver**: solicitudes en `PENDIENTE` o `EN_PROCESO` cuya fecha de vencimiento es anterior a la fecha de referencia. El mismo día del vencimiento todavía no cuenta como vencida.
- Calcula el porcentaje de resueltas con un decimal.
- Si no hay solicitudes: `No hay solicitudes registradas.`
- Si la fecha es inválida: `Error: fecha invalida`.

### 5.6. Exportar CSV

Pide la ruta del archivo destino.

Reglas:

- Escribe una cabecera con las columnas `id, legajo, nombre, tipo, estado, prioridad, fecha, vencimiento, responsable, observacion`.
- Escribe una fila por cada solicitud y muestra `Se exportaron N solicitudes.`
- **Problemas detectados:**
  - Al usarla con una ruta inválida, el sistema **no informa nada**: ni éxito ni error, porque el `catch` está vacío (defecto E3).
  - Al leer el código vimos además que los valores no se escapan: un nombre como `Perez, Juan` agregaría una columna de más y desordenaría el archivo.

## 6. Reglas de plazos y prioridades por tipo (el `switch` de `registrar`)

Esta parte fue la que más nos sirvió para entender el sistema. Está en `SolicitudManager.registrar(...)` (líneas 55–77) y define, para cada tipo, el plazo en días y la prioridad según la urgencia:

| Tipo | Días de plazo | Prioridad si es urgente | Prioridad si no es urgente |
|---|---|---|---|
| `CERTIFICADO` | 3 | ALTA | MEDIA |
| `EQUIVALENCIA` | 15 | MEDIA | BAJA |
| `CAMBIO_COMISION` | 5 | ALTA | MEDIA |
| `RECLAMO_NOTA` | 7 | ALTA | ALTA (siempre) |

El vencimiento se calcula como fecha de alta + días de plazo (`LocalDate.plusDays`).

**Comentario del equipo:** este `switch` es, en sí mismo, un smell. Mezcla en un solo bloque la traducción de tipo a plazo y el cálculo de prioridad según urgencia, y además tiene un caso `default` (10 días, prioridad BAJA) que **nunca se ejecuta**, porque un tipo inválido ya se rechazó antes en la validación. Es código muerto. En la etapa de refactoring lo vamos a reemplazar por un `enum TipoSolicitud` que encapsule el plazo y la prioridad.

## 7. Tecnología

| Aspecto | Detalle |
|---|---|
| Lenguaje | Java 17 |
| Build | Maven (`maven.compiler.release` 17, `maven-surefire-plugin` 3.2.5) |
| Testing | JUnit 5 (5.10.2) declarado en `pom.xml`, sin pruebas escritas en la versión inicial |
| Interfaz | Consola (`Scanner` y `System.out`) |
| Persistencia | En memoria (`ArrayList<Solicitud>`). Solo la exportación escribe en disco, con `FileWriter` |
| Ejecución | `mvn compile exec:java` (`exec-maven-plugin` 3.2.0, clase principal `ar.utn.solicitudes.Main`), o el botón Run de VS Code sobre `Main.java` |

## 8. Repositorio

- **Repositorio:** https://github.com/PalmaUriel/trabajointegrador-mds2-solicitudes-academicas
- **Tag de la versión base:** `estado-inicial`
- **Commit de referencia:** `<hash>` *(se obtiene con `git rev-list -n 1 estado-inicial`)*

## 9. Evidencia de funcionamiento

Capturas de la ejecución de la versión inicial, en `docs/evidencias/inicial/`:

| Captura | Qué muestra |
|---|---|
| `01-menu.png` | Menú principal con las 6 opciones |
| `02-registro-ok.png` | Alta exitosa de una solicitud |
| `03-listado-pendientes.png` | Búsqueda por estado: 4 solicitudes con prioridades y vencimientos según el tipo |
| `04-regla-sin-responsable.png` | Error al intentar pasar a `EN_PROCESO` sin responsable |
| `05-asignar-responsable.png` | Asignación exitosa de responsable |
| `06-cambio-estado.png` | Paso a `EN_PROCESO` y luego a `RESUELTA` |
| `07-regla-motivo-rechazo.png` | Error al rechazar sin motivo |
| `08-busqueda-legajo.png` | Búsqueda por legajo |
| `09-reporte.png` | Reporte con conteos, vencidas y porcentaje de resueltas |
| `10-exportar.png`, `11-csv.png` | Exportación exitosa y archivo CSV abierto |
| `12-validacion-legajo.png`, `13-validacion-email.png` | Validaciones de registro |
| `14-defecto-fecha-31-02.png` | Defecto E2: fecha inexistente aceptada |
| `15-defecto-legajo-repetido.png` | Defecto E4: el nombre nuevo se ignora |
| `16-defecto-export-silencioso.png` | Defecto E3: exportación fallida sin mensaje |
| `17-defecto-id-no-numerico.png` | Defecto E1: el programa se cierra |

## 10. Observaciones iniciales (insumos para las etapas siguientes)

Al describir el sistema detectamos, sin corregirlos todavía, varios puntos que se analizan formalmente en las Etapas 2 y 3:

- `SolicitudManager` concentra validación, cálculo, asignación, cambio de estado, búsqueda, reporte, impresión y exportación ("clase Dios").
- `Solicitud` tiene todos sus campos públicos (sin encapsulamiento).
- `tipo`, `estado` y `prioridad` son `String` en lugar de `enum` (Primitive Obsession / Magic Strings).
- El `switch` de `registrar` mezcla plazos y prioridades y tiene un caso `default` inalcanzable.
- `buscarPorEstado`, `buscarPorTipo` y `buscarPorLegajo` repiten el mismo bucle.
- `asignarResponsable` y `cambiarEstado` repiten la búsqueda por id; `cambiarEstado` anida varios `if/else` por transición.
- `exportarCsv` no escapa valores y tiene un `catch` vacío con un `TODO`.
- La lógica de negocio imprime por consola y devuelve `-1`/`false`, lo que impide probarla de forma aislada.
- `Main` traduce las opciones numéricas a textos (`"CERTIFICADO"`, `"X"`), por lo que conoce detalles que le corresponden al dominio.
- No hay pruebas automatizadas.

En esta etapa no se corrige nada: solo se describe el sistema y se deja constancia de estos puntos.
