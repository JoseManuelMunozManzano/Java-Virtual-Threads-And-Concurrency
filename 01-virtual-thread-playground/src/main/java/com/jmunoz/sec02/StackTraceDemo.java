package com.jmunoz.sec02;

import com.jmunoz.util.CommonUtils;

import java.time.Duration;

public class StackTraceDemo {

    static void main() {
        // Probamos primero con platform threads.
//        demo(Thread.ofPlatform());

        // Probamos después con virtual threads.
        // Le damos un nombre.
        demo(Thread.ofVirtual().name("virtual-", 1));

        // Para los virtual threads, que son daemon threads, tenemos que bloquear la ejecución
        // del hilo principal. Si no, el programa termina y no veremos nada.
        CommonUtils.sleep(Duration.ofSeconds(2));
    }

    private static void demo(Thread.Builder builder) {
        // Crearemos 20 hilos en paralelo.
        for (int i = 1; i < 20; i++) {
            // Esto se hace porque la variable que se espera en el builder tiene que ser final
            // o effectively final.
            int j = i;
            builder.start(() -> Task.execute(j));
        }
    }
}
