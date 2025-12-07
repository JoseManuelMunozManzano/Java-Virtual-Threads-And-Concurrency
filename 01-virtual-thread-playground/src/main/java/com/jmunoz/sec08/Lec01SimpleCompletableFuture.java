package com.jmunoz.sec08;

import com.jmunoz.util.CommonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;

// Ejemplo de uso de CompletableFuture.
// El objetivo principal de este ejemplo es ser un escaparate de como un méto-do (slowTask()) puede enviar
// el resultado a otro méto-do (main()) sin hacer que este espere para siempre.
public class Lec01SimpleCompletableFuture {

    private static final Logger log = LoggerFactory.getLogger(Lec01SimpleCompletableFuture.class);

    static void main() {
        log.info("main starts");

        // Para obtener el valor que viene por el túnel creado en un CompletableFuture.
        //   get()  - pero tenemos que gestionar la excepción.
        //   join() - como get() pero si hay un error lanza una RuntimeException (excepción no controlada).
        // Ambos métodos son bloqueantes. No se continúa hasta que se completa CompletableFuture.
        var cf = fastTask();
        log.info("value = {}", cf.join());

        // ¿Cuál es el tema aquí? En vez de bloquear usando los métodos get() o join() podemos usar thenAccept()
        // Este significa: Cuando venga el valor, qué hacemos (se le manda un consumer).
        // Con esto, el méto-do main no queda bloqueado como en el caso anterior (recordar que un virtual thread es un daemon thread)
        //
        // NOTA: Si queremos el valor para devolverlo, vamos a tener que usar get() o join().
        // Recordar que esto es una demo de como funciona CompletableFuture.
        cf = slowTask();
        cf.thenAccept(v -> log.info("value = {}", v));

        log.info("main ends");

        // Bloqueamos el hilo principal para que no termine sin ver el resultado de slowTask().
        // En la vida real, en una aplicación de servidor, no tenemos que añadir esto.
        CommonUtils.sleep(Duration.ofSeconds(2));
    }

    // Este méto-do es llamado y devuelve una respuesta.
    // Es muy rápido.
    private static CompletableFuture<String> fastTask() {
        log.info("method starts");
        // CompletableFuture es el pipe o túnel por el que enviaremos el resultado.
        var cf = new CompletableFuture<String>();
        // Usamos complete() para enviar el resultado al méto-do llamador.
        cf.complete("hi");
        log.info("method ends");
        return cf;
    }

    // Es muy lento.
    private static CompletableFuture<String> slowTask() {
        log.info("method starts");
        // En vez de devolver el resultado directamente, enviamos el CompletableFuture.
        var cf = new CompletableFuture<String>();
        // Se devuelve el resultado vía un Virtual Thread.
        Thread.ofVirtual().start(() -> {
            CommonUtils.sleep(Duration.ofSeconds(1));
            cf.complete("hi");
        });
        log.info("method ends");
        return cf;
    }
}
