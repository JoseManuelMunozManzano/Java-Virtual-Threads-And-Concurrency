package com.jmunoz.sec05;

import com.jmunoz.util.CommonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

// Una demo de condiciones de carrera que provocan corrupción de la data.
/**
 * In a high level, It works like this under the hood which causes this issue.
 * 1. The list keeps a counter of how many items it has.
 * 2. Two threads check the counter at the same time.
 * 3. Both decide to put their item at the same position.
 * 4. One item overwrites the other.
 * Because of this interference, some additions never actually happen, so the total ends up less than 10000.
 */
public class Lec01RaceCondition {

    private static final Logger log = LoggerFactory.getLogger(Lec01RaceCondition.class);
    // Usamos un ArrayList porque no es Thread safe.
    private static final List<Integer> list = new ArrayList<>();

    static void main() {
        // Vemos que tenemos el mismo problema de condiciones de carrera tanto con platform threads como con
        // virtual threads.
//        demo(Thread.ofPlatform());
        demo(Thread.ofVirtual());

        // Indicamos este delay porque si no, llamamos al méto-do demo y casi
        // inmediatamente escribimos en el log el tamaño de la lista.
        CommonUtils.sleep(Duration.ofSeconds(2));

        log.info("list size: {}", list.size());
    }

    // Creo 50 threads y, para cada uno de ellos, invocamos el méto-do inMemoryTask 200 veces.
    // El resultado esperado sería de 10.000 items en la lista, pero vemos que sale un valor menor.
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

    private static void inMemoryTask() {
        list.add(1);
    }
}
