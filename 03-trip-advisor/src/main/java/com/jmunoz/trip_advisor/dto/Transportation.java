package com.jmunoz.trip_advisor.dto;

import java.util.List;

public record Transportation(List<CarRental> carRentals,
                             List<PublicTransportation> publicTransportations) {
}
