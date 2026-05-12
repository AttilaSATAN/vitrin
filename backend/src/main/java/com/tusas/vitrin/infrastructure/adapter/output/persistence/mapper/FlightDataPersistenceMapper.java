package com.tusas.vitrin.infrastructure.adapter.output.persistence.mapper;

import com.tusas.vitrin.domain.model.FlightData;
import com.tusas.vitrin.infrastructure.adapter.output.persistence.entity.FlightDataEntity;

/**
 * Mapper for converting between the FlightData domain model and FlightDataEntity.
 */
public final class FlightDataPersistenceMapper {

    private FlightDataPersistenceMapper() {
    }

    /**
     * Converts a FlightData domain model to a FlightDataEntity for JPA persistence.
     *
     * @param domain the domain model to convert
     * @return the corresponding JPA entity
     */
    public static FlightDataEntity toEntity(FlightData domain) {
        FlightDataEntity entity = new FlightDataEntity();
        entity.setId(domain.getId());
        entity.setFlightNumber(domain.getFlightNumber());
        entity.setLongitude(domain.getLongitude());
        entity.setLatitude(domain.getLatitude());
        entity.setAirSpeed(domain.getAirSpeed());
        entity.setRecordedAt(domain.getRecordedAt());
        return entity;
    }

    /**
     * Converts a FlightDataEntity to a FlightData domain model.
     *
     * @param entity the JPA entity to convert
     * @return the corresponding domain model instance
     */
    public static FlightData toDomain(FlightDataEntity entity) {
        return FlightData.builder()
                .id(entity.getId())
                .flightNumber(entity.getFlightNumber())
                .longitude(entity.getLongitude())
                .latitude(entity.getLatitude())
                .airSpeed(entity.getAirSpeed())
                .recordedAt(entity.getRecordedAt())
                .build();
    }
}
