# Java Virtual Threads & Concurrency MasterClass

Del curso Udemy: https://www.udemy.com/course/java-virtual-thread

## Introduction

**Threads: La columna vertebral de la concurrencia**

- Cada línea de código se ejecuta en un thread.
- Fundamentos del modelo de concurrencia de Java.

**Como Java maneja las peticiones**

Digamos que construimos la aplicación con el módulo Spring Web.

- Cada request de entrada -> asignado a un thread.
- Threads ejecuta la petición concurrentemente.
  - Si esperamos más peticiones concurrentes, entonces el servidor necesita suficientes threads para manejarlos.
  - Por defecto, Spring Web con Tomcat provee 200 threads, es decir, pueden manejarse a la vez 200 peticiones concurrentes.

**¿Qué ocurre cuando se hacen muchas peticiones?**

- Si se envían más de 200 peticiones a la vez, por ejemplo 210, 10 peticiones esperan en una cola hasta que un thread queda disponible para procesar la petición.
- Se incrementa la latencia.
- Los usuarios experimentan respuestas más lentas.

**¿Por qué no podemos sencillamente añadir más threads?**

¿Por qué no añadimos un millón de threads y nos olvidamos de problemas? Realmente esto no funciona.

Tenemos un problema con los Platform Threads:

- Platform Threads = OS Threads.
  - `Thread thread = new Thread();` Cada sentencia de estas genera un OS Thread.
- Cada Thread necesita su propio memory stack, normalmente en MB. No es práctico crear miles de ellos.
- Pesado y caro de crear.
- El Sistema Operativo limita cuántos threads pueden ejecutarse.
- No está contruido para concurrencia a gran escala.

Los Platform Threads consumen demasiada memoria y no pueden escalarse para gestionar una concurrencia masiva.

Esta limitación ha constituido desde siempre un desafio al modelo de concurrencia de Java.

**Virtual Threads**

En Java 21, se han añadido `Virtual Threads`:

- Ligeros y eficientes en memoria.
- Permiten concurrencia masiva.
  - Podemos crear miles o millones de ellos sin agotar nuestro sistema.
- Pero...**¡¡hay truco!!**
  - Los Virtual Threads no son siempre un interruptor mágico para un mejor rendimiento.

Los `Virtual Threads` brillan en ciertas situaciones. Aprenderemos cuando son de ayuda y cuando no deberíamos utilizarlos.

**Estructura del curso - Paso a paso**

![alt Course Structure](./images/01-CourseStructure.png)