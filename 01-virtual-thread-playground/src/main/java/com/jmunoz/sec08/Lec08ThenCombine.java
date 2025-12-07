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
    Vamos a llamar a los dos servicios en paralelo y estamos interesados en obtener los dos valores y devolver
    el valor más bajo que además tiene un 10% de descuento (usando thenApply()).

    Aquí es donde es útil usar thenCombine()
*/
public class Lec08ThenCombine {

    private static final Logger log = LoggerFactory.getLogger(Lec08ThenCombine.class);

    record Airfare(String airline, int amount) {
    }

    static void main() {
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            var cf1 = getDeltaAirfare(executor);
            var cf2 = getFrontierAirfare(executor);
            // Combinamos las dos respuestas y usando una BiFunction devolvemos el valor más bajo.
            // Luego usamos thenApply al que se le pasa una Function para aplicar un 10% de descuento.
            // Como los records son inmutables, creamos otro record Airfare.
            var bestDeal = cf1.thenCombine(cf2, (a, b) -> a.amount() <= b.amount() ? a : b)
                    .thenApply(af -> new Airfare(af.airline(), (int) (af.amount() * 0.9)))
                    .join();

            // No olvidar join() para obtener el resultado.
            log.info("best deal = {}", bestDeal);
        }
    }

    // Pasamos un executor para no crear uno cada vez que llamemos a este méto-do.
    private static CompletableFuture<Airfare> getDeltaAirfare(ExecutorService executor) {
        // No olvidar pasar executor para usar virtual threads.
        return CompletableFuture.supplyAsync(() -> {
            var random = ThreadLocalRandom.current().nextInt(100, 1000);
            CommonUtils.sleep(Duration.ofMillis(random));
            log.info("Delta-{}", random);
            return new Airfare("Delta", random);
        }, executor);
    }

    // Pasamos un executor para no crear uno cada vez que llamemos a este méto-do.
    private static CompletableFuture<Airfare> getFrontierAirfare(ExecutorService executor) {
        // No olvidar pasar executor para usar virtual threads.
        return CompletableFuture.supplyAsync(() -> {
            var random = ThreadLocalRandom.current().nextInt(100, 1000);
            CommonUtils.sleep(Duration.ofMillis(random));
            log.info("Frontier-{}", random);
            return new Airfare("Frontier", random);
        }, executor);
    }
}
