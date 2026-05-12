# Canlı Uçuş Bilgisi İzleme ve Jet Stream Tespiti


### Nedir bu?

Bu demoda backend'de Spring Boot, Spring Data JPA  

1. Hexagonal proje mimarisi ile modüler çözüm.
2. 
com.tusas.vitrin/
├── TusasVitrinApplication.java
├── domain/model/
│   └── FlightData.java                        ← domain model
├── application/
│   ├── port/input/FlightDataUseCase.java      ← input port
│   ├── port/output/FlightDataOutputPort.java  ← output port
│   └── service/FlightDataService.java         ← application service
└── infrastructure/
    ├── adapter/input/
    │   ├── rest/           ← POST + GET /api/flights (JSON)
    │   └── websocket/      ← STOMP /app/flights → /topic/flights
    ├── adapter/output/
    │   └── persistence/    ← JPA + PostgreSQL adapter
    └── config/
        ├── SecurityConfig.java    ← HTTP Basic, CORS
        └── WebSocketConfig.java  ← /ws-native + /ws (SockJS)