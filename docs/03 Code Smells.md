# 3. Diagnóstico de deuda técnica y code smells

## 3.1. Introducción

La deuda técnica comprende las deficiencias en el diseño, la implementación o el mantenimiento del software que incrementan el esfuerzo necesario para modificarlo y evolucionarlo.

Los *code smells* son indicios de posibles problemas en el código fuente que, aunque no necesariamente generan errores funcionales inmediatos, pueden afectar su legibilidad, mantenibilidad, reutilización y capacidad de evolución.

Para diagnosticar la calidad interna del Sistema de Gestión de Solicitudes Académicas se realizó una inspección estática del código Java de la versión inicial, identificando las principales debilidades y evaluando sus consecuencias.

El análisis contempla la ubicación del problema, su evidencia, impacto, riesgo de mantenimiento, prioridad y propuesta de refactoring.

## 3.2. Problemas identificados

### CS-01. Large Class / God Class

**Ubicación:** `SolicitudManager.java`, líneas 9–294.

**Prioridad:** Alta.

**Descripción:**

La clase `SolicitudManager` concentra numerosas responsabilidades que deberían estar distribuidas entre componentes especializados.

Actualmente administra el registro, las validaciones, las asignaciones, los cambios de estado, las búsquedas, los reportes y la exportación de archivos CSV.

**Evidencia:**

```java
public class SolicitudManager {
    public int registrar(...) { ... }
    public boolean asignarResponsable(...) { ... }
    public boolean cambiarEstado(...) { ... }
    public ArrayList<Solicitud> buscarPorEstado(...) { ... }
    public void reporte(...) { ... }
    public void exportarCsv(...) { ... }
}
```

*Fragmento esquemático que resume los métodos existentes.*

**Impacto:**

La concentración de responsabilidades reduce la cohesión y dificulta comprender, mantener y probar cada funcionalidad de manera independiente.

**Riesgo de mantenimiento:**

Una modificación en las reglas de negocio o en el formato de exportación requiere intervenir sobre una misma clase, incrementando el riesgo de introducir errores en otras operaciones.

**Mejora propuesta:**

Aplicar el principio de responsabilidad única (SRP), separando las responsabilidades en componentes especializados, como un servicio de solicitudes, un generador de reportes y un exportador CSV.

---

### CS-02. Long Method (método extenso)

**Ubicación:** `SolicitudManager.java`, método `registrar()`, líneas 16–104.

**Prioridad:** Alta.

**Descripción:**

El método `registrar()` realiza múltiples operaciones: valida datos, interpreta fechas, determina la prioridad y el vencimiento, busca estudiantes existentes, crea una solicitud y la incorpora a la lista.

**Evidencia:**

```java
if (!legajo.matches("\\d{5,6}")) {
    System.out.println("Error: legajo invalido");
    return -1;
}
```

Este bloque de validación forma parte de un método que también contiene cálculos y operaciones de almacenamiento.

**Impacto:**

Dificulta identificar responsabilidades, localizar errores y comprender el flujo completo de registro.

**Riesgo de mantenimiento:**

Modificar una regla de validación o un cálculo puede afectar otras etapas del proceso. También resulta más complejo crear pruebas unitarias aisladas.

**Mejora propuesta:**

Aplicar **Extract Method**, separando validaciones, cálculo de prioridad y vencimiento y búsqueda o creación de estudiantes.

---

### CS-03. Long Parameter List (lista extensa de parámetros)

**Ubicación:** `SolicitudManager.java`, líneas 16–17.

**Prioridad:** Media.

**Descripción:**

El método `registrar()` recibe siete parámetros individuales.

**Evidencia:**

```java
public int registrar(
    String legajo,
    String nombre,
    String email,
    String tipo,
    String descripcion,
    boolean urgente,
    String fecha
)
```

**Impacto:**

La cantidad de parámetros dificulta la lectura de las llamadas al método y aumenta el riesgo de intercambiar argumentos del mismo tipo.

**Riesgo de mantenimiento:**

La incorporación o modificación de datos requiere actualizar las llamadas existentes y puede afectar múltiples componentes.

**Mejora propuesta:**

Aplicar **Introduce Parameter Object** mediante un objeto `DatosRegistroSolicitud` que reúna la información necesaria para registrar solicitudes.

---

### CS-04. Duplicated Code (código duplicado)

**Ubicación:** `SolicitudManager.java`, líneas 106–113 y 131–138.

**Prioridad:** Media.

**Descripción:**

Los métodos `asignarResponsable()` y `cambiarEstado()` implementan la misma lógica para buscar una solicitud mediante su identificador.

**Evidencia:**

```java
Solicitud sol = null;
for (Solicitud s : lista) {
    if (s.id == id) {
        sol = s;
        break;
    }
}
```

Este fragmento se repite en ambos métodos.

**Impacto:**

La duplicación incrementa la cantidad de código y dificulta centralizar el mantenimiento de la funcionalidad.

**Riesgo de mantenimiento:**

Si cambia el mecanismo de búsqueda, será necesario modificar ambos métodos. Una implementación diferente entre ellos podría producir comportamientos inconsistentes.

**Mejora propuesta:**

Aplicar **Extract Method** para crear un método privado reutilizable `buscarSolicitudPorId(int id)`.

---

### CS-05. Primitive Obsession / Magic Strings

**Ubicación:** `Solicitud.java`, líneas 9–12; `SolicitudManager.java`, líneas 35–38, 52–78 y 143–179.

**Prioridad:** Media.

**Descripción:**

Los estados, tipos y prioridades se representan mediante cadenas de texto (`String`), y sus valores se comparan directamente en diferentes partes del código.

**Evidencia:**

```java
if (sol.estado.equals("PENDIENTE")) {
    if (nuevo.equals("EN_PROCESO")) {
        // ...
    }
}
```

**Impacto:**

El uso repetido de cadenas dificulta centralizar los valores permitidos y permite errores de escritura que no pueden detectarse durante la compilación.

**Riesgo de mantenimiento:**

Incorporar o modificar un estado puede requerir cambios en múltiples métodos, aumentando el riesgo de inconsistencias.

**Mejora propuesta:**

Evaluar la utilización de enumeraciones (`enum`) para representar tipos, estados y prioridades.

Durante el refactoring deberá mantenerse la representación externa esperada por el sistema, incluyendo los textos utilizados en reportes y archivos exportados.

---

### CS-06. Data Class / Encapsulamiento insuficiente

**Ubicación:** `Solicitud.java`, líneas 7–16.

**Prioridad:** Alta.

**Descripción:**

La clase `Solicitud` contiene atributos públicos que pueden ser modificados directamente desde otras clases.

**Evidencia:**

```java
public int id;
public Estudiante est;
public String tipo;
public String desc;
public String estado;
public String prioridad;
```

**Impacto:**

La exposición de los atributos dificulta controlar las modificaciones y proteger la consistencia de los datos.

**Riesgo de mantenimiento:**

Otros componentes pueden modificar directamente el estado o la prioridad de una solicitud sin respetar las reglas de negocio.

**Mejora propuesta:**

Aplicar **Encapsulate Field**, convirtiendo los atributos en privados e incorporando métodos de acceso y modificación controlados según las necesidades del dominio.

---

### CS-07. Mixed Responsibilities / Acoplamiento entre lógica y presentación

**Ubicación:** `SolicitudManager.java`, líneas 19–49, 213–226 y 228–277, entre otras.

**Prioridad:** Alta.

**Descripción:**

La clase responsable de gestionar solicitudes también presenta mensajes y resultados directamente en la consola.

**Evidencia:**

```java
if (legajo == null || legajo.trim().equals("")) {
    System.out.println("Error: el legajo es obligatorio");
    return -1;
}
```

**Impacto:**

La lógica de negocio queda vinculada a un medio específico de presentación.

**Riesgo de mantenimiento:**

La incorporación de una interfaz gráfica, una API REST o nuevas estrategias de notificación exigiría modificar componentes que deberían concentrarse únicamente en la lógica de negocio.

**Mejora propuesta:**

Separar la presentación de los resultados de las operaciones de negocio. Los servicios deberán comunicar resultados y errores de forma que puedan ser interpretados por la capa de presentación.

---

### CS-08. Silent Failure (errores ignorados)

**Ubicación:** `SolicitudManager.java`, líneas 279–293.

**Prioridad:** Alta.

**Descripción:**

El método `exportarCsv()` captura excepciones generales, pero no informa el error ni permite que otro componente gestione la falla.

**Evidencia:**

```java
} catch (Exception e) {
    // TODO: manejar error
}
```

**Impacto:**

Ante una falla durante la generación del archivo, el usuario puede no recibir información sobre el problema.

**Riesgo de mantenimiento:**

Dificulta diagnosticar fallas, identificar su origen y comprobar si la exportación finalizó correctamente.

**Mejora propuesta:**

Incorporar manejo explícito de excepciones, con información significativa sobre la operación fallida. Evaluar el uso de excepciones específicas de entrada/salida y cierre automático de recursos mediante `try-with-resources`.

---

### CS-09. Complex Conditional (condicionales complejos)

**Ubicación:** `SolicitudManager.java`, líneas 131–181.

**Prioridad:** Media.

**Descripción:**

El método `cambiarEstado()` utiliza múltiples estructuras condicionales anidadas para comprobar las transiciones permitidas y sus requisitos.

**Evidencia:**

```java
if (sol.estado.equals("PENDIENTE")) {
    if (nuevo.equals("EN_PROCESO")) {
        if (sol.resp == null) {
            // ...
        }
    } else if (nuevo.equals("RECHAZADA")) {
        // ...
    }
}
```

**Impacto:**

La estructura dificulta interpretar las reglas de transición y comprobar que todos los escenarios sean tratados correctamente.

**Riesgo de mantenimiento:**

Agregar nuevos estados o modificar las transiciones existentes incrementa la complejidad del método y puede introducir errores de lógica.

**Mejora propuesta:**

Aplicar **Decompose Conditional** y **Extract Method**, separando la validación de transiciones de los cambios efectivos sobre la solicitud.

---

### CS-10. Ausencia de pruebas automatizadas

**Ubicación:** Estructura general del proyecto. No se identificaron archivos de pruebas en `src/test` dentro del ZIP analizado.

**Prioridad:** Alta.

**Descripción:**

El proyecto declara JUnit 5 en `pom.xml`, pero no incluye pruebas automatizadas que verifiquen las operaciones principales.

**Evidencia:**

```xml
<dependency>
    <groupId>org.junit.jupiter</groupId>
    <artifactId>junit-jupiter</artifactId>
    <version>${junit.version}</version>
    <scope>test</scope>
</dependency>
```

La dependencia está configurada, pero no se encontraron clases de prueba en la versión recibida.

**Impacto:**

No existe una batería automatizada de regresión que permita comprobar rápidamente las reglas de negocio después de modificar el código.

**Riesgo de mantenimiento:**

Los refactorings podrían alterar funcionalidades existentes sin que el equipo detecte inmediatamente las regresiones.

**Mejora propuesta:**

Incorporar pruebas unitarias con JUnit 5 para el registro, las validaciones, las asignaciones, las transiciones de estado y las consultas.

Esta debilidad es una carencia de aseguramiento de calidad, no un code smell del código fuente en sentido estricto.

## 3.3. Priorización de la deuda técnica

Se propone intervenir inicialmente los problemas relacionados con la ausencia de pruebas, la concentración de responsabilidades, los métodos extensos, el encapsulamiento y el manejo de errores.

El orden de ejecución deberá priorizar la protección del comportamiento existente mediante pruebas automatizadas, seguida de refactorings pequeños y verificables.

Los problemas de complejidad moderada, como la repetición de búsquedas y el uso de cadenas de texto, podrán abordarse progresivamente durante la reorganización del código.

## 3.4. Conclusión del diagnóstico

El análisis estático permitió identificar nueve code smells y debilidades de diseño, además de una carencia de pruebas automatizadas.

Las principales fuentes de deuda técnica están relacionadas con la concentración de responsabilidades, duplicación de código, acoplamiento, exposición de atributos y falta de control de errores.

Estas características no implican necesariamente que las funcionalidades actuales sean incorrectas, pero aumentan los riesgos asociados al mantenimiento y la evolución del sistema.

El diagnóstico constituye el punto de partida para aplicar los principios SOLID, Clean Code y las técnicas de refactoring exigidas por la cátedra.

Las mejoras deberán respaldarse mediante evidencia del código inicial y refactorizado, junto con pruebas que permitan verificar la conservación del comportamiento externo.