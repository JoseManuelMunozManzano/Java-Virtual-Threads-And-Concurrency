package com.jmunoz.sec07;

import com.jmunoz.util.CommonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

// Vemos los tipos de ExecutorService.
// SingleThreadExecutor, FixedThreadPool y CachedThreadPool son para platform threads.
// ThreadPerTaskExecutor y VirtualThreadPerTaskExecutor son para virtual threads.
// SingleThreadScheduledExecutor para planificaciones periódicas, también es para platform threads.
public class Lec02ExecutorService {

    private static final Logger log = LoggerFactory.getLogger(Lec02ExecutorService.class);

    static void main() {
        // SingleThreadExecutor
        // Ejecuta los tasks de uno en uno. Cuando una task termina, empieza la siguiente.
        // Es útil cuando queremos ejecutar tareas secuencialmente, por ejemplo, cuando son tareas críticas.
        // No tenemos que tratar con synchronized porque las tareas se ejecutan secuencialmente por un único thread.
//        execute(Executors.newSingleThreadExecutor(), 3);

        // FixedThreadPool
        // Indicamos el número de threads del pool. Si indicamos 1, básicamente sería como trabajar con SingleThreadExecutor,
        // con la única diferencia de que aquí podemos reconfigurar el thread pool.
        // En este ejemplo, podremos ejecutar 5 tareas a la vez. Cuando una tarea termine, el thread cogerá la siguiente.
//        execute(Executors.newFixedThreadPool(5), 20);

        // CachedThreadPool
        // Es un thread pool elástico.
        // Por defecto no tiene ningún thread. Empieza con 0 threads.
        // Basado en el número de tareas que obtiene, creará automáticamente esos threads.
        // En el ejemplo, para 200 tareas crea 200 threads que ejecutan dichas tareas en paralelo.
//        execute(Executors.newCachedThreadPool(), 200);

        // ThreadPerTaskExecutor y VirtualThreadPerTaskExecutor
        // Nuevo tipo de ExecutorService para virtual threads.
        // A ThreadPerTaskExecutor hay que pasarle el ThreadFactory, mientras que a VirtualThreadPerTaskExecutor no
        // es obligatorio pasárselo (se puede) porque ya lo crea.
        // En el ejemplo, se crean 200 virtual threads que se ejecutan en paralelo.
        // La diferencia con CachedThreadPool es que si en CachedThreadPool indico 10000 tareas, como son platform
        // threads, obtendré el error OutOfMemoryError. Sin embargo, con este no, porque son virtual threads.
//        execute(Executors.newVirtualThreadPerTaskExecutor(), 200);

        // Tipo SingleThreadScheduledExecutor para planificaciones periódicas.
        scheduled();
    }

    // Para planificar tareas de forma periódica.
    // Este méto-do es solo para los tipos de ExecutorService Schedule, cuya interface es diferente.
    private static void scheduled() {
        // Interface diferente porque devuelve un ScheduledExecutorService.
        try(var executorService = Executors.newSingleThreadScheduledExecutor()) {
            // Los tipos scheduled tienen algunos métodos adicionales.
            // Por ejemplo, usando scheduleAtFixedRate() podemos planificar una tarea periódicamente, etc.
            // En este ejemplo, se ejecuta, comenzando desde ya, cada segundo, el log.
            executorService.scheduleAtFixedRate(() -> {
                log.info("executing task");
            }, 0, 1, TimeUnit.SECONDS);

            // Bloqueamos la ejecución del hilo principal para que no termine el scheduler directamente.
            // Importante bloquear dentro del bloque try.
            CommonUtils.sleep(Duration.ofSeconds(5));
        }
    }

    // A este méto-do le pasaremos diferentes implementaciones de ExecutorService para ver
    // como funcionan.
    private static void execute(ExecutorService executorService, int taskCount) {
        // Aprovechando que executorService implementa AutoCloseable.
        try (executorService) {
            for (int i = 0; i < taskCount; i++) {
                // Para mantener una variable effectively final.
                int j = i;
                executorService.submit(() -> ioTask(j));
            }
            log.info("submitted");
        }
    }

    // Simula una operación que consume mucho tiempo.
    private static void ioTask(int i) {
        log.info("Task started: {}. Thread Info {}", i, Thread.currentThread());
        CommonUtils.sleep(Duration.ofSeconds(5));
        log.info("Task ended: {}. Thread Info {}", i, Thread.currentThread());
    }
}
