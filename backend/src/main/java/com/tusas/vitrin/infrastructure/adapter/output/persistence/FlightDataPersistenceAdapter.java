package com.tusas.vitrin.infrastructure.adapter.output.persistence;

import com.tusas.vitrin.application.port.output.FlightDataOutputPort;
import com.tusas.vitrin.domain.model.FlightData;
import com.tusas.vitrin.infrastructure.adapter.output.persistence.entity.FlightDataEntity;
import com.tusas.vitrin.infrastructure.adapter.output.persistence.mapper.FlightDataPersistenceMapper;
import com.tusas.vitrin.infrastructure.adapter.output.persistence.repository.FlightDataJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Persistence adapter implementing the FlightDataOutputPort using Spring Data JPA.
 */
@Component
public class FlightDataPersistenceAdapter implements FlightDataOutputPort {

    private final FlightDataJpaRepository repository;

    /**
     * Constructs a FlightDataPersistenceAdapter with the required JPA repository.
     *
     * @param repository the Spring Data JPA repository
     */
    public FlightDataPersistenceAdapter(FlightDataJpaRepository repository) {
        this.repository = repository;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FlightData save(FlightData flightData) {
        FlightDataEntity entity = FlightDataPersistenceMapper.toEntity(flightData);
        FlightDataEntity saved = repository.save(entity);
        return FlightDataPersistenceMapper.toDomain(saved);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<FlightData> findAll() {
        return repository.findAll().stream()
                .map(FlightDataPersistenceMapper::toDomain)
                .collect(Collectors.toList());
    }
}
