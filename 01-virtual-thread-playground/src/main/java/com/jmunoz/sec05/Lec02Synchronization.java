package com.jmunoz.sec05;

import com.jmunoz.util.CommonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

// Evitamos condiciones de carrera usando sincronización (synchronized)
// Lo usamos en el méto-do inMemoryTask()
//
// También podría haberse corregido este problema creando una lista
// usando Collections.synchronizedList(new ArrayList<>()) en vez del ArrayList()
public class Lec02Synchronization {

    private static final Logger log = LoggerFactory.getLogger(Lec02Synchronization.class);
    // Usamos un ArrayList porque no es Thread safe.
    private static final List<Integer> list = new ArrayList<>();

    static void main() {
        // Vemos que el problema de condiciones de carrera que ocurría tanto con platform threads como con
        // virtual threads queda solucionado usando synchronized.
//        demo(Thread.ofPlatform());
        demo(Thread.ofVirtual());

        // Indicamos este delay porque si no, llamamos al méto-do demo y casi
        // inmediatamente escribimos en el log el tamaño de la lista.
        CommonUtils.sleep(Duration.ofSeconds(2));

        log.info("list size: {}", list.size());
    }

    // Creo 50 threads y, para cada uno de ellos, invocamos el méto-do inMemoryTask 200 veces.
    // El resultado obtenido es 10.000 items en la lista gracias a la sincronización.
    private static void demo(Thread.Builder builder) {
        for (int i = 0; i < 50; i++) {
            builder.start(() -> {
                log.info("Task started. {}", Thread.currentThread());
                for (int j = 0; j < 200; j++) {
                    inMemoryTask();
                }
                log.info("Task ended. {}", Thread.currentThread());
            });
        }
    }

    // Corregir condiciones de carrera es tan fácil como añadir en la firma del méto-do la palabra clave
    // synchronized.
    private static synchronized void inMemoryTask() {
        list.add(1);
    }
}
