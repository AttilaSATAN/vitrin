package com.tusas.vitrin.infrastructure.adapter.output.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * JPA entity mapping the flight_data table in PostgreSQL.
 */
@Entity
@Table(name = "flight_data")
public class FlightDataEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "flight_number", nullable = false, length = 10)
    private String flightNumber;

    @Column(nullable = false)
    private Double longitude;

    @Column(nullable = false)
    private Double latitude;

    @Column(name = "air_speed", nullable = false)
    private Double airSpeed;

    @Column(name = "recorded_at")
    private LocalDateTime recordedAt;

    public FlightDataEntity() {
    }

    public Long getId() { return id; }
    public String getFlightNumber() { return flightNumber; }
    public Double getLongitude() { return longitude; }
    public Double getLatitude() { return latitude; }
    public Double getAirSpeed() { return airSpeed; }
    public LocalDateTime getRecordedAt() { return recordedAt; }

    public void setId(Long id) { this.id = id; }
    public void setFlightNumber(String flightNumber) { this.flightNumber = flightNumber; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    public void setAirSpeed(Double airSpeed) { this.airSpeed = airSpeed; }
    public void setRecordedAt(LocalDateTime recordedAt) { this.recordedAt = recordedAt; }
}
