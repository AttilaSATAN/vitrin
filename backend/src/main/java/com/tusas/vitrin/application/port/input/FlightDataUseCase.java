package com.tusas.vitrin.application.port.input;

import com.tusas.vitrin.domain.model.FlightData;

import java.util.List;

/**
 * Input port defining the use cases for managing flight telemetry data.
 */
public interface FlightDataUseCase {

    /**
     * Persists new flight telemetry data and broadcasts it to WebSocket subscribers.
     *
     * @param flightData the flight data to save
     * @return the persisted FlightData with a generated id and timestamp
     */
    FlightData saveFlightData(FlightData flightData);

    /**
     * Retrieves all stored flight telemetry records.
     *
     * @return list of all FlightData records
     */
    List<FlightData> getAllFlightData();
}
