package com.tusas.vitrin.infrastructure.adapter.input.rest.dto;

import java.time.LocalDateTime;

/**
 * DTO for outgoing flight data responses.
 */
public class FlightDataResponse {

    private Long id;
    private String flightNumber;
    private Double longitude;
    private Double latitude;
    private Double airSpeed;
    private LocalDateTime recordedAt;

    public FlightDataResponse() {
    }

    /**
     * Constructs a fully populated FlightDataResponse.
     *
     * @param id           the record identifier
     * @param flightNumber the flight number
     * @param longitude    the flight longitude
     * @param latitude     the flight latitude
     * @param airSpeed     the air speed in knots
     * @param recordedAt   the timestamp of the record
     */
    public FlightDataResponse(Long id, String flightNumber, Double longitude, Double latitude,
                               Double airSpeed, LocalDateTime recordedAt) {
        this.id = id;
        this.flightNumber = flightNumber;
        this.longitude = longitude;
        this.latitude = latitude;
        this.airSpeed = airSpeed;
        this.recordedAt = recordedAt;
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
