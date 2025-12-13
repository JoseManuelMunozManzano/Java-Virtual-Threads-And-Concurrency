package com.jmunoz.trip_advisor.dto;

import java.time.LocalDate;

public record TripReservationRequest(String departure,
                                     String arrival,
                                     LocalDate date) {
}
