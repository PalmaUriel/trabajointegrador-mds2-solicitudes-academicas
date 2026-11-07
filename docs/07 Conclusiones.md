# Etapa 8 — Conclusiones

## 1. Conclusión del equipo

**Un sistema que funciona no es necesariamente un sistema de calidad.** La versión inicial cumplía todas sus funciones en el uso normal, pero al analizarla encontramos 13 code smells y 4 defectos visibles para el usuario. La calidad interna no se ve desde la pantalla, pero define cuánto cuesta cada cambio: por ejemplo, agregar un tipo de solicitud requería modificar 5 lugares en 2 archivos distintos.

**Refactorizar no es corregir errores.** Durante los refactorings mantuvimos a propósito los defectos conocidos (E1 a E4). Así, si el comportamiento cambiaba, sabíamos que el error era del refactoring. Separar las dos actividades hizo que cada cambio fuera más fácil de verificar y de explicar. Entendimos que el refactoring busca mejorar la estructura interna sin alterar lo que el usuario percibe.

**Los cambios chicos son más seguros.** Hicimos cuatro refactorings acotados, uno por rama y por Pull Request, y probamos cada uno antes de aprobarlo. Ninguno alteró el comportamiento del sistema. La revisión entre pares (Code Review) fue clave: detectó problemas de formato que habían quedado de los refactorings y un cambio que entró sin Pull Request. Ninguno afectaba el funcionamiento, pero sí la calidad del código y del proceso.

## 2. Dificultades

- Entender la diferencia entre un bug (falla funcional) y un code smell (señal de mal diseño), y por qué un sistema que "anda" puede tener deuda técnica.
- Trabajar en equipo sobre el mismo repositorio: coordinar ramas, Pull Requests y el orden de los refactorings para no generar conflictos.
- Conservar exactamente el comportamiento externo, incluso los defectos, mientras se modificaba el código.
- Organizar las evidencias: los recorridos de prueba antes y después se hicieron con algunos datos distintos, lo que complicó la comparación visual.

## 3. Límites del trabajo

- Se resolvieron 4 de los 14 problemas detectados. Los más grandes (la clase `SolicitudManager` con demasiadas responsabilidades, los atributos públicos y la lógica mezclada con la consola) quedaron pendientes.
- Las pruebas son manuales; no hay pruebas automatizadas con JUnit (aunque la dependencia esté declarada).
- Los defectos E1 a E4 siguen presentes, porque corregirlos cambiaba el comportamiento externo.
- Los datos se guardan en memoria y se pierden al cerrar el programa.

## 4. Mejoras futuras

1. Dividir `SolicitudManager` en clases con una sola responsabilidad (registro, reportes, exportación).
2. Encapsular los atributos de `Solicitud` y mover allí las reglas de transición de estado.
3. Reemplazar los estados y prioridades en texto por enums.
4. Separar la lógica de negocio de la consola, para poder reutilizarla desde una web o una API.
5. Corregir los defectos E1 a E4 como cambios documentados.
6. Automatizar las pruebas con JUnit.

---

## 5. Conclusiones individuales

### Uriel Palma
Mi aporte se centró en el sistema base, la gestión del repositorio y los refactorings R1 (Extract Method) y R4 (Enum `TipoSolicitud`). Al principio me costó entender por qué teníamos que mantener los errores (E1-E4) durante el refactoring, pero luego comprendí que era la única forma de garantizar que no rompíamos nada. Trabajar con Pull Requests me enseñó la importancia de la revisión cruzada: a veces uno está tan enfocado en su código que no ve errores de formato o de diseño obvios. Me llevo como aprendizaje que la deuda técnica no es un concepto abstracto, sino algo que se ve en los métodos largos y en el acoplamiento.

### Lorenzo Pattini
Me encargué de la descripción inicial del sistema, del refactoring R2 (Move Method) y de las evidencias de funcionamiento. El mayor desafío fue entender el concepto de "Feature Envy" y darme cuenta de que la lógica de "qué es una solicitud cerrada" estaba en el manager en lugar de estar dentro de la propia clase `Solicitud`. Mover esos métodos fue un cambio pequeño pero que dejó el código mucho más limpio. También aprendí que documentar el sistema base no es solo listar funciones, sino descubrir las reglas de negocio que están escondidas en los `switch` y los `if/else`.

### Federico *(completar apellido)*
Mi trabajo se enfocó en el análisis de calidad, la identificación de code smells y el refactoring R3 (Introduce Parameter Object). El diagnóstico me permitió ver que los problemas no estaban en que el sistema fallara, sino en cómo estaba estructurado. El refactoring R3 fue especialmente útil para entender que agrupar parámetros en un objeto (`DatosRegistroSolicitud`) no solo mejora la legibilidad, sino que también prepara el código para futuras extensiones sin modificar la firma del método. Aprendí que el Code Review es una herramienta de aprendizaje mutuo y no una auditoría.

---

## 6. Participación

| Integrante                              | Aportes principales |
|-----------------------------------------|---|
| Uriel Palma                             | Sistema base, evidencias de funcionamiento, refactorings R1 (Extract Method) y R4 (Enum TipoSolicitud), pruebas antes/después, code review, gestión del repositorio. |
| Lorenzo Pattini                         | Descripción del sistema base, refactoring R2 (Move Method), evidencias de refactoring, tags. |
| Federico Quinteros | Análisis de calidad y code smells, refactoring R3 (Introduce Parameter Object). |
