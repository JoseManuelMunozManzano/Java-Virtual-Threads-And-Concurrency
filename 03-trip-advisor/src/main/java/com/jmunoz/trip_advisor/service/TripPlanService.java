package com.jmunoz.trip_advisor.service;

import com.jmunoz.trip_advisor.client.*;
import com.jmunoz.trip_advisor.dto.TripPlan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

// En este servicio inyectamos los service clients correspondientes a Trip Planning Service Providers (sec02)
@Service
public class TripPlanService {

    private static final Logger log = LoggerFactory.getLogger(TripPlanService.class);
    private final EventServiceClient eventServiceClient;
    private final WeatherServiceClient weatherServiceClient;
    private final AccommodationServiceClient accommodationServiceClient;
    private final TransportationServiceClient transportationServiceClient;
    private final LocalRecommendationServiceClient localRecommendationServiceClient;

    // Como parte de la clase de configuración expondremos este ExecutorService bean,
    // para que Spring inyecte to-do en el constructor.
    // Necesitamos un ExecutorService porque todos estos client son llamadas independientes.
    // Usando ExecutorService podremos hacer llamadas en paralelo.
    private final ExecutorService executor;

    public TripPlanService(EventServiceClient eventServiceClient, WeatherServiceClient weatherServiceClient,
                           AccommodationServiceClient accommodationServiceClient,
                           TransportationServiceClient transportationServiceClient,
                           LocalRecommendationServiceClient localRecommendationServiceClient,
                           ExecutorService executor) {
        this.eventServiceClient = eventServiceClient;
        this.weatherServiceClient = weatherServiceClient;
        this.accommodationServiceClient = accommodationServiceClient;
        this.transportationServiceClient = transportationServiceClient;
        this.localRecommendationServiceClient = localRecommendationServiceClient;
        this.executor = executor;
    }

    public TripPlan getTripPlan(String airportCode) {
        var events = this.executor.submit(() -> this.eventServiceClient.getEvents(airportCode));
        var weather = this.executor.submit(() -> this.weatherServiceClient.getWeather(airportCode));
        var accommodations = this.executor.submit(() -> this.accommodationServiceClient.getAccommodations(airportCode));
        var transportation = this.executor.submit(() -> this.transportationServiceClient.getTransportation(airportCode));
        var recommendations = this.executor.submit(() -> this.localRecommendationServiceClient.getRecommendations(airportCode));

        return new TripPlan(
                airportCode,
                getOrElse(accommodations, Collections.emptyList()),
                getOrElse(weather, null),
                getOrElse(events, Collections.emptyList()),
                getOrElse(recommendations, null),
                getOrElse(transportation, null)
        );
    }

    // Si falla un servicio, no significa que toda la respuesta sea fallida.
    // Daremos al usuario la información que nos venga correcta.
    private <T> T getOrElse(Future<T> future, T defaultValue) {
        try {
            return future.get();
        } catch (Exception e) {
            log.error("error", e);
        }

        return defaultValue;
    }
}
