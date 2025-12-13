package com.jmunoz.trip_advisor.client;

import com.jmunoz.trip_advisor.dto.Flight;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestClient;

import java.util.List;

// No se indica la anotación @Service porque en una clase de configuración expondremos este bean.
public class FlightSearchServiceClient {

    private final RestClient client;

    // Se construirá SOLO UNA VEZ RestClient vía una clase de configuración, pasando
    // RestClient a esta clase.
    // La baseUrl ya viene configurada. Solo tenemos que añadir la parte del uri que falta.
    public FlightSearchServiceClient(RestClient client) {
        this.client = client;
    }

    public List<Flight> getFlights(String departure, String arrival) {
        return this.client.get()
                .uri("/{departure}/{arrival}", departure, arrival)
                .retrieve()
                .body(new ParameterizedTypeReference<List<Flight>>() {});
    }
}
