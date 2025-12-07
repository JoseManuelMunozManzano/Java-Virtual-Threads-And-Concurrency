package com.jmunoz.sec08;

import com.jmunoz.util.CommonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;

/*
    Ejemplo de uso del méto-do `anyOf()` de `CompletableFuture`.

    Imaginemos que tenemos 2 aerolíneas, Delta Airlines y Frontier Airlines y queremos la información
    de la tarifa aérea.
    Vamos a llamar a los dos servicios en paralelo y estamos interesados en la primera respuesta.
    Es decir, vamos a coger quien primero nos responda.
    Aquí es donde es útil usar anyOf()
*/
public class Lec07AnyOf {

    private static final Logger log = LoggerFactory.getLogger(Lec07AnyOf.class);

    static void main() {
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            var cf1 = getDeltaAirfare(executor);
            var cf2 = getFrontierAirfare(executor);
            // No olvidar join() para obtener el resultado.
            log.info("airfare = {}", CompletableFuture.anyOf(cf1, cf2).join());
        }
    }

    // Pasamos un executor para no crear uno cada vez que llamemos a este méto-do.
    private static CompletableFuture<String> getDeltaAirfare(ExecutorService executor) {
        // No olvidar pasar executor para usar virtual threads.
        return CompletableFuture.supplyAsync(() -> {
            var random = ThreadLocalRandom.current().nextInt(100, 1000);
            CommonUtils.sleep(Duration.ofMillis(random));
            return "Delta-$" + random;
        }, executor);
    }

    // Pasamos un executor para no crear uno cada vez que llamemos a este méto-do.
    private static CompletableFuture<String> getFrontierAirfare(ExecutorService executor) {
        // No olvidar pasar executor para usar virtual threads.
        return CompletableFuture.supplyAsync(() -> {
            var random = ThreadLocalRandom.current().nextInt(100, 1000);
            CommonUtils.sleep(Duration.ofMillis(random));
            return "Frontier-$" + random;
        }, executor);
    }
}
