package com.tusas.vitrin.application.service;

import com.tusas.vitrin.application.port.input.FlightDataUseCase;
import com.tusas.vitrin.application.port.output.FlightDataOutputPort;
import com.tusas.vitrin.domain.model.FlightData;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Application service implementing the FlightDataUseCase.
 * Coordinates persistence and real-time broadcasting of flight telemetry data.
 */
@Service
public class FlightDataService implements FlightDataUseCase {

    private final FlightDataOutputPort outputPort;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Constructs a FlightDataService with the required dependencies.
     *
     * @param outputPort        the persistence output port
     * @param messagingTemplate the STOMP messaging template for WebSocket broadcasts
     */
    public FlightDataService(FlightDataOutputPort outputPort, SimpMessagingTemplate messagingTemplate) {
        this.outputPort = outputPort;
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * {@inheritDoc}
     * Sets the recordedAt timestamp before saving, then broadcasts the result to /topic/flights.
     */
    @Override
    public FlightData saveFlightData(FlightData flightData) {
        flightData.setRecordedAt(LocalDateTime.now());
        FlightData saved = outputPort.save(flightData);
        messagingTemplate.convertAndSend("/topic/flights", saved);
        return saved;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<FlightData> getAllFlightData() {
        return outputPort.findAll();
    }
}
