# 📊 Exemples de Réponses JSON - TravelSmart API

Ce document contient des exemples concrets de réponses JSON que vous obtiendrez des endpoints.

---

## ✈️ Airlines (Compagnies aériennes)

### Endpoint
```
GET /api/airlines/search?airlineName=Air France
```

### Réponse
```json
[
  {
    "airlineCode": "AF",
    "airlineName": "Air France",
    "iataCode": "AF",
    "icaoCode": "AFR",
    "country": "France",
    "callsign": "AIRFRANS",
    "active": true
  },
  {
    "airlineCode": "AF",
    "airlineName": "Air France Cargo",
    "iataCode": "AF",
    "icaoCode": "AFC",
    "country": "France",
    "callsign": "AIRFRANS CARGO",
    "active": true
  }
]
```

---

## 🛫 Airports (Aéroports)

### Endpoint
```
GET /api/airports/search?country=France
```

### Réponse
```json
[
  {
    "airportCode": "CDG",
    "airportName": "Charles de Gaulle International Airport",
    "city": "PAR",
    "country": "France",
    "countryCode": "FR",
    "latitude": 49.012779,
    "longitude": 2.55,
    "timezone": "Europe/Paris",
    "iataCode": "CDG",
    "icaoCode": "LFPG"
  },
  {
    "airportCode": "ORY",
    "airportName": "Paris-Orly Airport",
    "city": "PAR",
    "country": "France",
    "countryCode": "FR",
    "latitude": 48.725278,
    "longitude": 2.359444,
    "timezone": "Europe/Paris",
    "iataCode": "ORY",
    "icaoCode": "LFPO"
  },
  {
    "airportCode": "NCE",
    "airportName": "Nice Côte d'Azur Airport",
    "city": "NCE",
    "country": "France",
    "countryCode": "FR",
    "latitude": 43.658411,
    "longitude": 7.215872,
    "timezone": "Europe/Paris",
    "iataCode": "NCE",
    "icaoCode": "LFMN"
  }
]
```

---

## 🏨 Hotels (Amadeus)

### Endpoint
```
GET /api/hotels/search?cityCode=PAR
```

### Réponse
```json
[
  {
    "hotelId": "HLPAR123",
    "name": "Hotel Lutetia Paris",
    "address": "45 Boulevard Raspail, 75006",
    "city": "Paris",
    "country": "FR",
    "latitude": 48.8513,
    "longitude": 2.3262,
    "stars": 5,
    "minPrice": null,
    "currency": null,
    "amenities": [
      "WIFI",
      "RESTAURANT",
      "BAR",
      "PARKING",
      "SPA",
      "GYM"
    ],
    "rating": null,
    "reviewCount": null,
    "imageUrl": null,
    "description": null
  },
  {
    "hotelId": "HLPAR456",
    "name": "Le Bristol Paris",
    "address": "112 Rue du Faubourg Saint-Honoré, 75008",
    "city": "Paris",
    "country": "FR",
    "latitude": 48.8711,
    "longitude": 2.3167,
    "stars": 5,
    "minPrice": null,
    "currency": null,
    "amenities": [
      "WIFI",
      "POOL",
      "RESTAURANT",
      "SPA"
    ],
    "rating": null,
    "reviewCount": null,
    "imageUrl": null,
    "description": null
  }
]
```

---

## 🏨 Hotels (Booking.com)

### Endpoint
```
GET /api/hotels/booking?destination=-553173&checkinDate=2025-01-15&checkoutDate=2025-01-20
```

### Réponse
```json
[
  {
    "hotelId": "123456",
    "name": "Hôtel Plaza Athénée",
    "address": "25 Avenue Montaigne, 8th arr., 75008 Paris",
    "city": "Paris",
    "country": "France",
    "latitude": 48.8662,
    "longitude": 2.3048,
    "stars": 5,
    "minPrice": 850.00,
    "currency": "EUR",
    "amenities": null,
    "rating": 9.2,
    "reviewCount": 1543,
    "imageUrl": "https://cf.bstatic.com/images/hotel/max1024x768/...",
    "description": null
  },
  {
    "hotelId": "789012",
    "name": "Shangri-La Hotel Paris",
    "address": "10 Avenue d'Iéna, 16th arr., 75116 Paris",
    "city": "Paris",
    "country": "France",
    "latitude": 48.8634,
    "longitude": 2.2934,
    "stars": 5,
    "minPrice": 920.00,
    "currency": "EUR",
    "amenities": null,
    "rating": 9.5,
    "reviewCount": 987,
    "imageUrl": "https://cf.bstatic.com/images/hotel/max1024x768/...",
    "description": null
  }
]
```

---

## ✈️ Flights (Vols)

### Endpoint
```
GET /api/flights/search?origin=CDG&destination=JFK&departureDate=2025-12-31&adults=1
```

### Réponse
```json
[
  {
    "flightNumber": "AF007",
    "airlineName": null,
    "airlineCode": "AF",
    "departureAirport": null,
    "departureAirportCode": "CDG",
    "arrivalAirport": null,
    "arrivalAirportCode": "JFK",
    "departureTime": "2025-12-31T10:30:00",
    "arrivalTime": "2025-12-31T13:45:00",
    "duration": "PT8H15M",
    "price": 542.50,
    "currency": "EUR",
    "flightStatus": null,
    "availableSeats": 7,
    "cabinClass": null
  },
  {
    "flightNumber": "AF065",
    "airlineName": null,
    "airlineCode": "AF",
    "departureAirport": null,
    "departureAirportCode": "CDG",
    "arrivalAirport": null,
    "arrivalAirportCode": "JFK",
    "departureTime": "2025-12-31T14:15:00",
    "arrivalTime": "2025-12-31T17:30:00",
    "duration": "PT8H15M",
    "price": 489.00,
    "currency": "EUR",
    "flightStatus": null,
    "availableSeats": 12,
    "cabinClass": null
  },
  {
    "flightNumber": "DL264",
    "airlineName": null,
    "airlineCode": "DL",
    "departureAirport": null,
    "departureAirportCode": "CDG",
    "arrivalAirport": null,
    "arrivalAirportCode": "JFK",
    "departureTime": "2025-12-31T18:45:00",
    "arrivalTime": "2025-12-31T22:00:00",
    "duration": "PT8H15M",
    "price": 625.75,
    "currency": "EUR",
    "flightStatus": null,
    "availableSeats": 5,
    "cabinClass": null
  }
]
```

---

## ⚠️ Erreur - API Externe Indisponible

### Réponse (503 Service Unavailable)
```json
{
  "timestamp": "2025-12-24T19:30:00",
  "status": 503,
  "error": "External API Error",
  "message": "Failed to search flights from Amadeus"
}
```

---

## ⚠️ Erreur - Paramètres Invalides

### Réponse (400 Bad Request)
```json
{
  "timestamp": "2025-12-24T19:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Invalid request to Amadeus API: Invalid city code"
}
```

---

## ⚠️ Erreur - Authentification Amadeus Échouée

### Réponse (503 Service Unavailable)
```json
{
  "timestamp": "2025-12-24T19:30:00",
  "status": 503,
  "error": "External API Error",
  "message": "Failed to authenticate with Amadeus API"
}
```

**Cause** : Clés API Amadeus invalides ou expirées.

---

## 📋 Notes sur les réponses

### Champs null
Certains champs peuvent être `null` car :
- L'API externe ne fournit pas toujours toutes les données
- Les plans gratuits ont des limitations
- Certaines données nécessitent des appels API supplémentaires

### Durée des vols
Format ISO 8601 : `PT8H15M` = 8 heures 15 minutes

### Dates
Format ISO 8601 : `2025-12-31T10:30:00`

### Tableaux vides
Si aucun résultat : `[]` (liste vide, pas d'erreur)

---

## 🔧 Personnalisation des réponses

Pour enrichir les réponses, vous pouvez :

1. **Appels API supplémentaires** dans les services
2. **Caching** pour réduire les appels
3. **Enrichissement des DTOs** avec des données calculées
4. **Filtrage côté backend** pour affiner les résultats

---

**💡 Ces exemples sont basés sur les structures réelles des APIs externes.**

Les données peuvent varier selon :
- La disponibilité des APIs
- Les limitations des plans gratuits
- La qualité des données fournies par les APIs

---

*Document mis à jour : 24 décembre 2025*

