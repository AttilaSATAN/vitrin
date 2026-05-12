package com.tusas.vitrin.infrastructure.adapter.output.persistence.repository;

import com.tusas.vitrin.infrastructure.adapter.output.persistence.entity.FlightDataEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for FlightDataEntity persistence operations.
 */
@Repository
public interface FlightDataJpaRepository extends JpaRepository<FlightDataEntity, Long> {
}
