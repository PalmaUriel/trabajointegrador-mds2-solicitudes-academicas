# Etapa 7 — Code review

## 1. Cómo hicimos la revisión

Durante todo el trabajo, cada cambio entró al repositorio mediante un **Pull Request** revisado y aprobado por otro integrante antes del merge. Al terminar los refactorings, revisamos además la **versión refactorizada completa** (tag `etapa-refactoring`) con el checklist de la sección 2.

Para el feedback usamos siempre el mismo formato, objetivo y sin juicios sobre las personas:

> **Observación** (qué se ve y dónde) → **Impacto** (por qué importa) → **Sugerencia** (qué hacer).

## 2. Checklist técnico

| # | Criterio | Resultado | Comentario |
|---|---|---|---|
| 1 | El sistema compila y el comportamiento externo se conserva | ✅ Cumple | Verificado con los 16 casos de la Etapa 6 |
| 2 | Cada commit hace un solo cambio lógico, con un mensaje claro | ⚠️ Parcial | Ver OBS-03 y OBS-04 |
| 3 | Todo cambio entra por Pull Request revisado por otro integrante | ⚠️ Parcial | Ver OBS-03 |
| 4 | El código respeta un formato e indentación consistentes | ❌ No cumple | Ver OBS-01 y OBS-02 |
| 5 | Los nombres de métodos y variables revelan su intención | ⚠️ Parcial | Los métodos nuevos sí (`buscarPorId`, `estaCerrada`…). Persisten nombres cortos de la versión inicial (`p`, `e`, `s`, `x`), CS-12 |
| 6 | No hay código duplicado | ⚠️ Parcial | Se eliminó la búsqueda por id duplicada (R1); los tres métodos de búsqueda siguen repitiendo su bucle |
| 7 | Los métodos son cortos y hacen una sola cosa | ⚠️ Parcial | `registrar()` y `reporte()` siguen siendo extensos (CS-02) |
| 8 | Los métodos reciben pocos parámetros | ✅ Cumple | `registrar()` pasó de 7 parámetros a 1 (R3) |
| 9 | No hay números ni textos mágicos | ⚠️ Parcial | Los plazos están en el enum (R4); estados y prioridades siguen como `String`. Ver OBS-06 |
| 10 | Las reglas de negocio están en la clase que tiene los datos | ⚠️ Parcial | Cierre y vencimiento en `Solicitud` (R2); las transiciones de estado siguen en el manager |
| 11 | Los atributos están encapsulados | ❌ No cumple | `Solicitud` mantiene sus campos públicos (CS-06). Pendiente |
| 12 | Los errores se informan; no hay `catch` vacíos | ❌ No cumple | `exportarCsv()` mantiene el `catch` vacío (CS-08, defecto E3). Se conservó a propósito durante el refactoring. Pendiente |
| 13 | Se documentó el cambio y su verificación | ✅ Cumple | Etapas 4 a 6 y capturas en `docs/evidencias/` |

Los ítems 11 y 12 no cumplen porque corresponden a smells que el equipo decidió no abordar en esta iteración (ver Etapa 5, sección 4). Quedan registrados como deuda técnica conocida.

## 3. Observaciones

| Id | Severidad | Observación | Impacto | Sugerencia | Decisión |
|---|---|---|---|---|---|
| OBS-01 | Baja | En `SolicitudManager.java` quedaron líneas con sangría incorrecta (`Solicitud sol = buscarPorId(id);` en `asignarResponsable` y `cambiarEstado`, e `if (!TipoSolicitud.existe(tipo))` en `registrar`) y 5 líneas con espacios en blanco al final. Vienen de R1 y R4. | Dificulta la lectura y genera diferencias innecesarias en los diffs. | Aplicar el formateador del editor (`Shift+Alt+F` en VS Code). | ✅ **Corregido** |
| OBS-02 | Baja | `DatosRegistroSolicitud.java` tiene la sangría de los parámetros y la llave de cierre desalineadas (`String fecha){` / `}` indentada). | Ídem OBS-01. | Aplicar el formateador. | ✅ **Corregido** |
| OBS-03 | Media | El commit de R2 (`afec233`) se hizo directamente en `main`, sin Pull Request, y su mensaje no sigue el formato acordado (`refactor(R2): ...`). | Ese cambio no tuvo revisión previa de otro integrante, y el historial queda menos uniforme. | Para cambios futuros, todo entra por PR. | ⏸️ **Aceptado:** el cambio ya fue verificado (Etapa 6) y corregir el historial implicaría reescribirlo. Se adopta la regla para adelante. |
| OBS-04 | Baja | El mensaje del commit de R3 no tiene espacio después de los dos puntos (`refactor(R3):introduce...`). | Menor; afecta la uniformidad del historial. | Respetar el formato `tipo(alcance): descripción`. | ⏸️ **Aceptado** sin cambios. |
| OBS-05 | Baja | `registrar()` desarma `DatosRegistroSolicitud` en 7 variables locales al inicio del método. | El Parameter Object todavía no aporta todo su valor: la validación de los datos sigue en el manager. | Mover las validaciones al propio `DatosRegistroSolicitud`. | 📌 **Mejora futura:** se dejó así a propósito para que R3 fuera un paso chico y seguro. |
| OBS-06 | Media | `TipoSolicitud` devuelve la prioridad como texto (`"ALTA"`, `"MEDIA"`, `"BAJA"`). Los estados también siguen como `String`. | Persisten los textos mágicos (CS-05): un error de tipeo no lo detecta el compilador. | Crear enums `Prioridad` y `EstadoSolicitud`. | 📌 **Mejora futura.** |
| OBS-07 | Baja | El método privado `buscarPorId` quedó ubicado entre dos métodos públicos de búsqueda. | Menor; mezcla métodos públicos y privados. | Agrupar los métodos privados al final de la clase. | ⏸️ **Aceptado** sin cambios. |

**Referencias:** ✅ corregido · ⏸️ aceptado con justificación · 📌 registrado como mejora futura.

## 4. Correcciones aplicadas

OBS-01 y OBS-02 se corrigieron en el commit `style: corregir formato e indentacion (code review OBS-01, OBS-02)`, que solo cambia espacios y sangrías. Por eso no modifica el comportamiento del sistema; aun así se ejecutó el sistema para confirmarlo.

## 5. Criterios de aceptación

Un Pull Request se aprueba cuando:

1. El sistema compila y se ejecuta sin errores nuevos.
2. El comportamiento externo se conserva (o el cambio está documentado como intencional).
3. Otro integrante distinto del autor lo revisó y lo aprobó.
4. Las observaciones de severidad **media o alta** se corrigieron o tienen una decisión justificada por escrito.
5. El mensaje del commit sigue el formato `tipo(alcance): descripción`.

Con las correcciones de la sección 4, la versión refactorizada **cumple estos criterios** y queda aceptada.

## 6. Evidencia

- Pull Requests aprobados en GitHub: #4 (R1), #5 (R3), #6 (R4), y los de documentación.
- Capturas de las aprobaciones y comentarios de los PR: `docs/evidencias/code-review/`.
