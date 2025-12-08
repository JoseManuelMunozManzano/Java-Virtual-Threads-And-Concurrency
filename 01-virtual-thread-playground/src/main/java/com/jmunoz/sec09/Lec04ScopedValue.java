package com.jmunoz.sec09;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Para JDK 25 o superior.
public class Lec04ScopedValue {

    private static final Logger log = LoggerFactory.getLogger(Lec04ScopedValue.class);
    private static final ScopedValue<String> SESSION_TOKEN = ScopedValue.newInstance();

    static void main() {
        // Atando (bind) el valor.
        ScopedValue.where(SESSION_TOKEN, "session-1")
                        .run(() -> checkBinding());

        // Llamada sin atar un valor.
        // El valor anterior, una vez ha terminado el runnable (o callable) se limpia automáticamente.
        checkBinding();
    }

    // Comprueba si hay un valor atado a la key.
    // Si no hay un valor atado a la key, e intentamos ejecutar el méto-do get(), se lanzará una excepción
    // con texto ScopedValue not bound
    // Podemos usar el méto-do orElse() para que, si no hay valor, devuelva uno por defecto.
    private static void checkBinding() {
        log.info("is bound? : {}", SESSION_TOKEN.isBound());
//        log.info("value     : {}", SESSION_TOKEN.get());
        log.info("value     : {}", SESSION_TOKEN.orElse("dummy token"));
    }
}
