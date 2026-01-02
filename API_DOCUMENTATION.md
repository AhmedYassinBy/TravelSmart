# TravelSmart - API Backend pour Plateforme d'Agence de Voyage

## 📋 Description

Backend Spring Boot 3 qui consomme des **APIs de voyage RÉELLES** pour récupérer des données en temps réel :
- **Hôtels** (Amadeus & Booking.com)
- **Vols** (Amadeus)
- **Compagnies aériennes** (AviationStack)
- **Aéroports** (AviationStack)

**⚠️ AUCUNE donnée statique ou fictive - Tout vient d'APIs externes**

---

## 🏗️ Architecture

```
Controller → Service → Client API → API Externe
```

### Structure du projet

```
src/main/java/com/ahmedyassin/TravelSmart/
├── clients/                     # Clients pour appeler les APIs externes
│   ├── AmadeusApiClient.java
│   ├── AviationStackApiClient.java
│   └── BookingApiClient.java
├── config/
│   └── WebClientConfig.java    # Configuration WebClient avec timeouts
├── controllers/external/        # Controllers REST
│   ├── HotelController.java
│   ├── FlightController.java
│   ├── AirlineController.java
│   └── AirportController.java
├── dto/external/               # DTOs pour les réponses
│   ├── HotelDTO.java
│   ├── FlightDTO.java
│   ├── AirlineDTO.java
│   ├── AirportDTO.java
│   └── response/               # DTOs brutes des APIs
│       ├── AmadeusHotelResponse.java
│       ├── AmadeusFlightResponse.java
│       ├── AmadeusTokenResponse.java
│       ├── AviationStackAirlineResponse.java
│       ├── AviationStackAirportResponse.java
│       └── BookingHotelResponse.java
├── services/external/          # Services métier
│   ├── HotelService.java
│   ├── FlightService.java
│   ├── AirlineService.java
│   └── AirportService.java
└── exceptions/                 # Gestion des erreurs
    ├── ExternalApiException.java
    └── GlobalExceptionHandler.java
```

---

## 🔑 Configuration des APIs

### 1. Obtenir les clés API

#### **Amadeus API** (Hôtels & Vols)
1. Créer un compte sur [https://developers.amadeus.com/](https://developers.amadeus.com/)
2. Créer une application
3. Récupérer `API Key` et `API Secret`

#### **AviationStack** (Airlines & Airports)
1. Créer un compte sur [https://aviationstack.com/](https://aviationstack.com/)
2. Récupérer l'`Access Key`

#### **Booking.com via RapidAPI**
1. Créer un compte sur [https://rapidapi.com/](https://rapidapi.com/)
2. S'abonner à [Booking.com API](https://rapidapi.com/apidojo/api/booking-com/)
3. Récupérer la `RapidAPI Key`

### 2. Configurer application.properties

Éditer `src/main/resources/application.properties` :

```properties
# Amadeus API
amadeus.api.base-url=https://test.api.amadeus.com
amadeus.api.key=VOTRE_CLE_AMADEUS
amadeus.api.secret=VOTRE_SECRET_AMADEUS

# AviationStack API
aviationstack.api.base-url=https://api.aviationstack.com/v1
aviationstack.api.key=VOTRE_CLE_AVIATIONSTACK

# Booking.com API via RapidAPI
rapidapi.booking.base-url=https://booking-com.p.rapidapi.com/v1
rapidapi.booking.key=VOTRE_CLE_RAPIDAPI
rapidapi.booking.host=booking-com.p.rapidapi.com

# API Timeout Configuration
api.timeout.connect=10
api.timeout.read=30
```

---

## 🚀 Démarrage

### Prérequis
- Java 21
- Maven
- PostgreSQL (pour l'authentification)

### Lancer l'application

```bash
mvn clean install
mvn spring-boot:run
```

Le serveur démarre sur : **http://localhost:8085**

---

## 📡 Endpoints REST

### 🏨 **Hôtels**

#### 1. Rechercher des hôtels (Amadeus)
```http
GET /api/hotels/search?cityCode=PAR
```

**Paramètres :**
- `cityCode` : Code IATA de la ville (ex: PAR, LON, NYC)

**Exemple :**
```bash
curl -X GET "http://localhost:8085/api/hotels/search?cityCode=PAR"
```

#### 2. Rechercher des hôtels (Booking.com)
```http
GET /api/hotels/booking?destination=-553173&checkinDate=2025-01-15&checkoutDate=2025-01-20
```

**Paramètres :**
- `destination` : ID de destination Booking
- `checkinDate` : Date d'arrivée (YYYY-MM-DD)
- `checkoutDate` : Date de départ (YYYY-MM-DD)

**Exemple :**
```bash
curl -X GET "http://localhost:8085/api/hotels/booking?destination=-553173&checkinDate=2025-01-15&checkoutDate=2025-01-20"
```

---

### ✈️ **Vols**

#### Rechercher des vols
```http
GET /api/flights/search?origin=CDG&destination=JFK&departureDate=2025-12-31&adults=1
```

**Paramètres :**
- `origin` : Code IATA aéroport de départ (ex: CDG, JFK)
- `destination` : Code IATA aéroport d'arrivée
- `departureDate` : Date de départ (YYYY-MM-DD)
- `adults` : Nombre d'adultes (optionnel, défaut: 1)

**Exemple :**
```bash
curl -X GET "http://localhost:8085/api/flights/search?origin=CDG&destination=JFK&departureDate=2025-12-31&adults=2"
```

---

### 🛫 **Aéroports**

#### Rechercher des aéroports
```http
GET /api/airports/search?airportName=Charles&country=France
```

**Paramètres :**
- `airportName` : Nom de l'aéroport (optionnel)
- `country` : Pays (optionnel)

**Exemple :**
```bash
curl -X GET "http://localhost:8085/api/airports/search?country=France"
```

---

### 🛩️ **Compagnies aériennes**

#### Rechercher des compagnies aériennes
```http
GET /api/airlines/search?airlineName=Air France
```

**Paramètres :**
- `airlineName` : Nom de la compagnie (optionnel)

**Exemple :**
```bash
curl -X GET "http://localhost:8085/api/airlines/search?airlineName=Emirates"
```

---

## 📊 Exemples de réponses JSON

### Hôtel (HotelDTO)
```json
{
  "hotelId": "HLPAR123",
  "name": "Hotel Paris Centre",
  "address": "123 Rue de Rivoli",
  "city": "Paris",
  "country": "FR",
  "latitude": 48.8566,
  "longitude": 2.3522,
  "stars": 4,
  "minPrice": 150.00,
  "currency": "EUR",
  "rating": 8.5,
  "reviewCount": 1250,
  "imageUrl": "https://...",
  "amenities": ["WIFI", "PARKING", "POOL"]
}
```

### Vol (FlightDTO)
```json
{
  "flightNumber": "AF123",
  "airlineName": "Air France",
  "airlineCode": "AF",
  "departureAirport": "Charles de Gaulle",
  "departureAirportCode": "CDG",
  "arrivalAirport": "JFK International",
  "arrivalAirportCode": "JFK",
  "departureTime": "2025-12-31T10:30:00",
  "arrivalTime": "2025-12-31T18:45:00",
  "duration": "PT8H15M",
  "price": 450.00,
  "currency": "EUR",
  "availableSeats": 15,
  "cabinClass": "ECONOMY"
}
```

### Aéroport (AirportDTO)
```json
{
  "airportCode": "CDG",
  "airportName": "Charles de Gaulle International Airport",
  "city": "Paris",
  "country": "France",
  "countryCode": "FR",
  "latitude": 49.0097,
  "longitude": 2.5479,
  "timezone": "Europe/Paris",
  "iataCode": "CDG",
  "icaoCode": "LFPG"
}
```

### Compagnie aérienne (AirlineDTO)
```json
{
  "airlineCode": "AF",
  "airlineName": "Air France",
  "iataCode": "AF",
  "icaoCode": "AFR",
  "country": "France",
  "callsign": "AIRFRANS",
  "active": true
}
```

---

## 🛡️ Gestion des erreurs

Toutes les erreurs d'API externe sont gérées par `GlobalExceptionHandler` :

```json
{
  "timestamp": "2025-12-24T12:30:00",
  "status": 503,
  "error": "External API Error",
  "message": "Failed to search hotels from Amadeus"
}
```

**Codes d'erreur :**
- `503 Service Unavailable` : API externe indisponible
- `500 Internal Server Error` : Erreur interne
- `400 Bad Request` : Paramètres invalides

---

## 🔒 Sécurité

- Les endpoints des APIs externes sont **publics** (pas besoin de token)
- Les clés API sont **sécurisées** dans `application.properties`
- **Ne jamais exposer les clés au frontend**

---

## 🧪 Tests avec Postman

### Collection Postman

Importer dans Postman :

**GET Hôtels Amadeus**
```
GET http://localhost:8085/api/hotels/search?cityCode=LON
```

**GET Vols**
```
GET http://localhost:8085/api/flights/search?origin=LON&destination=PAR&departureDate=2025-01-15&adults=1
```

**GET Aéroports**
```
GET http://localhost:8085/api/airports/search?country=United Kingdom
```

**GET Compagnies**
```
GET http://localhost:8085/api/airlines/search?airlineName=British Airways
```

---

## 📝 Codes IATA utiles

### Villes (pour hôtels)
- **PAR** : Paris
- **LON** : Londres
- **NYC** : New York
- **DXB** : Dubaï
- **TYO** : Tokyo

### Aéroports (pour vols)
- **CDG** : Paris Charles de Gaulle
- **LHR** : London Heathrow
- **JFK** : New York JFK
- **DXB** : Dubai International
- **NRT** : Tokyo Narita

---

## 🔧 Technologies utilisées

- **Spring Boot 3.4.7**
- **Spring WebFlux** (WebClient)
- **Spring Security**
- **Lombok**
- **Jackson** (JSON mapping)
- **Reactor Netty** (HTTP client)
- **PostgreSQL**

---

## 📦 Dépendances Maven

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webflux</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
</dependency>
```

---

## 🚨 Notes importantes

### Limites des APIs gratuites

- **Amadeus Test** : 1000 requêtes/mois
- **AviationStack Free** : 100 requêtes/mois
- **RapidAPI Booking Free** : 500 requêtes/mois

### Environnement de test vs production

Pour **production**, remplacer :
```properties
amadeus.api.base-url=https://api.amadeus.com  # Production
```

---

## 🎯 Prochaines étapes

- [ ] Implémenter un **cache Redis** pour réduire les appels API
- [ ] Ajouter **rate limiting** côté backend
- [ ] Créer des **tests unitaires** avec MockWebServer
- [ ] Ajouter **Swagger UI** pour la documentation
- [ ] Implémenter **pagination** pour les résultats

---

## 👨‍💻 Auteur

**Ahmed Yassin**  
Projet : TravelSmart Backend

---

## 📄 Licence

Ce projet est privé et à usage éducatif.

---

## 🆘 Support

En cas de problème :
1. Vérifier que les **clés API sont valides**
2. Vérifier les **logs** dans la console
3. Tester les APIs externes directement (Postman)
4. Vérifier les **quotas** des APIs gratuites

---

**✅ Le backend est maintenant prêt à être consommé par votre frontend Angular!**

