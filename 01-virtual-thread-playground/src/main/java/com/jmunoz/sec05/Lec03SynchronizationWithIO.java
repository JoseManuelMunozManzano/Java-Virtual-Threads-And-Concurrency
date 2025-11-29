package com.jmunoz.sec05;

import com.jmunoz.util.CommonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

// Una demo del problema de escalabilidad que se daba en Java 21, 22 y 23, llamado Thread Pinning.
// Para ver la explicación de qué se está haciendo y su resultado, ver Thread Pinning: Demo en README.md
//
// Cambiar POM para compilar con JAVA 21 y luego con JAVA 25.
public class Lec03SynchronizationWithIO {

    private static final Logger log = LoggerFactory.getLogger(Lec03SynchronizationWithIO.class);

    // Usar esto en tu aplicación para comprobar si los virtual threads tienen el problema de pinning.
    // Hay dos valores que podemos usar, full o short.
    // -Djdk.tracePinnedThreads=full
    // -Djdk.tracePinnedThreads=short
    static {
        System.setProperty("jdk.tracePinnedThreads", "short");
    }

    static void main() {
        // Con platform threads, tanto task 1 como task 2 hacen progresos.
//        demo(Thread.ofPlatform());

        // Con virtual threads, solo task 1 hace progresos.
        // Cuando termina task 1 entonces se comienza a ejecutar task 2.
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

    // IO Task 1 - requiere sincronización.
    private static synchronized void updateSharedDocument() {
        CommonUtils.sleep(Duration.ofSeconds(10));
    }

    // IO Task 2
    private static void fetchUserProfile() {
        CommonUtils.sleep(Duration.ofSeconds(1));
    }
}
