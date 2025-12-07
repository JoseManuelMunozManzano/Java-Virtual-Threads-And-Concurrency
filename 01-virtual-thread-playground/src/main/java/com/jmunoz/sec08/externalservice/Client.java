package com.jmunoz.sec08.externalservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;

public class Client {

    private static final Logger log = LoggerFactory.getLogger(Client.class);
    private static final String PRODUCT_REQUEST_FORMAT = "http://localhost:7070/sec01/product/%d";
    private static final String RATING_REQUEST_FORMAT = "http://localhost:7070/sec01/rating/%d";

    public static String getProduct(int id) {
        return callExternalService(PRODUCT_REQUEST_FORMAT.formatted(id));
    }

    public static Integer getRating(int id) {
        return Integer.parseInt(
                callExternalService(RATING_REQUEST_FORMAT.formatted(id))
        );
    }

    private static String callExternalService(String url) {
        log.info("calling {}", url);
        // Esto inicia una conexión TCP con el servicio externo remoto basado en la url dada.
        // Nos devuelve la respuesta en la variable stream (obtendremos los bytes y los convertiremos a String).
        // En la aplicación que haremos más tarde con Spring usaremos HttpClient.
        try(var stream = URI.create(url).toURL().openStream()) {    // stream debe estar cerrado una vez leído.
            return new String(stream.readAllBytes());               // el tamaño de la respuesta es pequeño.
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
