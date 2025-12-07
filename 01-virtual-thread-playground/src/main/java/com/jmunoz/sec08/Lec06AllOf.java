package com.jmunoz.sec08;

import com.jmunoz.sec08.aggregator.AggregatorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

/*
    Para probar este ejemplo, tiene que estar ejecutándose `external-services.jar`.
    Ejemplo para probar el méto-do CompletableFuture.allOf()
*/
public class Lec06AllOf {

    private static final Logger log = LoggerFactory.getLogger(Lec06AllOf.class);

    static void main() {
        // En una app real crearíamos el ExecutorService como un bean manejado por Spring (depende de la app, claro)
        // Y AggregatorService también como un bean / singleton.
        var executor = Executors.newThreadPerTaskExecutor(Thread.ofVirtual().name("jm", 1).factory());
        var aggregator = new AggregatorService(executor);

        // Vamos a hacer 100 llamadas y las vamos a hacer en paralelo para no tardar 50sg.
        // Tenemos productos hasta el 50, pero no tendremos excepciones porque están manejadas en AggregatorService.
        var futures = IntStream.rangeClosed(1, 100)
                .mapToObj(id -> CompletableFuture.supplyAsync(() -> aggregator.getProductDto(id), executor))
                .toList();

        // Para usar allOf(), indicamos un array de futures y, con join() esperamos que se completen.
        CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).join();

        // Iteramos sobre la lista de futures.
        var list = futures.stream()
                .map(CompletableFuture::join)
                .toList();

        log.info("list: {}", list);
    }
}
