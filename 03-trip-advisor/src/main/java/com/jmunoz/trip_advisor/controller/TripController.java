package com.jmunoz.trip_advisor.controller;

import com.jmunoz.trip_advisor.dto.FlightReservationResponse;
import com.jmunoz.trip_advisor.dto.TripPlan;
import com.jmunoz.trip_advisor.dto.TripReservationRequest;
import com.jmunoz.trip_advisor.service.TripPlanService;
import com.jmunoz.trip_advisor.service.TripReservationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("trip")
public class TripController {

//    private static final Logger log = LoggerFactory.getLogger(TripController.class);

    private final TripPlanService planService;
    private final TripReservationService reservationService;

    public TripController(TripPlanService planService, TripReservationService reservationService) {
        this.planService = planService;
        this.reservationService = reservationService;
    }

    // En vez de TripPlan podríamos devolver un ResponseEntity<TripPlan> para hacerlo incluso mejor.
    @GetMapping("{airportCode}")
    public TripPlan planTrip(@PathVariable String airportCode) {
//        log.info("airport code: {}, is Virtual: {}", airportCode, Thread.currentThread().isVirtual());
        return this.planService.getTripPlan(airportCode);
    }

    // En vez de FlightReservationResponse podríamos devolver un ResponseEntity<FlightReservationResponse> para hacerlo incluso mejor.
    @PostMapping("reserve")
    public FlightReservationResponse reserve(@RequestBody TripReservationRequest request) {
        return this.reservationService.reserve(request);
    }
}
