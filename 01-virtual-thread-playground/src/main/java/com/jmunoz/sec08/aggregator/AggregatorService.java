package com.jmunoz.sec08.aggregator;

import com.jmunoz.sec08.externalservice.Client;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/*
    Este aggregator es el mismo que se hizo en sec07, pero usando CompletableFuture con gestión de errores
    y timeout (si queremos errores de timeout indicar 750 ms en vez de los 1250)
*/
public class AggregatorService {

    private final ExecutorService executorService;

    public AggregatorService(ExecutorService executorService) {
        this.executorService = executorService;
    }

    public ProductDto getProductDto(int id) {
        // Si no añadimos executor (virtual threads) usará fork join pool.
        // Parece más código que la clase con ExecutorService, pero ganamos una mejor gestión de errores.

        // En caso de excepción, usando programación funcional, vamos a indicar un valor por defecto.
        // No es necesario el uso de try-catch (mejora de legibilidad).
        var product = CompletableFuture.supplyAsync(() -> Client.getProduct(id), executorService)
                .orTimeout(1250, TimeUnit.MILLISECONDS)
//                .exceptionally(ex -> "product-not-found");
                // También podemos devolver null si queremos
                .exceptionally(ex -> null);
        var rating = CompletableFuture.supplyAsync(() -> Client.getRating(id), executorService)
                .exceptionally(ex -> -1)
                .orTimeout(1250, TimeUnit.MILLISECONDS)
                .exceptionally(ex -> -2);   // Si no indicamos exceptionally obtendremos la excepción del timeout.

        // Como tenemos fallback values en caso de excepción, podemos usar join() en vez de get() (no habrá excepciones)
        // Hemos quitado de la firma del méto-do throws Exception.
        return new ProductDto(id, product.join(), rating.join());
    }
}
