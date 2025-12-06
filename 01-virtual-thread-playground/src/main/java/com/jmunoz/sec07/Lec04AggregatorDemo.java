package com.jmunoz.sec07;

import com.jmunoz.sec07.aggregator.AggregatorService;
import com.jmunoz.sec07.aggregator.ProductDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.IntStream;

// No olvidar ejecutar external-services.jar
public class Lec04AggregatorDemo {

    private static final Logger log = LoggerFactory.getLogger(Lec04AggregatorDemo.class);

    static void main() throws Exception {
        // En una app real crearíamos el ExecutorService como un bean manejado por Spring (depende de la app, claro)
        // Y AggregatorService también como un bean / singleton.
        // Podemos usar newVirtualThreadPerTaskExecutor() o newThreadPerTaskExecutor() si queremos configurar un Factory.
//        var executor = Executors.newVirtualThreadPerTaskExecutor();
        var executor = Executors.newThreadPerTaskExecutor(Thread.ofVirtual().name("jm", 1).factory());
        var aggregator = new AggregatorService(executor);

        // Vamos a hacer 50 llamadas y las vamos a hacer en paralelo para no tardar 50sg.
        var futures = IntStream.rangeClosed(1, 50)
                .mapToObj(id -> executor.submit(() -> aggregator.getProductDto(id)))
                .toList();

        // Iteramos sobre la lista de futures.
        var list = futures.stream()
                .map(Lec04AggregatorDemo::toProductDto)
                .toList();

        log.info("list: {}", list);
    }

    private static ProductDto toProductDto(Future<ProductDto> future) {
        try {
            return future.get();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
