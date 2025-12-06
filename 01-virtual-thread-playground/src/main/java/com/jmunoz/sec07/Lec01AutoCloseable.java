package com.jmunoz.sec07;

import com.jmunoz.util.CommonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.concurrent.Executors;

// ExecutorService ahora extiende la interface AutoCloseable (desde Java 21, antes no!).
public class Lec01AutoCloseable {

    private static final Logger log = LoggerFactory.getLogger(Lec01AutoCloseable.class);

    static void main() {
        // ANTES DE JAVA 21
        // Así creamos un ExecutorService
        var executorService = Executors.newSingleThreadExecutor();
        executorService.submit(Lec01AutoCloseable::task);
        log.info("submitted");

        // Sin esto, ExecutorService sigue ejecutando los threads y el pool sigue esperando tareas.
        // Lo que hace close() es esperar a que las tareas sometidas se ejecuten, pero no permite
        // someter nuevas tareas.
        executorService.close();
        // Por tanto, esta tarea ya no puede someterse. Da error RejectedExecutionException
//        executorService.submit(Lec01AutoCloseable::task);

        // También existe el méto-do shutdown()
        // Espera a que se ejecute la tarea existente para terminar.
        executorService.shutdown();

        // También existe el méto-do shutdownNow()
        // No espera a que se ejecute la tarea existente. Cancela a la fuerza la tarea y termina.
//        executorService.shutdownNow();

        // DESDE JAVA 21
        // Así es como usamos ExecutorService desde Java 21, ahora que implementa AutoCloseable.
        // Usamos try with resources, que ejecute por debajo el méto-do shutdown()
        try (var executorServiceWithAutocloseable = Executors.newSingleThreadExecutor()) {
            executorServiceWithAutocloseable.submit(Lec01AutoCloseable::task);
            executorServiceWithAutocloseable.submit(Lec01AutoCloseable::task);
            executorServiceWithAutocloseable.submit(Lec01AutoCloseable::task);
            executorServiceWithAutocloseable.submit(Lec01AutoCloseable::task);
            log.info("submitted executorServiceWithAutocloseable");
        }
    }

    private static void task() {
        CommonUtils.sleep(Duration.ofSeconds(1));
        log.info("task executed");
    }
}
