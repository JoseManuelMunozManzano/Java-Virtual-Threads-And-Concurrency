# Virtual Threads

## Project Setup

Creamos el proyecto para el estudio de `Virtual Threads` en IntelliJ con la siguiente configuración:

![alt Project Setup](./images/01-ProjectSetup.png)

## Deep Dive Into Virtual Threads

### Need For Virtual Threads

Necesitamos comprender como funciona el Sistema Operativo a alto nivel. Esto es muy importante para comprender el comportamiento de los virtual threads.

Ver: https://github.com/JoseManuelMunozManzano/Mastering-Java-Reactive-Programming?tab=readme-ov-file#process--thread--ram--cpu--scheduler

El problema que tratamos de resolver es el siguiente:

- Creamos threads, que son caros de crear, pero los mantenemos ociosos durante mucho tiempo.

Pero los `Virtual Threads` no son `OS Threads`, asi que, ¿cómo podemos hacerlos ejecutar? Vamos a verlos internamente, como funcionan bajo el capó, en esta sección.

### Platform Thread Creation Limit - Demo

Ya hemos visto que en una arquitectura de microservicios tenemos muchas llamadas de red. A menudo, un thread se bloqueará si incrementamos el número de threads. Tenemos que asignar un tamaño de stack para el thread. Y es caro.

Vamos a probar si realmente es caro crear threads. Una vez conozcamos el problema, podremos entender mejor la solución.

Para evitar confusiones, vamos a llamar a los threads originales `platform threads` y luego tenemos los `virtual threads`.

Para simular llamadas de red lentas, usaremos `Thread.sleep()` en las primeras secciones, para comprender mejor los problemas. Más tarde desarrollaremos una aplicación usando el módulo `Spring Boot web`.

En `src/java/com/jmunoz/sec01` creamos las clases siguientes:

- `Task`: Creamos un método que simula una llamada de red lenta.
- `InboundOutboundTaskDemo`: Creamos un bucle de Threads originales de Java (Platform Threads) que llaman a `Task`, para ver como de costoso es crear Threads.
  - Si indicamos que queremos crear 50_000 platform threads, dará un error `out of memory` o `process/resource limits reached`. Esto indica que hay restricciones a la cantidad de platform threads que se pueden crear.

### Thread Builder - Factory Method

En la clase anterior vimos la forma en la que se creaban anteriormente los threads.

Ahora, Java provee varios `Factory methods` para crear threads.

Vemos como crear `platform threads` non-daemon, donde el hilo principal de ejecución espera a que se terminen de ejecutar todos los hilos.

También vemos como crear `platform threads` daemon, donde el programa termina y los threads seguirán ejecutándose.

**How To Make Our Application Wait?**

![alt How To Make Our Application Wait?](./images/02-HowToMakeOurApplicationWait.png)

En nuestra demo de un `daemon thread`, nuestro `main thread` crea 10 threads en segundo plano y finaliza inmediatamente, sin esperar a que los `daemon threads` completen su tarea.

¿Cómo podemos hacer que nuestra aplicación espere en este caso? Si tenemos que esperar a que la aplicación haga el trabajo, ¿para qué creamos `daemon threads`?

De todas formas, si queremos que nuestra aplicación espere, se puede hacer.

**CountDownLatch**

![alt CountDownLatch](./images/03-CountDownLatch.png)

Podemos usar `CountDownLatch` para esperar hasta que los threads en segundo plano completen su tarea. Se le pasa el número de tareas a completar, por ejemplo, en la imagen son 3 threads.

Tenemos que indicar `latch.await()` también, para que realmente espere a que esos 3 threads se completen.

¿Cómo sé que la tarea de un thread en segundo plano se ha completado? Cada uno de los threads tiene que indicar que ha terminado. De ahí el método `countDown()` de la imagen. Cuando llegue a 0 entonces la aplicación termina.

En `src/java/com/jmunoz/sec01` modificamos las clases siguientes:

- `InboundOutboundTaskDemo`
  - Añadimos un método para mostrar como se crean threads actualmente, usando `factory methods`. Siguen siendo platform threads, no virtual threads, y el hilo principal de la app espera a que se completen los threads para terminar el programa.
  - Añadimos otro método cuyos threads se ejecutan en segundo plano, de forma que la app termina aunque el thread siga ejecutándose.
  - Añadimos otro método usando `CountDownLatch` para que, aunque sean `daemon threads` la aplicación espere a que todos los threads hayan hecho su trabajo para terminar.

### Virtual Thread Scaling - Demo

Ya hemos visto el problema de los `platform threads`, que existe un límite de threads que se pueden crear debido a los recursos del sistema.

Ahora podemos empezar a hablar de los `virtual threads`.

Java ha introducido una nueva clase llamada `VirtualThread` que extiende el `Thread` original (Platform Thread). Al hacerlo así, el `virtual thread` se ve exactamente igual que el thread original, pero es virtual.

- Thread (Platform Thread)
  - abstract BaseVirtualThread
    - VirtualThread

![alt Virtual Thread](./images/04-VirtualThread.png)

Usando el método que puede verse en la imagen, podemos pasar tanto un platform thread como un virtual thread.

Pero, ¿Qué es un virtual thread? ¿Cómo funciona?

- public Thread (Platform Thread)
  - abstract BaseVirtualThread
    - (package private) Virtual Thread

`Platform Thread` es una clase pública, por eso podemos crearlos usando el operador `new`.

`Virtual Thread` es package private, así que no podemos crearlo usando el operador `new`. Es por esto que Java introdujo los builder de thread que ya hemos usado para crear platform threads.

- Thread.Builder
  - Thread.ofPlatform()
  - Thread.ofVirtual()

`Thread.Builder` es una nueva interface y tenemos dos implementaciones diferentes, una para crear `Platform Threads` y la otra para crear `Virtual Threads`.

- Los virtual threads son daemon threads por defecto. No se pueden crear como non-daemon threads.
- Los virtual threads no tiene nombre por defecto.

![alt Thread.Builder](./images/05-ThreadBuilder.png)

Si tenemos un método como el de la imagen, que acepta un builder, podemos crear un `unstarted thread` que sea un platform thread o un virtual thread.

En `src/java/com/jmunoz/sec01` modificamos las clases siguientes:

- `InboundOutboundTaskDemo`
  - Añadimos un método donde creamos `virtual threads` usando el builder (no se puede con new).

### How Virtual Thread Works

¿Cómo es posible crear un millón de virtual threads sin que ocurra el error `out of memory error`?

- Los Virtual Threads son sencillamente una ilusión creada por Java.
  - Parece un thread
  - Acepta un runnable
  - Podemos ejecutar thread.start() / thread.join()
  - Pero... **El sistema operativo no puede verlos / planificarlos (schedule)**

Si el Sistema Operativo ni siquiera los ve, ¿cómo funcionan?

![alt Our Machine](./images/06-OurMachine.png)

Para comprender los virtual threads, consideremos la imagen como si fuera nuestra máquina.

Tenemos un único procesador (CPU), por lo tanto, podemos planificar (schedule) un único thread a la vez (el muelle en el dibujo representa el thread).

En azul vemos lo que sería nuestro código Java, y en verde sería la memoria.

Los lenguajes de programación son la forma a través de la cual interaccionamos con el sistema operativo para que este pase instrucciones que se consiguen ejecutar.

![alt Our Machine - Platform Thread](./images/07-OurMachinePlatformThread.png)

En los ejemplos con Platform Threads, intentamos crear miles de threads Java. Debido a esto, se intentaban crear demasiados OS threads y daba el error de que no se podían crear más threads.

![alt Our Machine - Virtual Thread](./images/08-OurMachineVirtualThread.png)

Pero cuando creamos Virtual Threads, lo que creamos son **objetos**. En la parte derecha de la imagen vemos como se crea un objeto person.

Los virtual threads son lo mismo, pero en vez de crear uno, podemos crear millones de objetos usando un bucle (por ejemplo).

Como vemos en la parte izquierda de la imagen, no se crea nada a nivel de sistema operativo. Los objetos se crean en el heap (la memoria en verde)

Para comprender los virtual threads, debemos dejar de verlos como threads.

![alt Our Machine - Virtual Thread -> Task](./images/09-OurMachineVirtualThreadTask1.png)

Tenemos que ver los virtual threads como **tareas**.

Los `virtual threads` aceptan `runnable`, así que todos tienen algún tipo de acciones para ser ejecutadas. Por tanto, parece como si usáramos threads, los arrancamos, y parece que hacemos algo con ellos como haríamos con los threads.

![alt Our Machine - Virtual Thread -> Task](./images/10-OurMachineVirtualThreadTask2.png)

Sin embargo, cuando ejecutamos `thread.start()`, lo que realmente ocurre es que todas estas tareas (los virtual threads) se añaden a una cola interna.

Recordar que el procesador no puede ejecutar por si solo los virtual threads porque el sistema operativo ni siquiera los ve. Lo que tenemos es un `fork-join pool`. Es un `fork-join pool` separado, no es el `pool` común.

El número de threads en el `fork-join pool` depende del número de procesadores que tenemos en nuestra máquina.

En nuestra imagen de ejemplo, tenemos un solo procesador, así que imaginemos que tenemos un único thread en el `fork-join pool`. Ese thread si es un platform thread (el muelle en la imagen).

Este platform thread lo que hace es coger la tarea de la cola y la comienza a ejecutar.

Es decir, por detrás, todos los virtual threads acaban siendo ejecutados en un platform thread.

De nuevo, depende del número de procesadores. Si tenemos 10, tendremos 10 threads en el `fork-join pool`.

Podemos ahora preguntar, como parte del runnable, hemos dado `thread.sleep()`. Si lo que ejecuta la tarea es un platform thread, ¿debería bloquearse, no?

¿Cómo es posible ejecutar millones de `thread.sleep()` en paralelo? Aquí es donde Java hace la magia.

![alt Our Machine - Blocking IO Task](./images/11-OurMachineBlockIOTask.png)

Entonces, el platform thread coge la tarea y comienza a ejecutarla.

![alt Our Machine - Action Parking](./images/12-OurMachineActionPark.png)

Cuando ve `thread.sleep()` o algún tipo de llamada de red, coge la tarea y la lleva de nuevo a la memoria. A esto se le llama `action parcking`.

Es decir, el objetivo final es que el platform thread **no se bloquee**. Mientras haya tareas, ejecutará. Cuando ve algo bloqueante, hasta que no obtenga la respuesta, la tarea se aparca en memoria y cojo otra tarea.

Al platform thread se le llama `Carrier Thread`.

Como los virtual threads son objeto pequeños en el heap, y no pueden ejecutarse directamente por sí mismos, los virtual threads tienen que montarse en un `Carrier Thread` para poder ejecutar la tarea (task). A esta acción se le llama `action mounting`.

Luego, se ejecuta la tarea como parte del runnable. Cuando tiene que ejecutar una acción bloqueante, como una llamada de red a un microservicio, el virtual thread se desmonta (`unmounted`) y se aparca en el heap. El siguiente virtual thread se monta y se ejecuta su tarea.

Como se ha dicho, el `Carrier Thread` no debe quedar ocioso.

![alt Our Machine - Unpark](./images/13-OurMachineUnpark.png)

¿Qué ocurre cuando obtenemos la respuesta del microservicio? El virtual thread está desmontado y aparcado en el heap, y el `carrier thread` ya está ejecutando otra tarea.

Lo que se hace es el proceso de `unpark`, añadiendo de nuevo el virtual thread a la cola, para que pueda volver a montarse en el `carrier thread` y pueda seguir ejecutándose desde el sitio donde paró.

Así es como, desde un punto de vista de alto nivel, los virtual threads se planifican para su ejecución.

![alt Virtual Threads Methods](./images/14-VirtualThreadsMethods.png)

Estos métodos, si los ejecuto, ¿qué nombre / estado obtengo? ¿El nombre/estado del virtual thread o el de carrier thread?

Nosotros, los programadores, tenemos al ejecutar, dos threads, el virtual y el platform. Cuando ejecutamos un virtual thread, deberíamos obtener el nombre / estado del virtual thread.

¿Cómo funciona?

![alt Virtual Threads Mount](./images/15-VirtualThreadsMount.png)

Si miramos el código fuente de Java, el método `mount()`, lo que hace es que, tras montar el virtual thread en el carrier thread, también modifica el carrier thread de esta forma:

`carrier.setCurrentThread(this);`

Asi se provee la información del virtual thread durante su ejecución.

Durante el proceso de desmontado, se limpia esta información.

![alt Virtual Threads Sleep](./images/16-VirtualThreadSleep.png)

Si tomamos `Thread.sleep()` y vemos el código fuente de Java, veremos que, para un virtual thread, lo que ejecuta es `vthread.sleepNanos(nanos)`, que realiza la acción `parking` y planifica la acción de `unparking` para que vuelva a la cola. Así parece que se hace el `sleep` durante esos nanosegundos.

Si es un platform thread lo que ejecuta es `sleep0(nanos)`, que es bloqueante.

¿Y qué pasa con el `stack memory`? Esto lo iremos viendo en las siguientes clases.

### Carrier Threads Demo

En la clase anterior indicamos que los virtual threads se ejecutaban a través de `carrier threads`.

Ejecutar la siguiente prueba (aquí no hay desarrollos):

- `InboundOutboundTaskDemo`: Ejecutar `platformThreadDemo1()` y notar los nombres de los threads. 
- `InboundOutboundTaskDemo`: Ejecutar `virtualThreadDemo()` y notar que aparecen logs del tipo `VirtualThread[#46,virtual-5]/runnable@ForkJoinPool-1-worker-5`
  - Vemos que los virtual threads se ejecutan por `forkjoinpool worker threads`.
  - Vemos que el mismo `forkjoinpool worker threads` se ejecuta para varias tareas (park y unpark).
  - Pero no está garantizado que el mismo worker que trabaja en una tarea, tras hacer park, vaya a hacer el unpark. Lo normal es que sea otro worker thread (carrier thread), para que siempre estén ocupados.
  - Esto prueba que los virtual threads son una ilusión y que por debajo no deja de ejecutarse un thread.

### Virtual Thread & Stack

Vamos a ver cómo trabaja la `stack memory` con los virtual threads, porque ya sabemos que los platform threads tendrán algo llamado `stack memory` para almacenar los métodos locales, variables, información de métodos llamados, etc.

**Stack Size**

- Platform Threads tienen un tamaño de stack fijo (1Mb / 2Mb...)
  - Aunque el tamaño se puede ajustar, hay que darlo por adelantado, antes de crear los threads. Después no puede ajustarse.
- Virtual Threads NO tienen un stack fijo. Tienen un stack redimensionable.
  - Se le llama: Objeto de fragmento de pila (`stack chunk object`)

![alt Virtual Threads And Stack 1](./images/17-VirtualThreadsAndStack1.png)

Imaginemos que la clase de Virtual Thread está implementada (es pseudocódigo) como muestra la parte izquierda de la imagen.

En la parte derecha y arriba de la imagen vemos que hemos creado tres virtual threads (o tres tareas) en el heap.

La parte azul es el runnable task y, en blanco están los virtual stack, inicialmente en null, límpios, porque no se han ejecutado.

En la parte de abajo de la imagen tenemos un único procesador. El palo vertical es el carrier thread, y el cuadrado a su lado es su correspondiente stack memory. Este es el stack memory fijo de 1Mb o 2Mb (da igual).

Cuando ejecutamos `thread.start()` estos tres virtual threads se añaden a la cola, listos para ser recogidos por el carrier thread para ejecutarse.

![alt Virtual Threads And Stack 2](./images/18-VirtualThreadsAndStack2.png)

Imaginemos que el carrier thread coge la primera tarea y comienza a ejecutarla. Cargará el runnable o comenzará a ejecutar el virtual thread task.

Como parte del runnable, dentro, puede que estemos creando muchos objetos y que tengamos muchos métodos a los que llamar de uno en uno, así que, en nombre del virtual thread, el carrier thread lo hará todo.

![alt Virtual Threads And Stack 3](./images/19-VirtualThreadsAndStack3.png)

Durante la ejecución, el carrier thread usará el stack memory para guardar la información al método llamado, referencias de objetos, la traza, todo se almacenará en el stack memory.

Imaginemos ahora que, a mitad del runnable, tenemos que llamar al product service para obtener información de un producto.

![alt Virtual Threads And Stack 4](./images/20-VirtualThreadsAndStack4.png)

De parte del virtual thread, el carrier thread hace la llamada de red, haciendo la petición a product service.

![alt Virtual Threads And Stack 5](./images/21-VirtualThreadsAndStack5.png)

El carrier thread se da cuenta rápidamente que es una llamada bloqueante, y, como no quiere estar bloqueada, lo que hace es que, la información que haya en el stack memory, la coge y la almacena. 

![alt Virtual Threads And Stack 6](./images/22-VirtualThreadsAndStack6.png)

Y la aparca en el heap. El stack memory queda limpio.

Luego, el carrier thread coge la siguiente tarea para ejecutarse. Y así una y otra vez.

![alt Virtual Threads And Stack 7](./images/23-VirtualThreadsAndStack7.png)

Eventualmente, cada virtual thread, su correspondiente información del stack acabará en el heap.

Es por esto que decimos que un virtual thread tiene un stack redimensionable (una tarea pueden ser 10Kb, la otra 100Kb...)

![alt Virtual Threads And Stack 8](./images/24-VirtualThreadsAndStack8.png)

Ahora obtenemos la respuesta de product service. Se hace el unpark y se añade la tarea (task) de nuevo a la cola para ejecutarse.

![alt Virtual Threads And Stack 9](./images/25-VirtualThreadsAndStack9.png)

El carrier thread cogerá la tarea y comenzará de nuevo la ejecución, desde el sitio donde se paró.

![alt Virtual Threads And Stack 10](./images/26-VirtualThreadsAndStack10.png)

Vemos que no perdemos el contexto, porque la información, objetos, variables, referencias, todo estaba guardado en el heap. Ahora lo volvemos a cargar en el stack memory y continuamos la ejecución.

Y así es como funciona todo esto.

Este funcionamiento tiene un coste, pero es mejor que gestionar miles de platform threads.

### Getting Stack Trace

En esta demo, vamos a tener una task que tiene varios métodos con llamadas encadenadas.

Es decir, habrá un método 1, que llamará al método 2, y este método 2 llamará a un método 3...

Tendremos también algunas excepciones y veremos el stack trace y si podemos hacer debug.

Todo esto, usando virtual threads.

En `src/java/com/jmunoz` creamos los paquetes/clases siguientes:

- `sec02`
  - `Task`: Creamos métodos que llaman a otros métodos de forma encadenada, y que pueden lanzar excepciones.
    - Vemos que, tanto usando Platform Threads como Virtual Threads, vemos la stack trace, para propósitos de debug, sin problemas.
  - `StackTraceDemo`: Clase main.
- `util`
  - `CommonUtils`: 
    - Método `sleep()`: Vamos a usar mucho Thread.sleep() y no quiero tener que hacer el catch de InterruptedException cada vez.

### CPU Intensive Task

Platform Threads vs Virtual Threads. ¿Cómo se comportarán en tareas de CPU intensivas? Es decir, tareas que no tienen IO, sino muchísima computación.

Vamos a hacer algunas pruebas para poder comparar como trabajan ambos tipos de hilo cuando hay tareas de CPU intensivas.

Vamos a considerar la secuencia de Fibonacci, donde cada número es la suma de los dos números anteriores:

`0, 1, 1, 2, 3, 5, 8, 13, 21, 34, 55, 89, 144, 233, 377, 610, 987 ...`

En las pruebas, pasaremos la posición, por ejemplo 5, para saber cuál es el quinto número en la secuencia de Fibonacci. Asumiendo un array que comienza en 0, el quinto número en la secuencia de Fibonacci es el valor 5. El octavo número sería 21...

El código que vamos a hacer es malo y lento a propósito, un algoritmo 2 ^ N, con muchas llamadas recursivas, para que haga mucho cálculo de CPU.

En la vida real, un cálculo intensivo de CPU se puede lograr haciendo serializaciones / deserializaciones, procesamiento de imágenes, procesamiento de video, etc.

En `src/java/com/jmunoz` creamos los paquetes/clases siguientes:

- `sec03`
  - `Task`: Demo con tarea de CPU intensiva para ver como se comportan los Platform Threads vs VirtualThreads.
  - `CPUTaskDemo`: Clase main.
- `util`
  - `CommonUtils`:
    - Método `timer()`: Cuando tarda en ejecutarse un runnable.

El comportamiento observado es el siguiente:

- Platform Threads: Con 1 task o 5 task, el tiempo es más o menos el mismo, unos 3sg.
  - Con el número de procesadores que tiene la máquina, cada tarea tarda en completarse unos 5sg.
  - Si multiplicamos el número de procesadores por 2, vemos que el tiempo que tarda en completar cada tarea es de unos 7.5sg.
  - Si multiplicamos el número de procesadores por 3, vemos que el tiempo que tarda en completar cada tarea es de unos 11.3sg.
  - Esto es porque el scheduler, cada poco, va cambiando de tarea entre los distintos threads creados. Por ejemplo, cuando multiplico por 3, cada procesador alterna entre tres hilos, ejecutándolos poco a poco.
- Virtual Threads: Con 1 task o 5 task, el tiempo es más o menos el mismo, unos 3sg.
  - Con el número de procesadores que tiene la máquina, cada tarea tarda en completarse unos 5sg.
  - Pero, si multiplicamos el número de procesadores por 2 o por 3, vemos que el tiempo no se duplica ni triplica. Sigue siendo de unos 5sg.
  - Esto es porque no crea todos los threads desde el principio. Crea 16 threads (los procesadores que tiene mi máquina) y el Carrier Thread ejecuta las tareas. Conforme va completando una tarea, otra nueva tarea (están en el heap) es recogida por ese Carrier Thread.
  - Como no hay llamadas de red (operaciones IO) no se hace park ni unpark.
  - No es que sea más rápido, es que su comportamiento es más inteligente. No crea hilos que no va a usar, sino que espera que el máximo de hilos de mi máquina (los Carrier Threads) esté siempre trabajando.

Visualmente:

![alt CPU Intensive - Platform Thread](./images/27-CPUIntensivePlatformThread.png)

Esto es como se comportan dos Platform Threads, donde se va cambiando entre los dos threads hasta terminar su ejecución.

![alt CPU Intensive - Virtual Thread](./images/28-CPUIntensiveVirtualThread.png)

Y así se comportan dos Virtual Threads, donde un Carrier Thread ejecuta una tarea y luego la otra.

Al final, una forma de comportamiento de ejecución distinta, pero que tarda lo mismo.

Los virtual threads NO SON MAS RÁPIDOS que los platform threads.

Si tenemos tareas de CPU intensivas, no tiene sentido cambiar de platform threads a virtual threads. No va a mejorar el rendimiento en nada.

Más adelante veremos que, en operaciones con muchas operaciones IO, como llamadas a microservicios, si que tiene sentido usar virtual threads.

### Virtual Thread - Scheduler Config

- Los Platform Threads son planificados por el OS Scheduler.
- Los Virtual Threads son planificados por la JVM
  - Hay un `ForkJoinPool` dedicado para planificar Virtual Threads.
  - Este `ForkJoinPool` tiene threads para igualar el número de procesadores disponible.
    - core pool size = available processor
    - Los Carrier Threads NO se bloquean durante operaciones I/O

![alt Virtual Thread Scheduler Config](./images/29-VirtualThreadSchedulerConfig.png)

El máximo número de threads que se pueden tener en el pool de una aplicación son 256. Aunque más adelante jugaremos con este valor, no tenemos que modificarlo en aplicaciones de producción.

### Preemptive vs Cooperative Scheduling

Cuando tenemos varios threads y un procesador limitado, todos los threads competirán por CPU. Sin embargo, solo un thread puede ejecutarse a la vez.

Tenemos varias políticas de planificación para decidir que thread ejecutar.

Hay dos tipos de planificación (scheduling):

- Preemptive
  - Esto es lo que hace el planificador del SO, y lo que normalmente vemos para platform threads, ya que estos son OS threads.
- Cooperative
  - Es difícil de conseguir con platform threads, pero es usada por virtual threads.

**Scheduling:Preemptive**

- Preemptive - Política OS Scheduling
  - La CPU está asignada por un tiempo limitado a un thread.
  - El OS puede pausar por la fuerza un thread en ejecución dando CPU a otro thread.
  - Esto basado en thread-priority, time-slice, availability of ready-to-run threads, etc.
  - Platform threads pueden tener prioridades (`thread.setPriority(6)`).
    - 1 es baja prioridad
    - 10 es alta prioridad
    - 5 es el valor por defecto
    - Esto no lo hacemos nosotros, lo suele hacer el framework por debajo.
  - Nota:
    - El comportamiento de planificación Preemptive es dependiente de la plataforma.
    - Los Virtual Threads tienen la prioridad por defecto (valor 5). NO PUEDE MODIFICARSE.

![alt Scheduling: Preemptive](./images/30-SchedulingPreemptive.png)

La ventaja de este planificador es que da oportunidades a todos los threads.

La desventaja es que tenemos muchos cambios de contexto.

**Scheduling:Cooperative**

- La CPU está asignada hasta que la ejecución se completa.
  - O el thread está dispuesto a dar CPU a otro thread (`Thread.yield()`).
    - A este comportamiento se le llama `behaviour yielding`.
- La ejecución no se interrumpe ni se pausa de forma forzada.
- Si hay un thread/task de larga ejecución, otros threads tienen que esperar.

![alt Scheduling: Cooperative](./images/31-SchedulingCooperative.png)

Es por este comportamiento de que los threads tienen que esperar a que termine el que se está ejecutando, que es mejor usar platform threads cuando la tarea es de uso intensivo de CPU.

De nuevo, los virtual threads son mejores para I/O, no para uso intensivo de CPU.

**Cooperative - Yield**

![alt Cooperative - Yield](./images/32-CooperativeYield.png)

Si tenemos un virtual thread que se está ejecutando en una CPU, dicho thread puede indicar al scheduler que ya llevo bastante tiempo ejecutándose y que necesita más tiempo. Si hay más threads esperando, dales una oportunidad y yo continuo más tarde.

Esta comunicación se consigue usando el método `Thread.yield()`.

Este método es muy antiguo y también se puede usar en platform threads, aunque el OS scheduler lo va a ignorar. Por eso, este comportamiento no lo vemos en platform threads. Este método es más una sugerencia que una orden.

Pero para virtual threads el scheduler es la JVM, no el SO, así que la JVM comprende y acepta `Thread.yield()` como una orden. El carrier thread cogerá esa tarea y la llevará de vuelta a la cola y cogerá la siguiente tarea para ejecutarla.

Lo normal es no tener que usar `Thread.yield()` en aplicaciones reales.

### Cooperative Scheduling Demo

En `src/java/com/jmunoz` creamos los paquetes/clases siguientes:

- `sec04`
  - `CooperativeSchedulingDemo`: Una demo para comprender la planificación cooperative. NO tendremos que usarlo en aplicaciones reales. 

### How Virtual Threads Can Help?

Después de todo lo que hemos visto, ¿cómo pueden ayudarme los virtual threads?

![alt Microservice Communication](./images/33-MicroservicesCommunication.png)

Ya hemos visto antes este ejemplo. Imaginemos que recibimos una petición para procesar una orden.

Tenemos que llamar a `product-service`, `payment-service` y `shipping-service`.

![alt Synchronous Blocking Style Code](./images/34-SynchronousBlockingStyleCode.png)

Así es como normalmente escribiríamos código, es un estilo síncrono bloqueante. El comentario I/O hace referencia a que son llamadas de red.

En este código, los threads tradicionales, los platform threads, quedan bloqueados.

![alt With Virtual Threads](./images/35-WithVirtualThreads.png)

El código usando un virtual thread queda así. Lo que hay dentro del `Runnable` es una tarea (task) que es ejecutada por el virtual thread.

En cada llamada I/O, el virtual thread se desmonta (unmounted) y la siguiente tarea se ejecuta.

![alt Non-Blocking For Free](./images/36-NonBlockingForFree.png)

Para el código usando virtual threads, escribimos el código como normalmente lo haríamos en estilo bloqueante. Por detrás, la JVM lo hará no bloqueante por nosotros.

Por tanto, nosotros seguimos escribiendo código bloqueante, pero obtenemos gratis los beneficios de un código no bloqueante.

### Concurrency - Synchronization Basics

En este punto, conocemos lo básico de los virtual threads, como funcionan por detrás, etc. Hablemos de la sincronización, muy importante en los virtual threads porque tiene varios retos que tenemos que tener en cuenta.

![alt Need For Synchronization](./images/37-NeedForSynchronization.png)

En la parte izquierda de la imagen vemos un proceso. Sabemos que un proceso puede tener uno o más threads. Estos threads pueden hablar entre ellos usando un objeto compartido (el cuadrado amarillo).

Esta comunicación será mucho más eficiente que tener dos procesos (parte derecha de la imagen) que usen protocolos como HTTP, etc. para hablar entre ellos.

Sin embargo, el problema de usar un objeto compartido con varios threads con las condiciones de carrera y la corrupción de la data.

![alt Need For Synchronization - Example](./images/38-NeedForSynchronizationExample.png)

Tomemos este ejemplo de la documentación de Java. Es una clase Counter con una variable miembro `i` inicializada a `0`.

Tenemos dos métodos, uno incrementa la variable en `1` y el otro la decrementa en `1`.

Vamos a crear dos threads. Uno va a invocar el método `increment()` y el otro va a invocar el método `decrement()`.

Podemos pensar que, si invocamos los dos threads, la variable quedará con valor `0`. Sin embargo, el resultado será impredecible, `-1`, `0` o `1`.

¿Por qué? Porque un thread NO SABE lo que está haciendo el otro thread, así que la información puede sobreescribirse.

Para evitar este problema en Java existe un mecanismo llamado `sincronización`.

- Es un mecanismo para proveer acceso controlado a recursos compartidos / secciones críticas de código en un entorno multi-threaded.
- Evita las condiciones de carrera / corrupción de data.
  - Se asegura que solo un thread puede acceder a un bloque de código o a un método a la vez.
- **Nota:**
  - **Todos los retos relacionados con multi-thread como condiciones de carrera (race conditions), puntos muertos (dead-locks), etc. son aplicables a los Virtual Threads.**

![alt Tasks Are Executed By Carrier Threads](./images/39-TasksAreExecutedByCarrierThreads.png)

Aunque hemos dicho que los Virtual Threads son solo tareas, no podemos olvidarnos que al final del día son ejecutadas por Carrier Threads.

Por tanto, tienen los mismos problemas de condiciones de carrera y corrupción de data que cualquier otro thread.

### Synchronization in Compute Tasks

En `src/java/com/jmunoz` creamos los paquetes/clases siguientes:

- `sec05`
  - `Lec01RaceCondition`: Una demo de condiciones de carrera que provocan corrupción de la data.
  - `Lec02Synchronization`: Una demo usando sincronización para evitar la condición de carrera que hemos visto en`Lec01RaceCondition`.

### Thread Pinning: Demo

- Existía en Java 21, 22 y 23 un problema de rendimiento.
- Ha sido parcialmente resuelto en Java 24.
- Muchos desarrolladores no lo notaron, pero afectaba fuertemente a la escalabilidad.

Este problema se llama `Thread Pinning`. En vez de explicar el problema de forma teórica, va a ser más fácil comprenderlo viendo un ejemplo.

![alt Thread Pinning Java 21, 22, 23](./images/40-ThreadPinningJava21-22-23.png)

Imaginemos que tenemos una aplicación que realiza dos tareas I/O independientes.

La **primera tarea** actualiza un documento compartido. Esta tarea implica un estado compartido, así que requiere cuidado en la actualización.

En una aplicación real, esto implica alguna forma de sincronización o bloqueo para asegurar la corrección.

La **segunda tarea** obtiene el perfil del usuario. Es solo una operación de lectura y no necesita sincronización.

Por tanto, tenemos dos tareas, donde una necesita sincronización y la otra no.

![alt Thread Pinning Java 21, 22, 23 Threads](./images/41-ThreadPinningJava21-22-23Threads.png)

Ahora vemos como muchos threads intentan ejecutar estas tareas.

Veamos que ocurre cuando usamos platform threads y cuando usamos virtual threads.

En `src/java/com/jmunoz` creamos los paquetes/clases siguientes:

- `sec05`
  - `Lec03SynchronizationWithIO`: Una demo del problema de escalabilidad que se daba en Java 21, 22 y 23, llamado Thread Pinning. Compilar usando Java 21, 22 o 23.

**Java 21**

Cambiar el POM para que use Java 21.

Para cambiar el SDK del proyecto para que use Java 21:

![alt Change Project Structure](./images/42-ChangeProjectStructure.png)

Para que el bytecode generado sea para Java 21.

![alt Change Target Bytecode](./images/43-ChangeTargetBytecode.png)

El resultado al ejecutar es:

**Platform Threads**

Se lanzan los 50 threads que llaman a `updateSharedDocument()` y también se lanzan los 3 threads que llaman a `fetchUserProfile()`. 

Como las tareas de `fetchUserProfile()` llevan solo un segundo, se completan.

Sin embargo, las tareas que llaman a `updateSharedDocument()`, como tienen la palabra clave `synchronized` se ejecutan una a una. Por eso, cada diez segundos vemos un log con texto `Update ended. Thread ...`.

![alt Thread Pinning Java 21, 22, 23 Platform Threads](./images/44-ThreadPinningJava21-22-23PlatformThreads.png)

Como vimos, cuando usamos un platform thread, el planificador del S.O. puede ver todos los platform threads creados.

Si un thread se bloquea debido a I/O o por la sincronización, el planificador puede ejecutar otro platform thread.

En nuestro código, la tarea 1 usa la palabra clave `synchronized`, así que se ejecutan secuencialmente de una en una.

La tarea 2 no necesita sincronización, así que el S.O. es libre de ejecutar esos threads concurrentemente.

Con platform threads, tanto la tarea 1 como la 2 pueden hacer progresos.

**Virtual Threads**

Se lanzan tantos threads como números de CPU tiene mi ordenador, en mi caso 16 tareas solo para la tarea 1 `updateSharedDocument()`.

No aparece ningún thread para la tarea 2 `fetchUserProfile()`.

Cuando pasan 10 segundos y termina una tarea 2, comienza a ejecutarse otra tarea 2.

Pero, ¿por qué solo se crean 16 threads? ¿Y los otros 34 que mando crear?

![alt Thread Pinning Java 21, 22, 23 Virtual Threads](./images/45-ThreadPinningJava21-22-23VirtualThreads.png)

Sabemos que Java nos permite crear millones de virtual threads (lo vimos en uno de los primeros ejemplos de este curso).

Son planificados por la JVM, no por el sistema operativo.

Por debajo, un pequeño conjunto de carrier threads, cuyo número está basado en el número de procesadores que tenemos, ejecutan esos virtual threads (las tareas).

El virtual thread se monta sobre el carrier thread. Si es una operación I/O bloqueante, una llamada de red o `Thead.sleep()`, se desmonta (park), y el carrier thread coge el siguiente virtual thread. Este es el comportamiento normal esperado.

![alt Thread Pinning Java 21, 22, 23 Virtual Threads Problem](./images/46-ThreadPinningJava21-22-23VirtualThreadsProblem.png)

El problema es el siguiente. Si el virtual thread entra en un método sincronizado, NO PUEDE DESMONTARSE.

La JVM mantiene montado al virtual thread en su carrier thread hasta que termina toda la parte que está sincronizada.

A este comportamiento se le llama `pinning`.

Es por esto que, cuando lanzamos 50 virtual threads para la tarea 1 (en blanco) y 3 virtual threads para la tarea 2 (en azul), si tenemos, digamos 10 carrier threads (10 CPUs), estos carrier threads cogen las 10 primeras tareas correspondientes a la tarea 1. Como son `synchronized`, se bloquean y no pueden desmontarse.

Es decir, tenemos 10 carrier threads, todos bloqueados, y ningún virtual thread de la tarea 2 puede ejecutarse.

**Java 25**

**Virtual Threads**

Si volvemos a dejar el POM para que se compile todo con Java 25 y volvemos a ejecutar, veremos que ya no ocurre el problema de `pinning`.

Se lanzan a la vez 50 virtual threads para la tarea 1 y los 3 virtual threads para la tarea 2.

Es decir, a partir de Java 24, el comportamiento es el mismo que hemos visto en Java 21 para los platform threads.

### Thread Pinning: When & Why it Matters

- `Pinning` es una situación donde un virtual thread debe permanecer en su carrier thread y **NO PUEDE DESMONTARSE** mientras ejecuta código **`synchronized` o `native`**.
  - Es una limitación conocida de Java 21, 22 y 23. 
- Esto evita que la JVM pueda cambiar a otro virtual thread y reduce la escalabilidad.

![alt Thread Pinning Java 21, 22, 23 Synchronized & Native](./images/47-ThreadPinningJava21-22-23SynchronizedNative.png)

Con Java 24 o superior, el problema de `pinning` al usar `synchronized` está solucionado.

Pero el problema de `pinning` continua para llamadas a métodos nativos usando `native`. Por suerte, no es muy común usar JNI, pero si estamos en esta situación, es mejor no usar virtual threads hasta que este problema se solucione. Usar platform threads.

- ¡**synchronized** NO es malo!
  - Collections.synchronizedList(new ArrayList<>());
- **synchronized + Virtual Thread => Pinned**
  - **I/O tasks ->** Los Virtual Thread no pueden desmontarse. Afecta a la escalabilidad.
  - Esto en Java 21-23.
  - Solucionado en Java 24 y posteriores.

### Diagnosing Pinning: How to Trace

Estoy usando Java 21-23 y quiero usar virtual threads. Estoy usando librerías de terceros. ¿Cómo sé que no están usando `synchronized` o `JNI`, etc.?

La JVM nos da una opción para rastrear eventos `pinning`.

Para esta demo, tenemos que cambiar el POM a Java 21. Ver las imágenes de [Thread Pinning Demo](#thread-pinning-demo).

En `src/java/com/jmunoz` modificamos los paquetes/clases siguientes:

- `sec05`
    - `Lec03SynchronizationWithIO`:
      - Se añade una traza para saber si tenemos problemas de `pinning`.

Al ejecutar, si la JVM detecta `pinning` y usamos `System.setProperty("jdk.tracePinnedThreads", "full");`, la traza se verá así:

![alt Thread Pinning Java 21, 22, 23 Trace Full](./images/48-ThreadPinningJava21-22-23TraceFull.png)

Y, si usamos `System.setProperty("jdk.tracePinnedThreads", "short");`, la traza se verá así:

![alt Thread Pinning Java 21, 22, 23 Trace Short](./images/49-ThreadPinningJava21-22-23TraceShort.png)

En ambos casos, si pulsamos sobre el link, accederemos al método donde se produce `pinning`.

### Fixing Pinning: The ReentrantLock Solution

Estoy usando Java 21-23 y no puedo actualizar a Java 24+. Tengo un método con una tarea I/O que requiere sincronización. ¿NO PUEDO usar virtual threads?

Sí, existe una solución alternativa si usamos `ReentrantLock`, y es lo que el equipo de Java nos recomienda usar en vez de `synchronized` para evitar el problema `pinning`.

- `ReentrantLock` se introdujo en Java 1.5
- Funciona como `synchronized`, pero provee más flexibilidad y control.
  - Ver en la imagen como explícitamente bloqueamos con `lock()` y desbloqueamos con `unlock()`.
  - Parece más complicado que la versión con `synchronized`, pero obtenemos los dos puntos siguientes.
- Política de equidad (fairness policy)
  - El primer thread que viene es el primero que obtiene el bloqueo (esto no pasa con `synchronized` donde es aleatorio)
  - El thread que ha estado esperando más tiempo tendrá la oportunidad de adquirir el bloqueo (el lock).
  - En la segunda imagen, vemos que habilitamos esta `fairness policy` indicando `true` en la instrucción `private Lock lock = new ReentrantLock(true);`.
- **tryLock** con timeout
  - Hay un máximo de tiempo de espera para que el thread adquiera el bloqueo.

![alt Reentrant Lock](./images/50-ReentrantLock.png)

![alt Reentrant Lock Fairness](./images/51-ReentrantLockFairness.png)

Compilar esta demo usando Java 21 (cambiar POM, etc.)

En `src/java/com/jmunoz` creamos los paquetes/clases siguientes:

- `sec05`
  - `Lec04ReentrantLock`: Es la demo `Lec02Synchronization` pero hecha usando `ReentrantLock`.
  - `Lec05ReentrantLockWithIO`: Es la demo `Lec03SynchronizationWithIO` pero hecha usando `ReentrantLock`.
    - Vemos en el resultado que no obtenemos la traza de que haya `pinning`.

### Advanced Creation: Virtual Thread Factory

Volvemos a Java 24+.

En esta clase vamos a hablar de `ThreadFactory`.

- `Thread.Builder` - Lo hemos estado usando hasta ahora. No es thread safe.
  - Tampoco pasa nada, no entremos en pánico.
- main - `thread.builder`
  - t1, t2, t3 ...
  - Crear threads de esta forma es correcto, sin problemas.
- main - `thread.builder` 
  - t1
    - t11, t12, t13 ...
  - t2
    - t21, t22, t23 ...
  - t3
    - t31, t32, t33 ...
  - A veces, nuestra aplicación es de esta otra forma. Tenemos una gran tarea y creamos un `thread.builder` y dividimos la tarea grande en tres tareas más pequeñas t1, t2 y t3.
  - Cada hilo podría dividirse a su vez en tareas mucho más pequeñas, creando sus propios threads, para hacer cosas en paralelo. Tendremos t1 y sus hijos t11, t12, t13, etc. Podemos ver una especie de jerarquía.
  - Es en estos casos donde `thread.builder` no es thread safe y la indicación es usar `ThreadFactory`.

Ese caso queda:

- `Thread.Builder` - No es thread safe.   (1)
  - main - `thread.builder`               (2)
      - t1, t2, t3 ...
- main - `thread.builder` -> `factory`    (3)
  - t1
      - t11, t12, t13 ...
  - t2
      - t21, t22, t23 ...
  - t3
      - t31, t32, t33 ...
  - Seguimos creando la configuración en el builder, y a partir de él, creamos un factory.
  - Al usar el factory, podemos crear el número de threads que queramos.
  - No es que solo en este caso (3) se deba usar el factory, también podríamos haberlo usado directamente en (2) 

Por tanto, `ThreadFactory` es thread safe, podemos usarlo donde queramos, pero en (3) no podemos usar `thread.builder`.

¿Cuál es el propósito de crear tantos hilos en un proyecto real? Imaginemos que estamos creando una aplicación web, por ejemplo, la vista de productos de Amazon.

La información es una vista agregada de data de varias fuentes. Hay varios microservicios involucrados de los que tenemos que recoger data y devolverla.

Imaginemos ahora que tenemos 100 vistas de productos. Cada petición tiene que manejarse por varios threads hijos, luego construir la vista de producto y devolver la respuesta.

Es en casos como este donde crear muchos hilos tiene sentido.

Más tarde usaremos algo llamado `high level concurrency`. Por debajo, esto usa `Thread Factory` y por eso lo estamos aprendiendo ahora.

En `src/java/com/jmunoz` creamos los paquetes/clases siguientes:

- `sec06`
  - `Lec01ThreadFactory`: Ejemplo de uso de `ThreadFactory`.

### Key Thread Methods Review

En esta clase veremos algunos métodos útiles cuando trabajamos con virtual threads. Probablemente, no los usemos mucho en nuestro trabajo diario, pero es bueno conocerlos.

Son los mismos métodos que ya usábamos para platform threads.

En `src/java/com/jmunoz` creamos los paquetes/clases siguientes:

- `sec06`
  - `Lec02ThreadMethodsDemo`: Ejemplos mostrando algunos métodos útiles de thread.