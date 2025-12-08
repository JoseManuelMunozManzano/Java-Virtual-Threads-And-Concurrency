package com.jmunoz.sec09;

import com.jmunoz.util.CommonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.UUID;

// Un ejemplo sencillo usando ThreadLocal.
public class Lec01ThreadLocal {

    private static final Logger log = LoggerFactory.getLogger(Lec01ThreadLocal.class);

    // Debemos declarar ThreadLocal como static final.
    private static final ThreadLocal<String> sessionTokenHolder = new ThreadLocal<>();

    static void main() {
        // Si solo un usuario autenticado puede crear una orden.
        authFilter(Lec01ThreadLocal::orderController);

        // Otra petición, pero sin autenticación.
        // Funciona correctamente, con token a null, porque authFilter() eliminó el anterior en la cláusula finally.
        orderController();

        // Otra petición que necesita autenticación.
        // Usamos otro token, que acaba eliminándose.
        authFilter(Lec01ThreadLocal::orderController);

        // Si nos llegan dos peticiones concurrentes.
        // Asignamos esas peticiones a virtual threads.
        // Vemos que usamos dos token distintos, lo que es correcto y en cada log se indica el token correcto.
        Thread.ofVirtual().name("request-1").start(() -> authFilter(Lec01ThreadLocal::orderController));
        Thread.ofVirtual().name("request-2").start(() -> authFilter(Lec01ThreadLocal::orderController));

        // Para que el main thread no termine inmediatamente (para las peticiones concurrentes)
        CommonUtils.sleep(Duration.ofSeconds(1));
    }

    // Este código es solo para demostrar el flujo de trabajo.

    // WebFilter
    // ¿Qué pasa si orderController lanza un NullPointerException o RuntimeException?
    // No llegaremos a llamar al méto-do remove().
    // Para eso hemos creado este WebFilter.
    // El objetivo es ver como funciona ThreadLocal (establecer un valor y en el finally eliminarlo)
    private static void authFilter(Runnable runnable) {
        try {
            // Esto Spring lo hace automáticamente, en segundo plano.
            var token = authenticate();

            // Añadimos al ThreadLocal, para el current thread, su token.
            // NO necesitamos pasar el ThreadLocal como parámetro en ningún méto-do.
            // Spring inyecta automáticamente ThreadLocal por nosotros.
            sessionTokenHolder.set(token);
            // La ejecución de este runnable procesa la petición.
            runnable.run();
        } finally {
            // Ahora, aunque haya excepciones, se ejecuta el méto-do remove()
            // No olvidar ejecutar este méto-do, porque si no, entre distintas peticiones, usaremos el mismo token.
            sessionTokenHolder.remove();
        }
    }

    // Seguridad
    // Esto sería el módulo de Spring Security.
    private static String authenticate() {
        var token = UUID.randomUUID().toString();
        log.info("token = {}", token);
        return token;
    }

    // @Principal
    // Imaginemos que esto es una aplicación de microservicio hecho con Spring Boot.
    // POST /orders
    private static void orderController() {
        log.info("orderController: {}", sessionTokenHolder.get());
        orderService();
    }

    private static void orderService() {
        log.info("orderService: {}", sessionTokenHolder.get());
        // ERROR: No mutar los valores!!!!
        // Compila y funciona, pero rompe toda la lógica detrás del uso del un ThreadLocal.
        // Ahora, en cada méto-do get(), obtendremos el valor test, lo que será incorrecto.
        // Hemos perdido el token!!!
//        sessionTokenHolder.set("test");
        callProductService();
        callInventoryService();
    }

    // Este es un cliente que llama a product service (un microservicio)
    private static void callProductService() {
        log.info("calling product-service with header. Authorization: Bearer {}", sessionTokenHolder.get());
    }

    // Este es un cliente que llama a inventory service (un microservicio)
    private static void callInventoryService() {
        log.info("calling inventory-service with header. Authorization: Bearer {}", sessionTokenHolder.get());
    }
}
