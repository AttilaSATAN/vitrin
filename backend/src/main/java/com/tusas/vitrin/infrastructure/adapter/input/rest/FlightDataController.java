package com.tusas.vitrin.infrastructure.adapter.input.rest;

import com.tusas.vitrin.application.port.input.FlightDataUseCase;
import com.tusas.vitrin.domain.model.FlightData;
import com.tusas.vitrin.infrastructure.adapter.input.rest.dto.FlightDataRequest;
import com.tusas.vitrin.infrastructure.adapter.input.rest.dto.FlightDataResponse;
import com.tusas.vitrin.infrastructure.adapter.input.rest.mapper.FlightDataRestMapper;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST adapter exposing the /api/flights endpoint for flight data operations.
 */
@RestController
@RequestMapping("/api/flights")
public class FlightDataController {

    private final FlightDataUseCase flightDataUseCase;

    /**
     * Constructs a FlightDataController with the required use case dependency.
     *
     * @param flightDataUseCase the application-level flight data use case
     */
    public FlightDataController(FlightDataUseCase flightDataUseCase) {
        this.flightDataUseCase = flightDataUseCase;
    }

    /**
     * Creates a new flight data record. The saved record is also broadcast via WebSocket
     * to all /topic/flights subscribers.
     *
     * @param request the validated flight data request body
     * @return 201 Created with the persisted FlightDataResponse
     */
    @PostMapping
    public ResponseEntity<FlightDataResponse> createFlightData(@Valid @RequestBody FlightDataRequest request) {
        FlightData domain = FlightDataRestMapper.toDomain(request);
        FlightData saved = flightDataUseCase.saveFlightData(domain);
        return ResponseEntity.status(HttpStatus.CREATED).body(FlightDataRestMapper.toResponse(saved));
    }

    /**
     * Retrieves all stored flight data records.
     *
     * @return 200 OK with a list of FlightDataResponse objects
     */
    @GetMapping
    public ResponseEntity<List<FlightDataResponse>> getAllFlightData() {
        List<FlightDataResponse> responses = flightDataUseCase.getAllFlightData().stream()
                .map(FlightDataRestMapper::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }
}
