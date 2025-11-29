package com.jmunoz.sec01;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

// Creamos un méto-do que simula una llamada de red lenta.
public class Task {

    private static final Logger log = LoggerFactory.getLogger(Task.class);

    // Vamos a imprimir el valor de i para saber qué thread lo está haciendo.
    public static void ioIntensive(int i) {
        try {
            log.info("starting I/O task {}. Thread Info: {}", i, Thread.currentThread());
            // Dejar a 10 para cualquier prueba, menos la del millón de virtual threads, donde hay que poner 60
            // Esto es para que de tiempo a crear los virtual threads.
            Thread.sleep(Duration.ofSeconds(10));
            log.info("ending I/O task {}. Thread Info: {}", i, Thread.currentThread());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
