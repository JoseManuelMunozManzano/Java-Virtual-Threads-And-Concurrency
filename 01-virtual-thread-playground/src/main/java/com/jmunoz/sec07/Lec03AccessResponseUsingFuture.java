package com.jmunoz.sec07;

import com.jmunoz.sec07.externalservice.Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.Future;

// No olvidar ejecutar external-services.jar
public class Lec03AccessResponseUsingFuture {

    private static final Logger log = LoggerFactory.getLogger(Lec03AccessResponseUsingFuture.class);

    static void main() throws Exception {
        try(var executor = Executors.newVirtualThreadPerTaskExecutor()){
            // Usando `executor.submit()` obtenemos un Future.
            // Un Future es un placeholder (marcador de posición) a partir del cual podemos acceder a la respuesta.
            // Las tres llamadas se hacen en paralelo (tardan las 3 un segundo en total).
            Future<String> product1 = executor.submit(() -> Client.getProduct(1));
            var product2 = executor.submit(() -> Client.getProduct(2));
            var product3 = executor.submit(() -> Client.getProduct(3));
            // future.get() es bloqueante.
            // Normalmente no querremos escribir código bloqueante, porque los platform threads son un recurso caro.
            // Pero, usando los virtual threads el equipo de Java nos anima a programar así, ya que por debajo la JVM
            // va a hacerlo no bloqueante.
            // Es decir, usando virtual threads, podemos seguir codificando usando código síncrono bloqueante.
            log.info("product-1: {}", product1.get());
            log.info("product-2: {}", product2.get());
            log.info("product-3: {}", product3.get());
        }
    }
}
