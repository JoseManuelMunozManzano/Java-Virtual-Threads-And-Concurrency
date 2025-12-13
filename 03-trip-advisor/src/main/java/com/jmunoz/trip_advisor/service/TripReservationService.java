package com.jmunoz.trip_advisor.service;

import com.jmunoz.trip_advisor.client.FlightReservationServiceClient;
import com.jmunoz.trip_advisor.client.FlightSearchServiceClient;
import com.jmunoz.trip_advisor.dto.Flight;
import com.jmunoz.trip_advisor.dto.FlightReservationRequest;
import com.jmunoz.trip_advisor.dto.FlightReservationResponse;
import com.jmunoz.trip_advisor.dto.TripReservationRequest;
import org.springframework.stereotype.Service;

import java.util.Comparator;

// En este servicio inyectamos los service clients correspondientes a Flight Search Reservation Service Providers (sec03)
@Service
public class TripReservationService {

    private final FlightSearchServiceClient searchServiceClient;
    private final FlightReservationServiceClient reservationServiceClient;

    public TripReservationService(FlightSearchServiceClient searchServiceClient, FlightReservationServiceClient reservationServiceClient) {
        this.searchServiceClient = searchServiceClient;
        this.reservationServiceClient = reservationServiceClient;
    }

    // Pasamos la petición del usuario.
    public FlightReservationResponse reserve(TripReservationRequest request) {
        // Vamos a hacer llamadas secuenciales a los service client.
        var flights = this.searchServiceClient.getFlights(request.departure(), request.arrival());
        var bestDeal = flights.stream().min(Comparator.comparingInt(Flight::price));
        var flight = bestDeal.orElseThrow(() -> new IllegalStateException("No flights found"));
        var reservationRequest = new FlightReservationRequest(request.departure(), request.arrival(), flight.flightNumber(), request.date());
        return this.reservationServiceClient.reserve(reservationRequest);
    }
}
