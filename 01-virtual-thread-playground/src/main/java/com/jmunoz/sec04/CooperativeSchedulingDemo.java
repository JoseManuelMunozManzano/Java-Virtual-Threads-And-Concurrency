package com.jmunoz.sec04;

import com.jmunoz.util.CommonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

// Una demo para comprender la planificación cooperative.
// NO tendremos que usarlo en aplicaciones reales.
public class CooperativeSchedulingDemo {

    private static final Logger log = LoggerFactory.getLogger(CooperativeSchedulingDemo.class);

    // Para ver la planificación cooperative de la mejor manera, necesitamos recursos limitados.
    // Por eso configuramos el pool size para ejecutar en un solo procesador.
    static {
        System.setProperty("jdk.virtualThreadScheduler.parallelism", "1");
        System.setProperty("jdk.virtualThreadScheduler.maxPoolSize", "1");
    }

    static void main() {
        var builder = Thread.ofVirtual();
        var t1 = builder.unstarted(() -> demo(1));
        var t2 = builder.unstarted(() -> demo(2));

        // Tenemos dos worker threads para ejecutar estos dos virtual threads.
        // Se ejecutan en paralelo SI NO HACEMOS LA PARTE DE CONFIGURAR EL POOL SIZE
        // ni el Thread.yield() en el méto-do demo().
        //
        // ¡¡PARA RECURSOS LIMITADOS!!
        // Si se hace la parte de configuración de pool size, entonces se ejecutan en interactivo,
        //  primero un thread por completo y luego el otro, como si fuera un bucle for.
        //
        // Pero si además incluimos en el méto-do demo() la instrucción Thread.yield() conseguimos
        // que los threads pidan dejar de ejecutarse, volviendo a obtener una especie de paralelismo.
        t1.start();
        t2.start();

        // Como un virtual thread es un daemon thread, y el main thread termina inmediatamente,
        // bloqueamos la ejecución.
        CommonUtils.sleep(Duration.ofSeconds(2));
    }

    private static void demo(int threadNumber) {
        log.info("thread-{} started", threadNumber);
        for (int i = 0; i < 10; i++) {
            log.info("thread-{} is printing {}. Thread: {}", threadNumber, i, Thread.currentThread());
            // Imaginando que el thread tarda mucho tiempo en ejecutarse, podemos indicar que se dé CPU a otro thread.
            if ((threadNumber == 1 && i % 2 == 0) || (threadNumber == 2)) {
                Thread.yield();
            }
        }
        log.info("thread-{} ended", threadNumber);
    }
}
