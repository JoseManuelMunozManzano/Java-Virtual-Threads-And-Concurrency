package com.jmunoz.sec06;

import com.jmunoz.util.CommonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

// Ejemplos mostrando algunos métodos útiles de thread.
// Son métodos de bajo nivel que no va a ser normal usar en aplicaciones de la vida real, pero es bueno conocerlos.
public class Lec02ThreadMethodsDemo {

    private static final Logger log = LoggerFactory.getLogger(Lec02ThreadMethodsDemo.class);

    static void main() throws InterruptedException {
//        isVirtual();

//        join();

        // Para interrupt necesitamos el sleep. Es en sleep donde se hace que aparezca la excepción.
        // Es decir, de natural no tiene por qué salir la excepción.
        interrupt();
        CommonUtils.sleep(Duration.ofSeconds(1));
    }

    // Comprobar si un thread es virtual.
    // Usamos el méto-do isVirtual() que devuelve un booleano.
    private static void isVirtual() {
        var t1 = Thread.ofVirtual().start(() -> CommonUtils.sleep(Duration.ofSeconds(2)));
        var t2 = Thread.ofPlatform().start(() -> CommonUtils.sleep(Duration.ofSeconds(2)));
        log.info("Is t1 virtual: {}", t1.isVirtual());
        log.info("Is t2 virtual: {}", t2.isVirtual());
        log.info("Is current thread virtual: {}", Thread.currentThread().isVirtual());
    }

    // Para descargar múltiples llamadas de E/S que consumen mucho tiempo a Virtual Threads y esperar a que se completen.
    // NOTA: Lo vamos a hacer mejor en la aplicación que desarrollaremos más adelante.
    // Es un sencillo ejemplo usando thread.join()
    private static void join() throws InterruptedException {
        var t1 = Thread.ofVirtual().start(() -> {
            CommonUtils.sleep(Duration.ofSeconds(2));
            log.info("called product service");
        });
        var t2 = Thread.ofVirtual().start(() -> {
            CommonUtils.sleep(Duration.ofSeconds(2));
            log.info("called pricing service");
        });

        // Al usar el méto-do join(), esperamos a que el thread complete su tarea.
        // Por debajo, el méto-do join() usa CountdownLatch.
        // Las tareas se hacen en paralelo.
        // Si no tuviéramos este join(), el programa terminaría inmediatamente.
        t1.join();
        t2.join();
    }

    // Interrumpir / Parar la ejecución de un thread.
    // En algunos casos, Java lanzará Interrupted exception, IO exception, socket exception, etc.
    //
    // También podemos comprobar si el tread actual está interrumpido.
    // Thread.currentThread().isInterrupted() - devuelve un booleano.
    //
    // while(!Thread.currentThread().isInterrupted()) {
    //    continuar el trabajo
    //    ...
    //    ...
    // }
    private static void interrupt() {
        var t1 = Thread.ofVirtual().start(() -> {
            CommonUtils.sleep(Duration.ofSeconds(2));
            log.info("called product service");
        });
        log.info("is t1 interrupted: {}", t1.isInterrupted());
        t1.interrupt();
        log.info("is t1 interrupted: {}", t1.isInterrupted());
    }
}
