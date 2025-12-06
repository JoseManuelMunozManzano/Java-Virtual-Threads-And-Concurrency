package com.jmunoz.sec07.concurrencylimit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Queue;
import java.util.concurrent.*;

// Es una utility class que limita la concurrencia basado en un valor entero que se le pasa.
//
// Implementamos el AutoCloseable: No es que sea realmente importante, pero es bueno para la demo.
//
// Modificamos para garantizar el orden en la ejecución (ejecución secuencial)
//   Semaphore tiene un parámetro fair, pero no funciona igual que en ReentrantLock.
//     new Semaphore(limit, true)
//   El parámetro fair, en semáforo, sirve cuando hay varios threads que intentan adquirir el permit (acquire()),
//   el thread que llega primero, ese tiene la prioridad.
//   Como creamos 20 threads que se comienzan en paralelo, no sabemos si el orden se va a mantener. El primer
//   thread que adquiera el permit podría ser el tercero. Nuestro problema realmente es la tarea, no el thread.
//   Queremos que se ejecute la tarea de obtener el producto 1, luego el 2, ..., pero no sabemos que thread la coge
//   ni en que orden se van a ejecutar.
//   El parámetro fair (fairness) está relacionado con el thread, no con la tarea.
//
//   Para solucionar el problema del orden, tenemos que gestionar nosotros una cola.
public class ConcurrencyLimiter implements AutoCloseable {

    private static final Logger log = LoggerFactory.getLogger(ConcurrencyLimiter.class);

    private final ExecutorService executor;
    private final Semaphore semaphore;
    // Cola para gestionar el orden de ejecución correcto.
    private final Queue<Callable<?>> queue;

    public ConcurrencyLimiter(ExecutorService executor, int limit) {
        this.executor = executor;
        this.semaphore = new Semaphore(limit);
        // Nuestra implementación de la cola TIENE QUE SER THREAD SAFE
        this.queue = new ConcurrentLinkedDeque<>();
    }

    // NO ORDENADO
    //
    // No usamos executor service directamente.
    // Hacemos un objeto wrapper para nuestro executor service, que limitará la concurrencia para los
    // virtual threads.
//    public <T> Future<T> submit(Callable<T> callable) {
//        // No pasamos el callable a submit directamente.
//        // Añadimos la gestión de los permits (wrapper al callable).
//        return executor.submit(() -> wrapCallable(callable));
//    }

    // Gestión de permits.
//    private <T> T wrapCallable(Callable<T> callable) {
//        try{
//            semaphore.acquire();
//            return callable.call();
//        } catch (Exception e) {
//            log.error("error", e);
//        } finally {
//            semaphore.release();
//        }
//
//        return null;        // En caso de excepción
//    }

    // ORDENADO
    //
    // Para garantizar el orden de ejecución, en vez de dar la tarea al executor service directamente,
    // la almacenamos en la cola (comparar con el méto-do submit() comentado de arriba)
    public <T> Future<T> submit(Callable<T> callable) {
        this.queue.add(callable);
        // No decimos que tarea ejecutar. No llamamos ahora al callable.
        return executor.submit(() -> executeTask());
    }

    // Gestión de permits usando la cola.
    // No hay callable porque no llamamos a una tarea en concreto.
    // Son todas las tareas que tenemos en la cola.
    // Comparar con el méto-do wrapCallable() comentado de arriba.
    private <T> T executeTask() {
        try{
            semaphore.acquire();
            // Extraemos datos de la cola.
            // poll() devuelve un callable, que invocamos con call().
            // Así se ejecuta la tarea desde una cola.
            return (T) this.queue.poll().call();
        } catch (Exception e) {
            log.error("error", e);
        } finally {
            semaphore.release();
        }

        return null;        // En caso de excepción
    }

    @Override
    public void close() throws Exception {
        this.executor.close();
    }
}
