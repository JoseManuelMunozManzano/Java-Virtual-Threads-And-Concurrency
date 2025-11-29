package com.jmunoz.sec05;

import com.jmunoz.util.CommonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

// Uso de reentrantLock en vez de synchronized.
// Nos da más control y evitamos el problema pinned.
public class Lec04ReentrantLock {

    private static final Logger log = LoggerFactory.getLogger(Lec04ReentrantLock.class);
    // Podemos indicar new ReentrantLock(true) para habilitar Fairness Policy,
    // es decir, el primer thread que llega es el que adquiere el bloqueo.
    private static final Lock lock = new ReentrantLock();
    // Usamos un ArrayList porque no es Thread safe.
    private static final List<Integer> list = new ArrayList<>();

    static void main() {
        demo(Thread.ofVirtual());

        // Indicamos este delay porque si no, llamamos al méto-do demo y casi
        // inmediatamente escribimos en el log el tamaño de la lista.
        CommonUtils.sleep(Duration.ofSeconds(2));

        log.info("list size: {}", list.size());
    }

    // Creo 50 threads y, para cada uno de ellos, invocamos el méto-do inMemoryTask 200 veces.
    // El resultado obtenido es 10.000 items en la lista gracias a reentrantLock
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

    // Aquí codificamos reentrantLock.
    private static void inMemoryTask() {
        try {
            lock.lock();
            list.add(1);
        } finally {
            lock.unlock();
        }
    }
}
