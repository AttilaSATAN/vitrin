# Canlı Uçuş Bilgisi İzleme

Farklı kaynaklardan gelen uçuş datası (flightData) Spring Boot backend'i ile postgres'e kaydedilirken websocket /(STOMP subprotokolü) ile vue frontend yeni uçuş bilgisinden haberdar edilmektedir. Uçuş verileri go ile yazılmış küçük bir uygulama ile simüle edilmektedir. 

Sistem 3 temel elementten oluşmaktadır. `backend`, `frontend` ve `producer`. 

- `backend`: REST ve WS sunucusu olarak sistemin merkezindedir.
- `frontend`: Vue ile yapılmış basit bir harita uygulamasıdır.
- `producer`: Mock flight data (flightNumber, Latitude, Longtitude ve airSpeed) sağlayıcısıdır. 5sn'de bir backend'i bilgilendirir. 

> Local, docker compile ve kubernetes ile deoploy edilebilmektedir. Kubernetes altında çalışıken `backend` yeni `producer` podları spawn edebilmektedir.


## Nasıl?:

**Local Yayın:** 
Gereksinimler: 
* Local postgres sunucusu, 
* go (Golang) enviroiment, 
* mvn enviroiment, nodejs. (Versiyon uygunluk analizi yapılmadı.)

Öncelikle root (project root) dizindeki `.env.example` dosyasını `.env` olarak kopyalayın ve ortam değişkenlerini sisteminize göre düzenleyin.

Backend: 
```bash
cd backend
mvn spring-boot:run
```
Frontend: 
```bash
cd frontend
npm run dev
```
Producer: Birden çok producer çalıştırabilirsiniz. Producer port bind etmez.
```bash
cd producer
go run .
```

**Compose:** Sisteminizde çalışan bir docker olduğu sürece `docker compose up --build` yeterlidir.  
 
**Kubernetes:** Her bir deployment config dosyası `k8s` dizini altında bulunmaktadır. Minicube deployment örneği için `KUBERNETES.md` dosyasını inceleyiniz. 

> `/spawn` endpointi ile yeni producer kubernetes podları oluşturmak için sistemin kubernetes ile deploy edilmesi gerekmektedir. Birden çok uçuşu simüle etmek isterseniz birden çok producerı çalıştırabilirsiniz. Windows'da VSCode integrated terminal altında `go run .` ile çalıştırılan uygulamalar çalışma esnasında VSCode kapatılırsa arka planda çalışmaya devam edebilmektedir. Task Manager'dan kapatabilirsiniz. Linux de bu sorunla karşılaşmadım ancak Mac'lerde bu sorun mevcut mu emin değilim.  

## Backend

Backend'de Spring Boot, Data JPA, starter websocket, postgres, io.kubernetes temel bileşenlerdir. Basic Auth ve starter-validation basit bir güvenlik uygulaması olarak kullanıldı. Yatayda scale için bir message broker'a (RabitMQ gibi) ihtiyaç vardır ancak zaman kaygısı sebebiyle implemente edilmemiştir. Backend'in aynı zamanda kendine ait bir kubernetes `ServiceAccount`'u vardır ki (bkz: `./k8s/backend/rbac.yml`) bunun sayesinde yeni producer podları spawn edebilmektedir. Birden çok uçuşu simüle etmek için kullanılmaktadır.

1. Hexagonal proje mimarisi esas alınarak best practices implemente edildi.
2. `producer` ile gelen uçuş bilgisini postgres'e yazar ve frontend'i bilgilendirir.
3. `/spawn` endpoint'i ile yeni `producer` podu oluşturabilir.
4. dto ve mapper yapısı sayesinde request ve persistance arası veri yapı dönüşümlerinde (data structure changes) netlik ve kolay uygulanabilirlik sağlanmıştır.

Genel hatları ile proje mimarisi şu şekildedir:


```
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
    │   ├── persistence/    ← JPA + PostgreSQL adapter
    │   └── kubernetes/     ← Kubernetes service connection
    └── config/
        ├── SecurityConfig.java    ← HTTP Basic, CORS
        └── WebSocketConfig.java  ← /ws-native + /ws (SockJS)
```

**Eksikler:** 
- Bir message broker implementasyonu, horizontal scale için gereklidir. Şu anda ws bağlantıları tek bir sunucu instance'ının raminde durmaktadır. 
- Yukarıdaki değişiklikten sonra auto scale policy uygulanabilir. Halihazırda ingress ile load balancing yapılmaktadır. (1 sunucuya :) )
- JWT gibi daha gelişmiş bir auth daha uygun olur.
 

## Frontend

Basit bir vuejs uygulamasıdır. stompjs ve maplibre-gl ana bileşenleridir. backend verisini harita üzerine yarleştirir. ws (STOMP) ile canlı veri akışı sağlar. Yeni producer podları oluşturulabilir (sadece kubernetes'de).

## Producer

Uçuş verisi mock etmek için geliştirilmiş bir uygulamadır. Port dinlemesi yapmadığından birden çok örneği çalıştırılabilir. 5sn'de bir Enlem, Boylam ve kt cinsinden Hava hızını bildiği hava aracının yeni konumunu hesaplar ve backend'e uçuşun konumunu POST eder.


