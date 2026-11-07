# Etapa 6 — Pruebas y verificación

## 1. Estrategia

El objetivo de esta etapa es demostrar que los refactorings **no alteraron el comportamiento externo** del sistema.

Como la versión inicial no tenía pruebas automatizadas, verificamos el comportamiento con **casos de prueba manuales y evidencia antes/después**:

1. **Antes de refactorizar** (tag `estado-inicial`), recorrimos todas las funcionalidades del sistema y capturamos cada resultado (`docs/evidencias/inicial/`).
2. **Durante el refactoring**, después de cada cambio (R1 a R4) probamos el código modificado antes de aprobar el Pull Request (`docs/evidencias/refactorings/`).
3. **Después de refactorizar**, repetimos el recorrido completo con la versión final y capturamos los resultados (`docs/evidencias/refactorizado/`).

En cada caso comprobamos que la versión refactorizada cumple la misma regla y muestra el mismo mensaje que la versión inicial. Algunos datos de prueba variaron entre los dos recorridos (por ejemplo, fechas y nombres), pero las reglas verificadas y los mensajes del sistema son los mismos.

Los defectos detectados en el diagnóstico (E1 a E4) **no se corrigieron durante el refactoring**, por decisión del equipo: un refactoring no debe cambiar el comportamiento externo. Por eso, en esos casos el resultado esperado después del refactoring es **el mismo defecto**.

## 2. Casos de prueba

### 2.1. Funcionamiento normal

| Caso | Qué se verifica | Resultado esperado | Antes | Después |
|---|---|---|---|---|
| CP-01 | Inicio del sistema | Menú con las opciones 1 a 6 y 0 | ✅ `01-menu.png` | ✅ `01-menu.png` |
| CP-02 | Registro válido | `Solicitud registrada con id 1` | ✅ `02-registro-ok.png` | ✅ `02-registro-ok.png` |
| CP-03 | Prioridad y vencimiento según el tipo | Certificado urgente: ALTA, +3 días. Equivalencia: BAJA, +15. Reclamo: ALTA, +7. Cambio de comisión: ALTA si es urgente, MEDIA si no, +5 | ✅ `03-listado-pendientes.png` | ✅ `03-listado-pendientes.png` |
| CP-04 | No se puede iniciar sin responsable | `Error: debe asignar un responsable antes de iniciar el tramite` | ✅ `04-regla-sin-responsable.png` | ✅ `04-regla-sin-responsable.png` |
| CP-05 | Asignación de responsable | `Responsable asignado.` | ✅ `05-asignar-responsable.png` | ✅ `05-asignar-responsable.png` |
| CP-06 | Pendiente → En proceso → Resuelta | `Estado actualizado.` (dos veces) | ✅ `06-cambio-estado.png` | ✅ `06-cambio-estado.png` |
| CP-07 | El rechazo exige motivo | `Error: debe indicar el motivo del rechazo` | ✅ `07-regla-motivo-rechazo.png` | ✅ `07-regla-motivo-rechazo.png` |
| CP-08 | Búsqueda por legajo | Las solicitudes del legajo `10234`, con su estado actual | ✅ `08-busqueda-legajo.png` | ✅ `08-busqueda-legajo.png` |
| CP-09 | Reporte al `20/03/2026` | Conteo por estado y tipo, solicitudes vencidas y porcentaje de resueltas | ✅ `09-reporte.png` | ✅ `09-reporte.png` |
| CP-10 | Exportación CSV | `Se exportaron 4 solicitudes.` y archivo con cabecera y 4 filas | ✅ `10-exportar.png`, `11-csv.png` | ✅ `10-exportar.png`, `11-csv.png` |

### 2.2. Validaciones

| Caso | Qué se verifica | Resultado esperado | Antes | Después |
|---|---|---|---|---|
| CP-11 | Legajo con letras (`12AB`) | `Error: legajo invalido` | ✅ `12-validacion-legajo.png` | ✅ `12-validacion-legajo.png` |
| CP-12 | Email sin `@` | `Error: email invalido` | ✅ `13-validacion-email.png` | ✅ `13-validacion-email.png` |

### 2.3. Defectos conocidos (se conservan a propósito)

| Caso | Qué se verifica | Resultado esperado (antes y después) | Antes | Después |
|---|---|---|---|---|
| CP-13 (E2) | Fecha inexistente `31/02/2026` | Se acepta y vence `03/03/2026` (la toma como 28/02) | ✅ `14-defecto-fecha-31-02.png` | ✅ `14-defecto-fecha-31-02.png` |
| CP-14 (E4) | Legajo `20456` registrado con otro nombre | La solicitud nueva figura como Bruno Diaz | ✅ `15-defecto-legajo-repetido.png` | ✅ `15-defecto-legajo-repetido.png` |
| CP-15 (E3) | Exportar a una ruta inexistente | No muestra ningún mensaje | ✅ `16-defecto-export-silencioso.png` | ✅ `16-defecto-export-silencioso.png` |
| CP-16 (E1) | Id no numérico (`abc`) | El programa se cierra con `NumberFormatException` | ✅ `17-defecto-id-no-numerico.png` | ✅ `17-defecto-id-no-numerico.png` |

## 3. Verificación de cada refactoring

| Refactoring | Qué se verificó | Resultado | Evidencia |
|---|---|---|---|
| R1 — Extract Method (`buscarPorId`) | Asignar y cambiar estado con un id existente y uno inexistente | ✅ Sin cambios | `r1-verificacion.png` |
| R2 — Move Method (`estaCerrada`, `estaVencidaAl`) | Asignar a una solicitud cerrada; reporte el día del vencimiento y después | ✅ Sin cambios | `r2-verificacion.png` |
| R3 — Introduce Parameter Object (`DatosRegistroSolicitud`) | Registro válido con todos los datos en su lugar; validaciones | ✅ Sin cambios | `r3-verificacion.png` |
| R4 — Enum `TipoSolicitud` | Los 4 tipos dan la misma prioridad y el mismo vencimiento que antes; tipo inválido | ✅ Sin cambios | `r4-verificacion.png` |

## 4. Resultado

Los 16 casos de prueba dieron el mismo resultado antes y después del refactoring, incluidos los 4 defectos conocidos, que se conservan sin cambios. Esto demuestra que los refactorings R1 a R4 mejoraron la estructura interna del código **sin alterar el comportamiento externo** del sistema.

## 5. Limitaciones y mejoras futuras

- La verificación es manual: hay que repetir el recorrido después de cada cambio. Como mejora, se podría automatizar con pruebas JUnit.
- Los defectos E1 a E4 quedan registrados para corregirse en una etapa posterior, como cambios de comportamiento intencionales y documentados.
