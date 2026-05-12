package main

import (
	"context"
	"fmt"
	"log"
	"os"
	"path/filepath"
	"runtime"

	"github.com/jackc/pgx/v5"
	"github.com/joho/godotenv"
)

// rootEnvPath returns the absolute path to the .env file located two directories
// above the producer binary (i.e. the project root).
func rootEnvPath() string {
	_, file, _, _ := runtime.Caller(0)
	// producer/main.go → go up two levels to reach project root
	return filepath.Join(filepath.Dir(file), "..", ".env")
}

// connectDB loads the .env file from the project root and opens a pgx connection.
func connectDB(ctx context.Context) (*pgx.Conn, error) {
	envPath := rootEnvPath()
	if err := godotenv.Load(envPath); err != nil {
		return nil, fmt.Errorf("loading .env from %s: %w", envPath, err)
	}

	dsn := fmt.Sprintf(
		"host=%s port=%s dbname=%s user=%s password=%s sslmode=disable",
		os.Getenv("PG_HOST"),
		os.Getenv("PG_PORT"),
		os.Getenv("PG_DB"),
		os.Getenv("PG_USER"),
		os.Getenv("PG_PASSWORD"),
	)

	conn, err := pgx.Connect(ctx, dsn)
	if err != nil {
		return nil, fmt.Errorf("connecting to postgres: %w", err)
	}
	return conn, nil
}

func main() {
	ctx := context.Background()

	conn, err := connectDB(ctx)
	if err != nil {
		log.Fatalf("DB connection failed: %v", err)
	}
	defer conn.Close(ctx)
	createFlight(ctx, conn)
	if err := conn.Ping(ctx); err != nil {
		log.Fatalf("DB ping failed: %v", err)
	}

	log.Println("Connected to PostgreSQL successfully")
}

func createFlight(ctx context.Context, conn *pgx.Conn) error {
	flightNumber := "TK1234"
	_, err := conn.Exec(ctx, "INSERT INTO flights (flight_number) VALUES ($1)", flightNumber)
	return err
}
