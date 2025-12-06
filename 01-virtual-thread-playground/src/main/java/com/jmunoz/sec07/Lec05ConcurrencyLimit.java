package com.jmunoz.sec07;

import com.jmunoz.sec07.externalservice.Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// Para probar este ejemplo, tiene que estar ejecutándose external-services.jar.
public class Lec05ConcurrencyLimit {

    private static final Logger log = LoggerFactory.getLogger(Lec05ConcurrencyLimit.class);

    static void main() {
        // Esto ya lo hemos visto. Sometemos las tareas en paralelo.
//        execute(Executors.newCachedThreadPool(), 20);
        // Pero como tenemos un contrato para usar nuestro servicio de terceros (max. 3 llamadas concurrentes),
        // no podemos usarlo, porque newCachedThreadPool intentará crear tantos threads como necesite para
        // completar todas las tareas en paralelo.
        // Por eso utilizamos newFixedThreadPool e indicamos el máximo de threads.
        // Por tanto, se harán concurrentemente todas las tareas, pero de 3 en 3.
//        execute(Executors.newFixedThreadPool(3), 20);
        // Pero esto son platform threads. ¿Cómo lo hacemos con Virtual Threads? Usando un thread factory que acepta
        // como parámetro.
        var factory = Thread.ofVirtual().name("jm", 1).factory();
        execute(Executors.newFixedThreadPool(3, factory), 20);
        // Parece funcionar bien, pero el problema encontrado al ejecutar esto es:
        // Vemos que se usan los mismos threads para ejecutar distintas tareas (usa un pool), pero los virtual
        // threads se supone que no usan pools.
        // Es decir, independientemente del factory que indiquemos, y basado en el número, fixed crea esos threads y
        // los intenta reutilizar hasta completar las tareas.
    }

    private static void execute(ExecutorService executorService, int taskCount) {
        // Aprovechando que executorService implementa AutoCloseable.
        try (executorService) {
            for (int i = 1; i <= taskCount; i++) {
                // Para mantener una variable effectively final.
                int j = i;
                executorService.submit(() -> printProductInfo(j));
            }
            log.info("submitted");
        }
    }

    // Imaginemos que productService es un servicio de terceros.
    // Contrato: Se permiten 3 llamadas concurrentes.
    private static void printProductInfo(int id) {
        log.info("{} => {}", id, Client.getProduct(id));
    }
}
