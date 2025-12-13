package com.jmunoz.trip_advisor.client;

import com.jmunoz.trip_advisor.dto.FlightReservationRequest;
import com.jmunoz.trip_advisor.dto.FlightReservationResponse;
import org.springframework.web.client.RestClient;

import java.util.List;

// No se indica la anotación @Service porque en una clase de configuración expondremos este bean.
public class FlightReservationServiceClient {

    private final RestClient client;

    // Se construirá SOLO UNA VEZ RestClient vía una clase de configuración, pasando
    // RestClient a esta clase.
    // La baseUrl ya viene configurada y en este post no hace falta URI.
    public FlightReservationServiceClient(RestClient client) {
        this.client = client;
    }

    public FlightReservationResponse reserve(FlightReservationRequest request) {
        return this.client.post()
                .body(request)
                .retrieve()
                .body(FlightReservationResponse.class);
    }
}
