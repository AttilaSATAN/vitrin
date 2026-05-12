package com.tusas.vitrin.infrastructure.adapter.input.rest.mapper;

import com.tusas.vitrin.domain.model.FlightData;
import com.tusas.vitrin.infrastructure.adapter.input.rest.dto.FlightDataRequest;
import com.tusas.vitrin.infrastructure.adapter.input.rest.dto.FlightDataResponse;

/**
 * Mapper for converting between REST DTOs and the FlightData domain model.
 */
public final class FlightDataRestMapper {

    private FlightDataRestMapper() {
    }

    /**
     * Converts a FlightDataRequest DTO into a FlightData domain model.
     *
     * @param request the incoming REST request DTO
     * @return the corresponding domain model instance
     */
    public static FlightData toDomain(FlightDataRequest request) {
        return FlightData.builder()
                .flightNumber(request.getFlightNumber())
                .longitude(request.getLongitude())
                .latitude(request.getLatitude())
                .airSpeed(request.getAirSpeed())
                .build();
    }

    /**
     * Converts a FlightData domain model into a FlightDataResponse DTO.
     *
     * @param domain the domain model instance
     * @return the corresponding response DTO
     */
    public static FlightDataResponse toResponse(FlightData domain) {
        return new FlightDataResponse(
                domain.getId(),
                domain.getFlightNumber(),
                domain.getLongitude(),
                domain.getLatitude(),
                domain.getAirSpeed(),
                domain.getRecordedAt()
        );
    }
}
