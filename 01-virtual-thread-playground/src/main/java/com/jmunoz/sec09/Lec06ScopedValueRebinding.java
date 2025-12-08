package com.jmunoz.sec09;

import com.jmunoz.util.CommonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.UUID;

// Es el mismo ejemplo que hicimos en Lec05ScopedValueAssignment, pero añadimos rebinding.
// El objetivo es proveer un token diferente para productService, pero sin que afecte a inventoryService.
public class Lec06ScopedValueRebinding {

    private static final Logger log = LoggerFactory.getLogger(Lec06ScopedValueRebinding.class);

    private static final ScopedValue<String> SESSION_TOKEN = ScopedValue.newInstance();

    static void main() {
        // Si solo un usuario autenticado puede crear una orden.
        authFilter(Lec06ScopedValueRebinding::orderController);

        // Otra petición, pero sin autenticación.
        // Funciona correctamente, con token a 1 porque el valor por defecto que damos (uso de orElse())
        orderController();

        // Otra petición que necesita autenticación.
        // Usamos otro token, que acaba eliminándose.
        authFilter(Lec06ScopedValueRebinding::orderController);

        // Si nos llegan dos peticiones concurrentes.
        // Asignamos esas peticiones a virtual threads.
        // Vemos que usamos dos token distintos, lo que es correcto y en cada log se indica el token correcto.
        Thread.ofVirtual().name("request-1").start(() -> authFilter(Lec06ScopedValueRebinding::orderController));
        Thread.ofVirtual().name("request-2").start(() -> authFilter(Lec06ScopedValueRebinding::orderController));

        // Para que el main thread no termine inmediatamente (para las peticiones concurrentes)
        CommonUtils.sleep(Duration.ofSeconds(1));
    }

    // Este código es solo para demostrar el flujo de trabajo.

    // WebFilter
    private static void authFilter(Runnable runnable) {
        // Atando (bind) el valor.
        ScopedValue.where(SESSION_TOKEN, authenticate())
                .run(runnable);
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
        log.info("orderController: {}", SESSION_TOKEN.orElse("-1"));
        orderService();
    }

    private static void orderService() {
        log.info("orderService: {}", SESSION_TOKEN.orElse("-1"));
        // rebinding: Cambiamos el valor del token solo para productService.
        ScopedValue.where(SESSION_TOKEN, "new-token")
                .run(() -> callProductService());
        callInventoryService();
    }

    // Este es un cliente que llama a product service (un microservicio)
    private static void callProductService() {
        log.info("calling product-service with header. Authorization: Bearer {}", SESSION_TOKEN.orElse("-1"));
    }

    // Este es un cliente que llama a inventory service (un microservicio)
    private static void callInventoryService() {
        log.info("calling inventory-service with header. Authorization: Bearer {}", SESSION_TOKEN.orElse("-1"));
    }
}
