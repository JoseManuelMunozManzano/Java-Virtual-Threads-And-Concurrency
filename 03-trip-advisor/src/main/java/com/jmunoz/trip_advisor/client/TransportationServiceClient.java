package com.jmunoz.trip_advisor.client;

import com.jmunoz.trip_advisor.dto.Transportation;
import org.springframework.web.client.RestClient;

// No se indica la anotación @Service porque en una clase de configuración expondremos este bean.
public class TransportationServiceClient {

    private final RestClient client;

    // Se construirá SOLO UNA VEZ RestClient vía una clase de configuración, pasando
    // RestClient a esta clase.
    // La baseUrl ya viene configurada. Solo tenemos que añadir la parte del uri que falta.
    public TransportationServiceClient(RestClient client) {
        this.client = client;
    }

    public Transportation getTransportation(String airportCode) {
        return this.client.get()
                .uri("{airportCode}", airportCode)
                .retrieve()
                .body(Transportation.class);
    }
}
