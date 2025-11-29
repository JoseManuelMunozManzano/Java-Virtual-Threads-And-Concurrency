package com.jmunoz.sec06;

import com.jmunoz.util.CommonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.concurrent.ThreadFactory;

// Demo de ThreadFactory.
// ThreadFactory es thread safe.
// thread.builder no es thread safe.
public class Lec01ThreadFactory {

    private static final Logger log = LoggerFactory.getLogger(Lec01ThreadFactory.class);

    static void main() {
        // Seguimos creando un ThreadFactory usando el builder de Thread.ofVirtual().
        // Indicamos configuración en el builder.
        demo(Thread.ofVirtual().name("jmm", 1).factory());

        CommonUtils.sleep(Duration.ofSeconds(3));
    }

    // Crea algunos threads.
    // Cada thread crea 1 thread hijo.
    // Es un ejemplo. En la vida real usaríamos ExecutorService.
    // Los virtual threads son baratos de crear, así que no hay de que preocuparse si tenemos que crear varios de ellos.
    private static void demo(ThreadFactory factory) {
        for (int i = 0; i < 3; i++) {
            // Al factory se le pasa el Runnable y devuelve un Unstarted Thread, es decir, tenemos que
            // llamar nosotros a start().
            var t = factory.newThread(() -> {
                log.info("Task started. {}", Thread.currentThread());
                // Cada thread va a crear ahora 3 threads.
                var ct = factory.newThread(() -> {
                    log.info("Child task started. {}", Thread.currentThread());
                    CommonUtils.sleep(Duration.ofSeconds(2));
                    log.info("Child task ended. {}", Thread.currentThread());
                });
                ct.start();
                log.info("Task ended. {}", Thread.currentThread());
            });

            t.start();
        }
    }
}
