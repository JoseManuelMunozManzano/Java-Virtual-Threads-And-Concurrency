package com.jmunoz.sec07;

import com.jmunoz.sec07.externalservice.Client;
import com.jmunoz.util.CommonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

// Para probar este ejemplo, tiene que estar ejecutándose external-services.jar.
// Como no se puede usar directamente un schedulecExecutor con virtual threads,
// hacemos que un platform thread delegue la tarea a un virtual thread.
public class Lec07ScheduledExecutorWithVirtualThreads {

    private static final Logger log = LoggerFactory.getLogger(Lec07ScheduledExecutorWithVirtualThreads.class);

    static void main() {
        scheduled();
    }

    // Para planificar tareas de forma periódica.
    // Este es un platform thread que periódicamente llama al servicio de terceros.
    // Vamos a delegar la tarea a un virtual thread.
    private static void scheduled() {
        // Tenemos dos executors.
        //
        // Este executor es para planificar la tarea. Solo hace eso.
        var scheduler = Executors.newSingleThreadScheduledExecutor();
        // Este es el executor del virtual thread al que delegamos la tarea y que ejecuta.
        var executor = Executors.newVirtualThreadPerTaskExecutor();

        try(scheduler; executor) {
            // planificación periódica (platform thread)
            scheduler.scheduleAtFixedRate(() -> {
                // ejecución (virtual thread)
                executor.submit(() -> printProductInfo(1));
            }, 0, 3, TimeUnit.SECONDS);

            // Bloqueamos la ejecución del hilo principal para que no termine el scheduler directamente.
            // Importante bloquear dentro del bloque try.
            // En una aplicación real, ejecutándose en un servidor, no hace falta porque no termina.
            CommonUtils.sleep(Duration.ofSeconds(15));
        }
    }

    // Imaginemos que productService es un servicio de terceros.
    // Es una llamada IO que consume bastante tiempo.
    // No queremos usar platform threads porque son bloqueantes.
    // Queremos usar virtual threads para obtener beneficios no bloqueantes.
    private static void printProductInfo(int id) {
        log.info("{} => {}", id, Client.getProduct(id));
    }
}
