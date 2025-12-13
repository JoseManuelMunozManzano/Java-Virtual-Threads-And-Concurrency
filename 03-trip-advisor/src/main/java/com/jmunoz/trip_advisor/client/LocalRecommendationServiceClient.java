package com.jmunoz.trip_advisor.client;

import com.jmunoz.trip_advisor.dto.LocalRecommendations;
import org.springframework.web.client.RestClient;

// No se indica la anotación @Service porque en una clase de configuración expondremos este bean.
public class LocalRecommendationServiceClient {

    private final RestClient client;

    // Se construirá SOLO UNA VEZ RestClient vía una clase de configuración, pasando
    // RestClient a esta clase.
    // La baseUrl ya viene configurada. Solo tenemos que añadir la parte del uri que falta.
    public LocalRecommendationServiceClient(RestClient client) {
        this.client = client;
    }

    public LocalRecommendations getRecommendations(String airportCode) {
        return this.client.get()
                .uri("{airportCode}", airportCode)
                .retrieve()
                .body(LocalRecommendations.class);
    }
}
