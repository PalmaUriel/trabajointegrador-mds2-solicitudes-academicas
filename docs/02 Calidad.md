# 2. Análisis de calidad externa e interna

## 2.1. Introducción

La calidad del software puede analizarse desde dos perspectivas complementarias: la calidad externa y la calidad interna.

La **calidad externa** se relaciona con el comportamiento observable del sistema y su capacidad para satisfacer las necesidades del usuario. Incluye aspectos como la funcionalidad, la facilidad de uso y la confiabilidad.

La **calidad interna** se refiere a las características del código fuente y su organización, considerando aspectos como la legibilidad, mantenibilidad, modularidad, cohesión y acoplamiento.

Un sistema puede ofrecer las funcionalidades esperadas y, al mismo tiempo, presentar deficiencias internas que dificulten su mantenimiento y evolución.

## 2.2. Análisis de calidad externa

El Sistema de Gestión de Solicitudes Académicas dispone de una interfaz por consola que permite gestionar solicitudes realizadas por estudiantes.

A partir del análisis del código fuente, se identifican las siguientes funcionalidades implementadas:

| Funcionalidad | Comportamiento previsto |
|---|---|
| Registrar solicitudes | Permite ingresar los datos de un estudiante, seleccionar el tipo de trámite y registrar la solicitud. |
| Asignar responsables | Permite asociar un responsable y un área a una solicitud existente. |
| Cambiar estados | Permite actualizar el estado de una solicitud respetando determinadas reglas de transición. |
| Buscar solicitudes | Permite consultar solicitudes por estado, tipo o legajo del estudiante. |
| Generar reportes | Presenta estadísticas de solicitudes por estado y tipo, vencimientos y porcentaje de solicitudes resueltas. |
| Exportar CSV | Permite generar un archivo con la información de las solicitudes registradas. |

El código también contempla validaciones de datos obligatorios, formatos de legajo, tipos de solicitud, fechas y determinadas restricciones de negocio.

Estas características contribuyen a la calidad externa, ya que buscan evitar operaciones incorrectas y proporcionar información útil al usuario.

Sin embargo, el análisis estático también permite reconocer posibles limitaciones:

- El ingreso de identificadores no numéricos en determinadas operaciones puede provocar una excepción no controlada.
- Algunos errores de exportación no generan mensajes visibles, dificultando que el usuario identifique si la operación falló.
- La información se almacena en memoria, por lo que no se conserva al finalizar la ejecución del programa.

Estas observaciones deberán complementarse con las pruebas funcionales y las evidencias de ejecución de la versión inicial.

## 2.3. Análisis de calidad interna

Aunque el sistema implementa las operaciones previstas, su código presenta características que pueden dificultar el mantenimiento y la incorporación de futuras mejoras.

**Concentración de responsabilidades**

La clase `SolicitudManager` reúne diferentes tareas: validación de datos, registro de solicitudes, asignación de responsables, cambios de estado, búsquedas, generación de reportes y exportación de archivos.

Esta concentración reduce la cohesión de la clase y provoca que modificaciones en distintas funcionalidades afecten a un mismo componente.

**Duplicación de lógica**

Los métodos `asignarResponsable()` y `cambiarEstado()` implementan recorridos similares para localizar una solicitud mediante su identificador.

La repetición aumenta el esfuerzo de mantenimiento, ya que una modificación en el mecanismo de búsqueda podría requerir cambios en distintos métodos.

**Métodos extensos y responsabilidades mezcladas**

El método `registrar()` realiza validaciones, determina prioridades y vencimientos, busca estudiantes existentes y crea las solicitudes.

De manera similar, `reporte()` combina el cálculo de estadísticas con la presentación de resultados por consola.

Esto dificulta comprender y probar cada responsabilidad de forma independiente.

**Encapsulamiento insuficiente**

La clase `Solicitud` declara sus atributos como públicos, permitiendo que otras clases modifiquen directamente sus valores sin pasar por mecanismos de control.

Esta situación incrementa el riesgo de generar estados inconsistentes y dificulta centralizar futuras reglas de validación.

**Acoplamiento entre lógica de negocio y presentación**

La clase `SolicitudManager` utiliza instrucciones `System.out.println()` para comunicar resultados y errores.

Esto vincula la lógica del sistema con la interfaz de consola y dificulta su reutilización desde otros medios de interacción, como una interfaz gráfica o una API REST.

**Uso de cadenas de texto para representar estados y tipos**

Los estados, prioridades y tipos de solicitud se representan mediante valores de tipo `String`.

Esta implementación requiere comparaciones repetidas y puede generar inconsistencias ante errores de escritura o modificaciones de los valores permitidos.

## 2.4. Comparación entre calidad externa e interna

| Aspecto | Calidad externa | Calidad interna |
|---|---|---|
| Registro | Permite cargar solicitudes con validaciones. | El método concentra múltiples responsabilidades. |
| Asignación | Permite asociar un responsable a una solicitud. | Repite lógica de búsqueda utilizada por otros métodos. |
| Estados | Controla determinadas transiciones permitidas. | Utiliza condicionales anidados y cadenas de texto. |
| Reportes | Proporciona estadísticas para el usuario. | Mezcla procesamiento de datos y presentación. |
| Exportación | Ofrece una salida en formato CSV. | Presenta manejo insuficiente de excepciones. |
| Mantenimiento | Las funcionalidades actuales están implementadas. | La estructura puede dificultar modificaciones futuras. |

## 2.5. Impacto en la mantenibilidad y evolución futura

Los problemas identificados representan riesgos para la evolución del sistema.

Por ejemplo, si se incorporara un nuevo tipo de solicitud, podrían ser necesarias modificaciones en las validaciones, el cálculo de prioridades, los vencimientos y la generación de reportes.

De igual manera, si se reemplazara la interfaz de consola por una aplicación web, el acoplamiento entre la lógica de negocio y la presentación dificultaría la reutilización del código existente.

Por esta razón, resulta conveniente aplicar técnicas de refactoring que permitan separar responsabilidades, eliminar duplicaciones, mejorar el encapsulamiento y establecer límites más claros entre componentes.

## 2.6. Conclusión del análisis

El Sistema de Gestión de Solicitudes Académicas constituye una base funcional sobre la cual pueden aplicarse mejoras de calidad.

El análisis del código permite identificar funcionalidades orientadas a satisfacer los requerimientos del usuario, pero también evidencia problemas estructurales que afectan la legibilidad, cohesión, mantenibilidad y capacidad de evolución.

Por lo tanto, el objetivo de las siguientes etapas será mejorar la calidad interna mediante la aplicación de principios SOLID, Clean Code y técnicas de refactoring, procurando conservar el comportamiento externo esperado.

La conservación de dicho comportamiento deberá verificarse mediante pruebas ejecutadas sobre las versiones inicial y refactorizada.