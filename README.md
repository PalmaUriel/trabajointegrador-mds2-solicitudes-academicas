# Sistema de Gestión de Solicitudes Académicas

Trabajo Práctico Integrador de **Metodología de Sistemas II**: Tecnicatura Universitaria en Programación a Distancia, UTN.

**Integrantes:** Uriel Palma · Lorenzo Pattini · Federico Quinteros
**Comisión:** M25 C4-08

## Descripción

Sistema por consola para que la oficina de alumnos registre y gestione los trámites de los estudiantes: certificados, equivalencias, cambios de comisión y reclamos de nota.

El objetivo del trabajo no es agregar funcionalidades, sino **mejorar la calidad interna** de un sistema existente: diagnosticar su deuda técnica, aplicar refactorings seguros y verificar que el comportamiento externo se conserve. El repositorio contiene la versión inicial y su evolución hasta la versión refactorizada.

## Funcionalidades

| Opción | Funcionalidad | Reglas principales |
|---|---|---|
| 1 | Registrar solicitud | Legajo de 5 o 6 dígitos, email con `@`, descripción de 10 caracteres o más, fecha dd/mm/aaaa. Prioridad y vencimiento según el tipo |
| 2 | Asignar responsable | Solo en solicitudes abiertas |
| 3 | Cambiar estado | Para pasar a En proceso hace falta un responsable; para rechazar hace falta un motivo |
| 4 | Buscar solicitudes | Por estado, tipo o legajo |
| 5 | Reporte | Totales por estado y tipo, solicitudes vencidas y porcentaje de resueltas |
| 6 | Exportar CSV | Genera un archivo con todas las solicitudes |

| Tipo | Plazo | Prioridad normal / urgente |
|---|---|---|
| Certificado | 3 días | MEDIA / ALTA |
| Equivalencia | 15 días | BAJA / MEDIA |
| Cambio de comisión | 5 días | MEDIA / ALTA |
| Reclamo de nota | 7 días | ALTA / ALTA |

## Tecnologías

- Java 17
- Maven
- Interfaz por consola
- Datos en memoria

## Cómo ejecutarlo

Requisitos: JDK 17 o superior.

**Desde VS Code** (con la extensión *Extension Pack for Java*): abrir la carpeta del proyecto, abrir `src/main/java/ar/utn/solicitudes/Main.java` y hacer clic en **Run**.

**Desde la terminal** (con Maven instalado):

```
mvn compile exec:java
```

## Estructura del repositorio

```
src/main/java/ar/utn/solicitudes/
├── Main.java                     menú por consola
├── SolicitudManager.java         lógica del sistema
├── Solicitud.java                entidad principal
├── Estudiante.java
├── Responsable.java
├── DatosRegistroSolicitud.java   agregado en R3
└── TipoSolicitud.java            agregado en R4
docs/
├── 01 a 07                       informe técnico por etapa
├── uso-de-ia.md                  declaración de uso de IA
└── evidencias/                   capturas antes, durante y después del refactoring
```

## Versiones (tags)

| Tag | Contenido |
|---|---|
| `estado-inicial` | Sistema base, antes de cualquier mejora |
| `etapa-diagnostico` | Descripción del sistema, análisis de calidad y code smells |
| `etapa-refactoring` | Refactorings R1 a R4 aplicados |
| `etapa-pruebas` | Casos de prueba y evidencias antes/después |
| `etapa-docs` | Code review, conclusiones y documentación completa |
| `estado-refactorizado` | Versión final |

Para ver la versión inicial: `git checkout estado-inicial` (y `git checkout main` para volver).

## Documentación

| Etapa | Documento |
|---|---|
| 1. Sistema base | [01 Sistema Base](docs/01%20Sistema%20Base.md) |
| 2. Calidad externa e interna | [02 Calidad](docs/02%20Calidad.md) |
| 3. Deuda técnica y code smells | [03 Code Smells](docs/03%20Code%20Smells.md) |
| 4 y 5. SOLID, Clean Code y refactorings | [05 SOLID y Refactorings](docs/05%20SOLID%20y%20Refactorings.md) |
| 6. Pruebas y verificación | [04 Pruebas](docs/04%20Pruebas.md) |
| 7. Code review | [06 Code Review](docs/06%20Code%20Review.md) |
| 8. Conclusiones | [07 Conclusiones](docs/07%20Conclusiones.md) |
| Uso de IA | [uso-de-ia](docs/uso-de-ia.md) |

## Forma de trabajo

Cada cambio se hizo en una rama propia y entró a `main` mediante un Pull Request revisado por otro integrante. Los commits siguen el formato `tipo(alcance): descripción` (`feat`, `refactor`, `docs`, `style`).

## Datos

Todos los datos de ejemplo (nombres, legajos, emails) son **ficticios**.
