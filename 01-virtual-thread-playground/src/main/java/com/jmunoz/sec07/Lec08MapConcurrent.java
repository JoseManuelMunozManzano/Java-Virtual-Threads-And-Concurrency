package com.jmunoz.sec07;

import com.jmunoz.sec07.externalservice.Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.stream.Gatherers;
import java.util.stream.IntStream;

// Para probar este ejemplo, tiene que estar ejecutándose external-services.jar.
// Ejemplo usando Java Stream Gatherers, gather y Map Concurrent.
// Solo funciona para Java 24 o superior.
//
// En Lec06ConcurrencyLimitWithSemaphore, intentábamos encontrar la solución para limitar la concurrencia.
// Usando Map Concurrent, podemos implementar lo mismo, pero de manera mucho más sencilla.
public class Lec08MapConcurrent {

    private static final Logger log = LoggerFactory.getLogger(Lec08MapConcurrent.class);

    static void main() {
        // gather() no está disponible para stream de primitivos.
        // Pero usando boxed() obtenemos un Stream<Integer> y ya podemos usar gather().
        // mapConcurrent() acepta dos parámetros
        //    maxconcurrency
        //    mapper, que es una función
        // Para un rango de ids del 1 al 50, obtenemos de 3 en 3 el nombre del producto para ese id
        // y lo guardamos en una lista.
        // Cambiar luego maxConcurrency a 50 (en vez de 3). Lo hace to-do en 1 sg.
        // Usando virtual threads!!!
        var list = IntStream.rangeClosed(1, 50)
                .boxed()
                .gather(Gatherers.mapConcurrent(3, Lec08MapConcurrent::getProductName))
                .toList();

        log.info("size: {}", list.size());
    }

    // Imaginemos que productService es un servicio de terceros.
    // Contrato: Se permiten 3 llamadas concurrentes.
    private static String getProductName(int id) {
        var product = Client.getProduct(id);
        log.info("{} => {}", id, product);
        return product;
    }
}
