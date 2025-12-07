package com.jmunoz.sec08;

import com.jmunoz.sec08.externalservice.Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

/*
    Para probar este ejemplo, tiene que estar ejecutándose `external-services.jar`.
    Obtener información de varios productos en paralelo.

    Es el mismo ejemplo que hicimos en Lec03AccessResponseUsingFuture, pero ahora usando supplyAsync().
*/
public class Lec04GetProducts {

    private static final Logger log = LoggerFactory.getLogger(Lec04GetProducts.class);

    static void main() throws Exception {
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {

            // Si no añadimos executor (virtual threads) usará fork join pool.
            // Parece más código que la clase con ExecutorService, pero ganamos una mejor gestión de errores.
            var product1 = CompletableFuture.supplyAsync(() -> Client.getProduct(1), executor);
            var product2 = CompletableFuture.supplyAsync(() -> Client.getProduct(2), executor);
            var product3 = CompletableFuture.supplyAsync(() -> Client.getProduct(3), executor);

            log.info("product-1: {}", product1.get());
            log.info("product-2: {}", product2.get());
            log.info("product-3: {}", product3.get());
        }
    }
}
