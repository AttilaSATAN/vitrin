package com.tusas.vitrin.domain.model;

import java.time.LocalDateTime;

/**
 * Domain model representing flight telemetry data, including position and speed.
 */
public class FlightData {

    private Long id;
    private String flightNumber;
    private Double longitude;
    private Double latitude;
    private Double airSpeed;
    private LocalDateTime recordedAt;

    public FlightData() {
    }

    private FlightData(Builder builder) {
        this.id = builder.id;
        this.flightNumber = builder.flightNumber;
        this.longitude = builder.longitude;
        this.latitude = builder.latitude;
        this.airSpeed = builder.airSpeed;
        this.recordedAt = builder.recordedAt;
    }

    /**
     * Returns a new builder for constructing a FlightData instance.
     *
     * @return a new Builder
     */
    public static Builder builder() {
        return new Builder();
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

    /**
     * Builder for constructing immutable FlightData instances.
     */
    public static class Builder {
        private Long id;
        private String flightNumber;
        private Double longitude;
        private Double latitude;
        private Double airSpeed;
        private LocalDateTime recordedAt;

        /** Sets the id. */
        public Builder id(Long id) { this.id = id; return this; }
        /** Sets the flight number. */
        public Builder flightNumber(String flightNumber) { this.flightNumber = flightNumber; return this; }
        /** Sets the longitude. */
        public Builder longitude(Double longitude) { this.longitude = longitude; return this; }
        /** Sets the latitude. */
        public Builder latitude(Double latitude) { this.latitude = latitude; return this; }
        /** Sets the air speed. */
        public Builder airSpeed(Double airSpeed) { this.airSpeed = airSpeed; return this; }
        /** Sets the recorded timestamp. */
        public Builder recordedAt(LocalDateTime recordedAt) { this.recordedAt = recordedAt; return this; }
        /** Builds the FlightData instance. */
        public FlightData build() { return new FlightData(this); }
    }
}
