package com.tusas.vitrin.application.port.output;

import com.tusas.vitrin.domain.model.FlightData;

import java.util.List;

/**
 * Output port defining the persistence contract for flight data.
 */
public interface FlightDataOutputPort {

    /**
     * Persists a FlightData domain object.
     *
     * @param flightData the domain model to persist
     * @return the saved FlightData with a generated id
     */
    FlightData save(FlightData flightData);

    /**
     * Retrieves all persisted flight data records.
     *
     * @return list of all stored FlightData records
     */
    List<FlightData> findAll();
}
