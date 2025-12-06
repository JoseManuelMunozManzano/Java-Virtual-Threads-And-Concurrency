package com.jmunoz.sec07.aggregator;

import com.jmunoz.sec07.externalservice.Client;

import java.util.concurrent.ExecutorService;

public class AggregatorService {

    private final ExecutorService executorService;

    public AggregatorService(ExecutorService executorService) {
        this.executorService = executorService;
    }

    // De la clase: Do We Create Extra Thread?
    // Aquí nos llega un thread (creado en Lec04AggregatorDemo) y lo que hacemos es el submit() de dos tareas.
    // Es decir, crea dos threads hijos internamente en este méto-do.
    // El thread llamador sencillamente espera la respuesta de los dos threads hijos.
    // Aquí hay una pregunta. ¿Por qué creamos dos threads hijos innecesariamente?
    // Podríamos crear un solo thread hijo para obtener el producto y que el thread llamador (el padre)
    // obtuviera la respuesta del rating.
    public ProductDto getProductDto(int id) throws Exception {
        var product = executorService.submit(() -> Client.getProduct(id));

        // 3 Opciones.
        // 1. Creamos un thread hijo.
        var rating = executorService.submit(() -> Client.getRating(id));
        return new ProductDto(id, product.get(), rating.get());

        // 2. O el thread padre lo usamos para llamar a getRating.
        // Esto realmente funciona, pero hay un problema.
        // Hay muchos miembros desarrolladores en un equipo, con diferentes niveles de experiencia.
        // Algunos desarrolladores creerán que están haciendo una refactorización si, en vez de
        // usar aquí la variable rating para obtener el resultado de Client.getRating(id) se llevan
        // esto Client.getRating(id) directamente al return (sustituyendo rating por la llamada)
        // Así ahorran una variable.
        // Pero ahora el comportamiento es distinto. Ver comentario abajo.
        // Por eso es mejor no ahorrarse un thread hijo. Es mejor ahorrar confusiones.
        // Además, los virtual threads son objetos pequeños.
//        var rating = Client.getRating(id);
//        return new ProductDto(id, product.get(), rating);

        // 3. Pero esto si que no!!!!
        // Comentar las tres líneas de arriba antes de descomentar esta.
        // Ahora el comportamiento es distinto. En este return, como hicimos el submit de getProduct(),
        // tenemos que esperar a que venga el resultado, dura 1sg. Y luego, se llama a getRating(), que dura 1 sg más.
        // Hemos pasado de tardar 1sg a 2sg.
//        return new ProductDto(id, product.get(), Client.getRating(id));
    }
}
