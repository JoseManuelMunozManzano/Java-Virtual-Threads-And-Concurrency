package com.jmunoz.sec05;

import com.jmunoz.util.CommonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

// Uso de ReentrantLock en tareas I/O.
// Cambiar POM para compilar con JAVA 21.
public class Lec05ReentrantLockWithIO {

    private static final Logger log = LoggerFactory.getLogger(Lec05ReentrantLockWithIO.class);
    private static final Lock lock = new ReentrantLock();

    // Usar esto en tu aplicación para comprobar si los virtual threads tienen el problema de pinning.
    // Hay dos valores que podemos usar, full o short.
    // -Djdk.tracePinnedThreads=full
    // -Djdk.tracePinnedThreads=short
    static {
        System.setProperty("jdk.tracePinnedThreads", "short");
    }

    static void main() {
        demo(Thread.ofVirtual());

        CommonUtils.sleep(Duration.ofSeconds(15));
    }

    private static void demo(Thread.Builder builder) {
        // 50 threads intentando actualizar un documento compartido (synchronized, se ejecuta secuencialmente)
        for (int i = 0; i < 50; i++) {
            builder.start(() -> {
                log.info("Update started. {}", Thread.currentThread());
                updateSharedDocument();
                log.info("Update ended. {}", Thread.currentThread());
            });
        }

        // 3 threads obteniendo perfiles de usuario (se ejecutan concurrentemente)
        for (int i = 0; i < 3; i++) {
            builder.start(() -> {
                log.info("Fetch started. {}", Thread.currentThread());
                fetchUserProfile();
                log.info("Fetch ended. {}", Thread.currentThread());
            });
        }
    }

    // IO Task 1 - usando ReentrantLock para la sincronización.
    private static void updateSharedDocument() {
        try {
            lock.lock();
            CommonUtils.sleep(Duration.ofSeconds(10));
        } finally {
            lock.unlock();
        }
    }

    // IO Task 2
    private static void fetchUserProfile() {
        CommonUtils.sleep(Duration.ofSeconds(1));
    }
}
