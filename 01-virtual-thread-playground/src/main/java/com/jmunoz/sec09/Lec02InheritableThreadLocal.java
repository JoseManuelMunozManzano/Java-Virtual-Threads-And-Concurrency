package com.jmunoz.sec09;

import com.jmunoz.util.CommonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.UUID;

// Un ejemplo sencillo de Inheritable Thread Local.
// Para saber qué significa este código, ver Lec01ThreadLocal.
public class Lec02InheritableThreadLocal {

    private static final Logger log = LoggerFactory.getLogger(Lec02InheritableThreadLocal.class);

    // Descomentar para ver el problema (ver méto-do orderService()).
//    private static final ThreadLocal<String> sessionTokenHolder = new ThreadLocal<>();
    // Para solucionar el problema, usamos InheritableThreadLocal.
    // Cuando un thread crea thread hijos, estos hijos heredan automáticamente los valores ThreadLocal de su padre.
    // IMPORTANTE: Lo que sea que almacenemos en ThreadLocal, debemos asegurarnos que es un objeto inmutable.
    //   Si es un objeto mutable, entonces se copia a los threads hijos la REFERENCIA. Si algún hijo muta el valor,
    //   como es una referencia, se muta para los demás hijos y para el padre, porque es el mismo objeto.
    private static final ThreadLocal<String> sessionTokenHolder = new InheritableThreadLocal<>();

    static void main() {
        authFilter(Lec02InheritableThreadLocal::orderController);
        CommonUtils.sleep(Duration.ofSeconds(1));
    }

    // Este código es solo para demostrar el flujo de trabajo.

    // WebFilter
    private static void authFilter(Runnable runnable) {
        try {
            var token = authenticate();
            sessionTokenHolder.set(token);
            runnable.run();
        } finally {
            // Esto elimina los valores del ThreadLocal del padre, pero NO de los hijos.
            // En este caso no es un problema porque los hijos los hemos hecho como virtual threads, que, como ya
            // sabemos, son objetos que tienen una vida corta. No tenemos que preocuparnos de los virtual threads.
            // Si no fueran virtual threads, tendríamos que eliminar su valor de ThreadLocal, en orderService().
            sessionTokenHolder.remove();
        }
    }

    // Seguridad
    private static String authenticate() {
        var token = UUID.randomUUID().toString();
        log.info("token = {}", token);
        return token;
    }

    // @Principal
    // POST /orders
    private static void orderController() {
        log.info("orderController: {}", sessionTokenHolder.get());
        orderService();
    }

    private static void orderService() {
        log.info("orderService: {}", sessionTokenHolder.get());

        // Estas llamadas son secuenciales.
//        callProductService();
//        callInventoryService();
        // ¿Por qué no podemos crear virtual threads para enviar dos peticiones concurrentes?
        // PROBLEMA: Si ejecutamos, vemos que perdemos el valor del token en productService e inventoryService. Vale null.
        // Esto es porque establecimos el valor del token para el main thread, pero no para los threads child-1 y child-2.
        //
        // Es en estos casos donde usamos Inheritable ThreadLocal.
        // Y si ejecutamos, ahora sí que podemos ver el token.
        // Los threads hijos pueden acceder al token generado por el thread padre (en realidad los valores se copian a los hijos).
        Thread.ofVirtual().name("child-1").start(() -> callProductService());
        Thread.ofVirtual().name("child-2").start(() -> callInventoryService());
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
