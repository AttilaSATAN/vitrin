package main

import (
	"bytes"
	"encoding/json"
	"fmt"
	"log"
	"math"
	"math/rand"
	"net/http"
	"os"
	"path/filepath"
	"runtime"
	"time"

	"github.com/joho/godotenv"
)

// flightPayload matches the FlightDataRequest DTO on the Spring Boot API.
type flightPayload struct {
	FlightNumber string  `json:"flightNumber"`
	Longitude    float64 `json:"longitude"`
	Latitude     float64 `json:"latitude"`
	AirSpeed     float64 `json:"airSpeed"`
}

// flight holds the persistent state of a simulated flight across ticks.
type flight struct {
	flightNumber string
	latitude     float64
	longitude    float64
	airSpeed     float64 // knots
	directionDeg float64 // compass bearing 0–360°
}

// rootEnvPath returns the absolute path to the .env file in the project root
// (one level above the producer directory).
func rootEnvPath() string {
	_, file, _, _ := runtime.Caller(0)
	return filepath.Join(filepath.Dir(file), "..", ".env")
}

// loadEnv loads variables from the root .env file into the process environment.
// When running inside a Kubernetes pod the file will not be present; in that
// case the function logs a warning and returns — env vars are injected by K8s.
func loadEnv() {
	envPath := rootEnvPath()
	if err := godotenv.Load(envPath); err != nil {
		log.Printf("Warning: could not load .env from %s: %v (using environment variables)", envPath, err)
	}
}

// randomFlightNumber generates a flight number by picking a random airline prefix
// and appending a random 3-digit number (100–999).
func randomFlightNumber() string {
	prefixes := []string{"TK", "PC", "XQ", "TF", "AJ"}
	return fmt.Sprintf("%s%d", prefixes[rand.Intn(len(prefixes))], 100+rand.Intn(900))
}

// newFlight creates a flight with a random starting position over Turkish airspace,
// a random cruising speed (300–600 kts) and a random compass direction (0–360°).
func newFlight() flight {
	return flight{
		flightNumber: randomFlightNumber(),
		latitude:     36.0 + rand.Float64()*(42.0-36.0),
		longitude:    26.0 + rand.Float64()*(45.0-26.0),
		airSpeed:     300.0 + rand.Float64()*300.0,
		directionDeg: rand.Float64() * 360.0,
	}
}

// advance moves the flight forward by elapsedSeconds using a flat-earth approximation.
// airSpeed is in knots (nautical miles per hour); 1 nautical mile ≈ 1/60 degree of latitude.
func (f *flight) advance(elapsedSeconds float64) {
	const nmPerDeg = 60.0 // nautical miles per degree of latitude
	distanceNM := f.airSpeed * (elapsedSeconds / 3600.0)

	bearingRad := f.directionDeg * math.Pi / 180.0
	deltaLat := (distanceNM * math.Cos(bearingRad)) / nmPerDeg
	// longitude degrees shrink with latitude
	deltaLon := (distanceNM * math.Sin(bearingRad)) / (nmPerDeg * math.Cos(f.latitude*math.Pi/180.0))

	f.latitude += deltaLat
	f.longitude += deltaLon
}

// toPayload converts the current flight state into an API request payload.
func (f *flight) toPayload() flightPayload {
	return flightPayload{
		FlightNumber: f.flightNumber,
		Longitude:    f.longitude,
		Latitude:     f.latitude,
		AirSpeed:     f.airSpeed,
	}
}

// postFlight POSTs the current position of the flight to the API server.
func postFlight(f *flight) {

	apiURL := os.Getenv("API_URL") + "/api/flights"

	payload := f.toPayload()
	body, err := json.Marshal(payload)
	if err != nil {
		log.Printf("Marshal error: %v", err)
		return
	}

	req, err := http.NewRequest(http.MethodPost, apiURL, bytes.NewReader(body))
	if err != nil {
		log.Printf("Request build error: %v", err)
		return
	}
	req.Header.Set("Content-Type", "application/json")
	req.SetBasicAuth("admin", "password")

	resp, err := http.DefaultClient.Do(req)
	if err != nil {
		log.Printf("POST %s error: %v", apiURL, err)
		return
	}
	defer resp.Body.Close()

	log.Printf("[%s] dir=%.1f° spd=%.0fkts lat=%.4f lon=%.4f → HTTP %d",
		f.flightNumber, f.directionDeg, f.airSpeed, f.latitude, f.longitude, resp.StatusCode)
}

func main() {

	loadEnv()

	// Initialise the flight with a random number, position and direction.
	f := newFlight()
	log.Printf("Flight created: %s  direction=%.1f°  speed=%.0f kts  start=(%.4f, %.4f)",
		f.flightNumber, f.directionDeg, f.airSpeed, f.latitude, f.longitude)

	const tickSeconds = 5.0

	// Post immediately, then advance position and post every 5 seconds.
	postFlight(&f)
	ticker := time.NewTicker(tickSeconds * time.Second)
	defer ticker.Stop()
	for range ticker.C {
		f.advance(tickSeconds)
		postFlight(&f)
	}
}
