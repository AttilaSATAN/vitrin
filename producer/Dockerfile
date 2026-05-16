# ---- Build stage ----
FROM golang:1.22-alpine AS builder

WORKDIR /app/producer

COPY go.mod go.sum ./
RUN go mod download

COPY . .
RUN CGO_ENABLED=0 GOOS=linux go build -a -installsuffix cgo -o producer .

# ---- Runtime stage ----
FROM alpine:3.21 AS runtime

RUN apk add --no-cache ca-certificates

# The binary resolves the .env file at compile-time to /app/.env (one level
# above WORKDIR). Mount a .env file at /app/.env, or pass environment variables
# directly: PG_HOST, PG_PORT, PG_DB, PG_USER, PG_PASSWORD.
WORKDIR /app/producer

COPY --from=builder /app/producer/producer .

ENTRYPOINT ["./producer"]
