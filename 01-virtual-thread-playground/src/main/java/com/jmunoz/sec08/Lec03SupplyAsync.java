package com.jmunoz.sec08;

import com.jmunoz.util.CommonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

/*
    Podemos suministrar valores asíncronamente.
    Factory Method
    Executor
*/
public class Lec03SupplyAsync {

    private static final Logger log = LoggerFactory.getLogger(Lec03SupplyAsync.class);

    static void main() {
        log.info("main starts");
        var cf = slowTask();
        cf.thenAccept(v -> log.info("value = {}", v));

        log.info("main ends");

        // Bloqueamos el hilo principal para que no termine sin ver el resultado de slowTask().
        // En la vida real, en una aplicación de servidor, no tenemos que añadir esto.
        CommonUtils.sleep(Duration.ofSeconds(2));
    }

    // En vez de escribir to-do nosotros como hicimos en Lec01SimpleCompletableFuture,
    // usamos el factory supplyAsync() al que se le pasa un Supplier.
    private static CompletableFuture<String> slowTask() {
        log.info("method starts");
        // Si ejecutamos, vemos que se usa un ForkJoinPool, es decir IO bloqueante.
//        var cf = CompletableFuture.supplyAsync(() -> {
//            CommonUtils.sleep(Duration.ofSeconds(1));
//            return "hi";
//        });

        // Usando Virtual Threads gracias al Executors, cuando ejecutamos ahora vemos virtual threads, IO no bloqueante.
        var cf = CompletableFuture.supplyAsync(() -> {
            CommonUtils.sleep(Duration.ofSeconds(1));
            return "hi";
        }, Executors.newVirtualThreadPerTaskExecutor());

        log.info("method ends");
        return cf;
    }
}
