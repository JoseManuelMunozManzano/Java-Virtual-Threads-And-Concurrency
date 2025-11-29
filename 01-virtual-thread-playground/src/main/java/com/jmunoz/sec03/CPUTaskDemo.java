package com.jmunoz.sec03;

import com.jmunoz.util.CommonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;

// Vamos a ver cómo se comportan los Platform Threads vs Virtual Threads cuando hay tareas de CPU intensivas.
public class CPUTaskDemo {

    private static final Logger log = LoggerFactory.getLogger(CPUTaskDemo.class);

    // Probar primero con el valor 1, luego con el valor 5, luego con el número de procesadores disponibles,
    // luego multiplicar por 2 el número de procesadores disponibles y por último multiplicar por 3.
    // Comprobar los tiempos que tarda en ejecutar, tanto para Platform Thread como para Virtual Thread.
    private static final int TASKS_COUNT = 3 * Runtime.getRuntime().availableProcessors();

    static void main() {
        log.info("Tasks Count: {}", TASKS_COUNT);

        // Lo hacemos 3 veces porque Java necesita un calentamiento.
        // Es decir, la primera vez que se ejecuta algo en Java, suele ser más lento que la segunda, donde hace optimizaciones.
        // Por eso indicamos 3, para quedarnos con el tiempo que tarda la segunda y tercera ejecución.
        for (int i = 0; i < 3; i++) {
            // Como funciona un Virtual Thread.
            var totalTimeTaken = CommonUtils.timer(() -> demo(Thread.ofVirtual()));
            log.info("Total time taken with virtual {} ms", totalTimeTaken);

            // Como funciona un Platform Thread.
            totalTimeTaken = CommonUtils.timer(() -> demo(Thread.ofPlatform()));
            log.info("Total time taken with platform {} ms", totalTimeTaken);
        }
    }

    private static void demo(Thread.Builder builder) {
        // Para el virtual thread, que es daemon, necesitamos un CountDownLatch,
        // si no el programa terminará sin que veamos nada.
        var latch = new CountDownLatch(TASKS_COUNT);
        for (int i = 1; i <= TASKS_COUNT ; i++) {
            builder.start(() -> {
                // Si indicamos el valor 45, vemos que tarda mucho en devolver el resultado (es intencionado)
                // No estamos interesados en el valor, sino en el tiempo que tarda en ejecutarse.
                Task.cpuIntensive(45);
                latch.countDown();
            });
        }
        try {
            latch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
