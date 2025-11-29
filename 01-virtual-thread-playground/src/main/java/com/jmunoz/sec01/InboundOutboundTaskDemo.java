package com.jmunoz.sec01;

import java.util.concurrent.CountDownLatch;

// Vamos a ver como de costoso es crear threads originales Java (Platform Threads)
// Vamos a ver que los virtual threads escalan mucho mejor que los threads originales.
//
// Vamos a crear Platform Threads usando el operador new.
// Vamos a crear Platform Threads usando Thread.Builder
// Vamos a crear Daemon Platform Threads usando Thread.Builder
// Vamos a crear Daemon Platform Threads usando Thread.Builder con CountDownLatch
// Vamos a crear Virtual Threads con Thread.Builder (no se puede con new)
public class InboundOutboundTaskDemo {

    // Crear 10 threads no es problema, pero si queremos crear 50_000 threads, veremos
    // que da un error: Unable to create native thread: possibly out of memory or process/resource limits reached
    private static final int MAX_PLATFORM = 10;

    // Podemos crear 50_000 virtual threads sin problema, que se ejecutan en paralelo.
    // También, sin problema, 200_000 threads.
    // 1_000_000 de threads, ¡perfectamente! (cambiar el tiempo de espera en Task a 60)
    private static final int MAX_VIRTUAL = 50;

    // Los threads que creamos en los métodos platformThreadDemo1() y platformThreadDemo2() se llaman user threads
    // o non daemon threads o foreground threads.
    // Creamos los threads y ejecutamos Task, y la aplicación no termina hasta que terminen de ejecutarse los threads.
    static void main() throws InterruptedException {
        // Platform Threads usando operador new.
//        platformThreadDemo1();

        // Platform Threads, usando factory methods (Thread.Builder)
//        platformThreadDemo2();

        // Daemon thread. Se ejecuta en segundo plano.
//        platformThreadDemo3();

        // Daemon thread con CountDownLatch
//        platformThreadDemo4();

        // Virtual thread usando Thread.Builder (no se puede con operador new)
        virtualThreadDemo();
    }

    // Non-Daemon Thread
    // Es un Java Platform Thread sencillo.
    // En aplicaciones de la vida real no creamos threads así.
    // Se suelen usar Thread Pool, Executor Service, etc.
    // Como es un proyecto de pruebas, lo hacemos así para aprender.
    private static void platformThreadDemo1() {
        for (int i = 0; i < MAX_PLATFORM; i++) {
            // Esto hay que hacerlo así porque el Runnable acepta solo variables que son final o effectively final.
            int j = i;
            Thread thread = new Thread(() -> Task.ioIntensive(j));
            thread.start();
        }
    }

    // Non-Daemon Thread
    // Creamos Platform Thread usando Thread.Builder
    private static void platformThreadDemo2() {
        // Ahora el nombre de los threads comienza con jm- y empezamos en el número 1 en vez de en 0.
        var builder = Thread.ofPlatform().name("jm-", 1);

        for (int i = 0; i < MAX_PLATFORM; i++) {
            // Esto hay que hacerlo así porque el Runnable acepta solo variables que son final o effectively final.
            int j = i;
            // Estamos diciendo: Dame un thread con este runnable que no esté arrancado.
            // Yo lo arranco cuando quiera usando start().
            Thread thread = builder.unstarted(() -> Task.ioIntensive(j));
            thread.start();
        }
    }

    // Daemon thread.
    // Creamos Platform Daemon Thread usando Thread.Builder.
    // Este platform thread se ejecuta en segundo plano. El hilo principal termina (la app termina) y el thread
    // sigue ejecutándose.
    private static void platformThreadDemo3() {
        var builder = Thread.ofPlatform().daemon().name("daemon-", 1);

        for (int i = 0; i < MAX_PLATFORM; i++) {
            int j = i;
            Thread thread = builder.unstarted(() -> Task.ioIntensive(j));
            thread.start();
        }
    }

    // Daemon Thread y CountDownLatch
    // Creamos Platform Daemon Thread usando Thread.Builder y CountDownLatch.
    // Este platform thread se ejecuta en segundo plano, pero el hilo principal va a esperar a que las
    // tareas de los threads se completen antes de terminarse.
    private static void platformThreadDemo4() throws InterruptedException {
        var latch = new CountDownLatch(MAX_PLATFORM);
        var builder = Thread.ofPlatform().daemon().name("daemon-", 1);

        for (int i = 0; i < MAX_PLATFORM; i++) {
            int j = i;
            Thread thread = builder.unstarted(() -> {
                Task.ioIntensive(j);
                // Así indicamos que este thread ha acabado.
                // Cuando llegue a 0 la aplicación termina.
                latch.countDown();
            });
            thread.start();
        }
        latch.await();
    }

    // Virtual Thread
    // Creamos Virtual Threads usando Thread.Builder (no se puede con new)
    // Los virtual threads son daemon por defecto.
    // Añadimos CountDownLatch para que la aplicación espere a que se terminen las tareas de los virtual threads.
    // Los virtual threads con tiene un nombre por defecto.
    private static void virtualThreadDemo() throws InterruptedException {
        var latch = new CountDownLatch(MAX_VIRTUAL);
        var builder = Thread.ofVirtual().name("virtual-", 1);

        for (int i = 0; i < MAX_VIRTUAL; i++) {
            int j = i;
            Thread thread = builder.unstarted(() -> {
                Task.ioIntensive(j);
                latch.countDown();
            });
            thread.start();
        }
        latch.await();
    }
}
