package com.tusas.vitrin.infrastructure.adapter.input.websocket;

import com.tusas.vitrin.application.port.input.FlightDataUseCase;
import com.tusas.vitrin.domain.model.FlightData;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

/**
 * WebSocket adapter handling STOMP messages for flight data.
 * Clients connect via the /ws-native endpoint and communicate using STOMP protocol.
 *
 * <p>Send to:     {@code /app/flights}
 * <p>Subscribe to: {@code /topic/flights}
 */
@Controller
public class FlightWebSocketController {

    private final FlightDataUseCase flightDataUseCase;

    /**
     * Constructs a FlightWebSocketController with the required use case dependency.
     *
     * @param flightDataUseCase the application-level flight data use case
     */
    public FlightWebSocketController(FlightDataUseCase flightDataUseCase) {
        this.flightDataUseCase = flightDataUseCase;
    }

    /**
     * Handles flight data sent by WebSocket clients to /app/flights.
     * Persists the data and broadcasts the result to all /topic/flights subscribers.
     *
     * @param flightData the flight data payload received from the client
     * @return the saved FlightData domain model, broadcast to all subscribers
     */
    @MessageMapping("/flights")
    @SendTo("/topic/flights")
    public FlightData handleFlightData(FlightData flightData) {
        return flightDataUseCase.saveFlightData(flightData);
    }
}
