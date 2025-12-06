package com.jmunoz.sec07;

import com.jmunoz.sec07.concurrencylimit.ConcurrencyLimiter;
import com.jmunoz.sec07.externalservice.Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// Para probar este ejemplo, tiene que estar ejecutándose external-services.jar.
// Corrige el problema que teníamos con el límite de concurrencia y los virtual
// threads (ver Lec05ConcurrencyLimit) usando semáforos.
public class Lec06ConcurrencyLimitWithSemaphore {

    private static final Logger log = LoggerFactory.getLogger(Lec06ConcurrencyLimitWithSemaphore.class);

    static void main() throws Exception {
        var factory = Thread.ofVirtual().name("jm", 1).factory();
        var limiter = new ConcurrencyLimiter(Executors.newThreadPerTaskExecutor(factory), 3);
        execute(limiter, 20);
    }

    private static void execute(ConcurrencyLimiter concurrencyLimiter, int taskCount) throws Exception {
        // Aprovechando que executorService implementa AutoCloseable.
        try (concurrencyLimiter) {
            for (int i = 1; i <= taskCount; i++) {
                // Para mantener una variable effectively final.
                int j = i;
                concurrencyLimiter.submit(() -> printProductInfo(j));
            }
            log.info("submitted");
        }
    }

    // Imaginemos que productService es un servicio de terceros.
    // Contrato: Se permiten 3 llamadas concurrentes.
    // Hemos cambiado este méto-do con respecto a Lec05ConcurrentLimit porque Callable debe devolver algo.
    private static String printProductInfo(int id) {
        var product = Client.getProduct(id);
        log.info("{} => {}", id, product);
        return product;
    }
}
