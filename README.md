# JavaFX_Hilos_Practica

Proyecto independiente para la asignatura **Programación de Aplicaciones de Escritorio** de la **Universidad Americana**.

## Objetivo

Demostrar el uso de `Thread` y `Runnable` en JavaFX sin bloquear la interfaz gráfica. La aplicación simula dos procesos:

- **Clientes:** 100 registros, 100 ms por registro, duración aproximada de 10 segundos.
- **Productos:** 50 registros, 150 ms por registro, duración aproximada de 7,5 segundos.

Los registros son simulados. No hay formularios, tablas, persistencia, PostgreSQL ni dependencia de otro proyecto.

## Requisitos

- Java 21.
- Maven.
- JavaFX 21.0.6, descargado por Maven.

## Comandos

Compilar:

```bash
mvn clean compile
```

Ejecutar:

```bash
mvn javafx:run
```

Con Maven Wrapper en Windows:

```bash
.\mvnw.cmd clean compile
.\mvnw.cmd javafx:run
```

## Estructura

```text
pom.xml
src/main/java/module-info.java
src/main/java/ni/edu/uam/practicahilos/MainApp.java
src/main/java/ni/edu/uam/practicahilos/ProcesadorController.java
src/main/resources/ni/edu/uam/practicahilos/procesador-view.fxml
README.md
.gitignore
```

## Demostración

1. Abrir la aplicación con `mvn javafx:run`.
2. Para la práctica básica, presionar **Iniciar** en la sección **Procesamiento de clientes**.
3. Observar que procesa 100 registros simulados y finaliza cerca de los 10 segundos.
4. Para el reto final, presionar **Iniciar** en **Clientes** y **Productos**.
5. Observar que ambos progresos avanzan simultáneamente porque cada proceso usa su propio `Thread`.
6. Probar **Detener** en una sección y verificar que la otra continúa.
7. Probar **Reiniciar** después de detener o finalizar y verificar que solo se restablece esa sección.

## Explicación técnica

### Thread y Runnable

`Runnable` define el trabajo que se ejecutará en segundo plano. En este proyecto, cada proceso crea un `Runnable` con el ciclo que simula registros y pausas.

`Thread` representa el hilo real de ejecución. Cada vez que se presiona **Iniciar**, se crea un objeto `Thread` nuevo para ese proceso activo.

### Diferencia entre start() y run()

`start()` crea un nuevo hilo y ejecuta el `Runnable` fuera del hilo de JavaFX.

`run()` solo llama el método como una función normal en el hilo actual. Si se llamara `run()` desde el evento del botón, la interfaz se congelaría durante el procesamiento.

### Thread.sleep()

`Thread.sleep()` simula el tiempo que tarda cada registro:

- Clientes: `Thread.sleep(100)`.
- Productos: `Thread.sleep(150)`.

La espera ocurre dentro del trabajador, no dentro del evento JavaFX.

### Platform.runLater()

JavaFX no permite actualizar controles visuales directamente desde hilos secundarios. Por eso, el trabajador calcula el avance y envía los cambios de etiquetas, barras y contadores con `Platform.runLater()`.

### Detención con interrupt()

El botón **Detener** llama `interrupt()` sobre el trabajador activo. El ciclo revisa `isInterrupted()` y también maneja `InterruptedException` cuando la interrupción ocurre durante `Thread.sleep()`.

Al capturar `InterruptedException`, el código restaura la marca con `Thread.currentThread().interrupt()` y finaliza el proceso de forma ordenada.

### Reinicio

**Reiniciar** restablece únicamente la sección seleccionada:

- Progreso: `0%`.
- Contador: `0/total`.
- Estado: `Esperando inicio`.

No inicia automáticamente un nuevo proceso.

### Por qué no se reutiliza un Thread terminado

Un objeto `Thread` en Java solo puede iniciarse una vez. Cuando termina, no se puede volver a llamar `start()` sobre el mismo objeto. Por eso cada nueva ejecución crea un `Thread` nuevo.

### Cierre de la ventana

Al cerrar la ventana y también desde `Application.stop()`, la aplicación solicita la interrupción de ambos trabajadores. La limpieza puede ejecutarse más de una vez sin errores.

El controlador usa un identificador de ejecución para evitar que actualizaciones pendientes de una ejecución anterior modifiquen una ejecución nueva.

## Por qué no ejecutar procesos prolongados en eventos JavaFX

Los eventos de botones se ejecutan en el hilo principal de JavaFX. Si dentro de un evento se hace algo como:

```java
Thread.sleep(5000);
```

la ventana queda congelada durante 5 segundos: no responde a clics, no repinta bien y no actualiza barras ni etiquetas. Por eso los procesos prolongados deben ir en hilos trabajadores y las actualizaciones visuales deben enviarse con `Platform.runLater()`.

## Evidencias

No se incluyen capturas fabricadas. Capturas pendientes para la entrega:

- Estado inicial.
- Ambos procesos avanzando.
- Uno detenido y el otro activo.
- Ambos finalizados.

## Validación esperada

- Compilación del proyecto con Maven.
- Carga correcta del FXML.
- Clientes completa 100 registros.
- Productos completa 50 registros.
- Ambos procesos avanzan simultáneamente.
- La ventana responde durante el procesamiento.
- Cada proceso se detiene individualmente.
- Reiniciar restablece solo su sección.
- Pulsaciones repetidas no duplican trabajadores.
- Detener y volver a iniciar no muestra avances antiguos.
- Cerrar durante el procesamiento solicita la detención de trabajadores.
