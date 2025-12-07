package com.jmunoz.sec08;

import com.jmunoz.util.CommonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

/*
    Factory method
    Run async
    Executor
*/
public class Lec02RunAsync {

    private static final Logger log = LoggerFactory.getLogger(Lec02RunAsync.class);

    static void main() {
        log.info("main starts");

        // Este méto-do 1 llama al méto-do 2, pero el méto-do 2 es una tarea asíncrona.
        // Es decir, este méto-do 1 da al méto-do 2 tareas, y el méto-do 2 podría no ser
        // capaz de (o no ser necesario) devolver un resultado.
        // Puede ejecutar las tareas de manera asíncrona y devolver void, como en este caso.
        runAsync();

        // Imaginemos ahora que el méto-do 1 realmente no quiere el resultado que le devuelve el méto-do 2.
        // En el ejemplo el méto-do 2 devuelve un CompletableFuture<Void>, luego no hay resultado siquiera.
        // Lo que quiere es saber si el méto-do 2 se ha completado o no exitosamente.
        // Usamos el méto-do thenRun() al que se le pasa un Runnable que se ejecuta (asíncronamente) cuando la tarea se completa.
        //
        // ¿Qué pasa si la tarea no puede completarse? ¿Cómo lo sabe el méto-do 1?
        // Usamos el méto-do exceptionally() al que se le pasa una función.
        runAsync2()
                .thenRun(() -> log.info("it is done"))
                .exceptionally(ex -> {
                    log.info("error - {}", ex.getMessage());
                    return null;
                });

        log.info("main ends");

        // Bloqueamos el hilo principal para que no termine sin ver el resultado de slowTask().
        // En la vida real, en una aplicación de servidor, no tenemos que añadir esto.
        CommonUtils.sleep(Duration.ofSeconds(2));
    }

    // Este es el méto-do 2.
    private static void runAsync() {
        log.info("method starts");

        // No siempre es necesario crear un CompletableFuture usando el operador new.
//        var cf = new CompletableFuture<String>();
        // Podemos usar un factory method de los que tiene CompletableFuture para crear una instancia de CompletableFuture.
        // En el ejemplo usamos runAsync() porque queremos ejecutar tareas de manera asíncrona sin devolver un valor.

        // Si ejecutamos, vemos que usa un ForkJoinPool. Este es el comportamiento por defecto.
        // Sabemos que esto no es bueno porque es una operación bloqueante.
//        CompletableFuture.runAsync(() -> {
//            CommonUtils.sleep(Duration.ofSeconds(1));
//            log.info("task completed");
//        });

        // Executor para usar virtual threads.
        // Si ejecutamos, vemos que ahora la tarea se ha hecho usando un virtual thread.
        CompletableFuture.runAsync(() -> {
            CommonUtils.sleep(Duration.ofSeconds(1));
            log.info("task 1 completed");
        }, Executors.newVirtualThreadPerTaskExecutor());

        log.info("method ends");
    }

    // Este es el méto-do 2 de nuevo, pero ahora devuelve si se ha completado exitosamente o no.
    private static CompletableFuture<Void> runAsync2() {
        log.info("method starts");

        // Executor para usar virtual threads.
        // Si ejecutamos, vemos que ahora la tarea se ha hecho usando un virtual thread.
        var cf = CompletableFuture.runAsync(() -> {
            CommonUtils.sleep(Duration.ofSeconds(1));
            // Si la tarea no puede completarse (comentar si queremos que se complete y descomentar el log)
            throw new RuntimeException("oops!");
//            log.info("task 2 completed");
        }, Executors.newVirtualThreadPerTaskExecutor());

        log.info("method ends");
        return cf;
    }
}
