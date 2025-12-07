package com.jmunoz.sec08;

import com.jmunoz.sec08.aggregator.AggregatorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;

// No olvidar ejecutar external-services.jar
// El objetivo es probar la gestión de errores de CompletableFuture (ver aggregator/AggregatorService)
public class Lec05AggregatorDemo {

    private static final Logger log = LoggerFactory.getLogger(Lec05AggregatorDemo.class);

    static void main() throws Exception {
        // En una app real crearíamos el ExecutorService como un bean manejado por Spring (depende de la app, claro)
        // Y AggregatorService también como un bean / singleton.
        var executor = Executors.newThreadPerTaskExecutor(Thread.ofVirtual().name("jm", 1).factory());
        var aggregator = new AggregatorService(executor);

        // Probar valores 1, 51 (no hay rating), 52 (no hay producto ni rating)
        log.info("product = {}", aggregator.getProductDto(50));
    }
}
