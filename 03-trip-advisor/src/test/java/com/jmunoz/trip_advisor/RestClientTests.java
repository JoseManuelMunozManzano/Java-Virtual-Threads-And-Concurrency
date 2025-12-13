package com.jmunoz.trip_advisor;

import com.jmunoz.trip_advisor.dto.Accommodation;
import com.jmunoz.trip_advisor.dto.FlightReservationRequest;
import com.jmunoz.trip_advisor.dto.FlightReservationResponse;
import com.jmunoz.trip_advisor.dto.Weather;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.util.List;

// NO ES UNA CLASE DE TEST.
// Esta es una clase sencilla para jugar con RestClient.
public class RestClientTests {

    private static final Logger log = LoggerFactory.getLogger(RestClientTests.class);

    @Test
    void simpleGet() {
        // Esto lo creamos SOLO UNA VEZ.
        var client = RestClient.create();

        var response = client.get()
                .uri("http://localhost:7070/sec02/weather/LAS")
                .retrieve()
                .body(Weather.class);    // Decodifica el body de la respuesta como un objeto Weather.

        log.info("response: {}", response);
    }

    // Solo debemos crear una vez un RestClient y usarlo en toda la aplicación.
    // Esto es porque cuando se crea un RestClient, crea muchos pools de conexiones y si se vuelve a crear
    // otro RestClient se van a volver a crear los pools de conexiones, lo que afecta al rendimiento.
    @Test
    void baseUrl() {
        // Esto lo creamos SOLO UNA VEZ.
        var client = RestClient.builder()
                .baseUrl("http://localhost:7070/sec02/weather/")
                .build();

        var response = client.get()
                .uri("{airportCode}", "LAS")
                .retrieve()
                .body(Weather.class);    // Decodifica el body de la respuesta como un objeto Weather.

        log.info("response: {}", response);
    }

    // Si esperamos una respuesta que sea una lista.
    @Test
    void listResponse() {
        // Esto lo creamos SOLO UNA VEZ.
        var client = RestClient.builder()
                .baseUrl("http://localhost:7070/sec02/accommodations/")
                .build();

        var response = client.get()
                .uri("{airportCode}", "LAS")
                .retrieve()
                // Para obtener una lista
                .body(new ParameterizedTypeReference<List<Accommodation>>() {
                });

        log.info("response: {}", response);
    }

    // Enviar una petición POST sencilla.
    @Test
    void postRequest() {
        // Esto lo creamos SOLO UNA VEZ.
        var client = RestClient.builder()
                .baseUrl("http://localhost:7070/sec03/flight/reserve/")
                .build();

        var request = new FlightReservationRequest("ATL", "LAS", "UA789", LocalDate.now());

        var response = client.post()
                .body(request)
                // Si necesitamos pasar información en la cabecera, o cualquier cosa de la petición,
                // hacerlo siempre antes de .retrieve().
//                .header("token", "value")
                .retrieve()
                .body(FlightReservationResponse.class);

        log.info("response: {}", response);
    }
}
