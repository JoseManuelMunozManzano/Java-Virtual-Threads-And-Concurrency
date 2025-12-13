package com.jmunoz.trip_advisor.client;

import com.jmunoz.trip_advisor.dto.Accommodation;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestClient;

import java.util.List;

// No se indica la anotación @Service porque en una clase de configuración expondremos este bean.
public class AccommodationServiceClient {

    private final RestClient client;

    // Se construirá SOLO UNA VEZ RestClient vía una clase de configuración, pasando
    // RestClient a esta clase.
    // La baseUrl ya viene configurada. Solo tenemos que añadir la parte del uri que falta.
    public AccommodationServiceClient(RestClient client) {
        this.client = client;
    }

    public List<Accommodation> getAccommodations(String airportCode) {
        return this.client.get()
                .uri("{airportCode}", airportCode)
                .retrieve()
                .body(new ParameterizedTypeReference<List<Accommodation>>() {});
    }
}
