# 🌍 TravelSmart - Plateforme d'Agence de Voyage Intelligente

## 📖 Description

**TravelSmart** est un backend Spring Boot 3 professionnel qui consomme des **APIs de voyage RÉELLES** pour fournir des données en temps réel :

- ✈️ **Vols** - Via Amadeus API
- 🏨 **Hôtels** - Via Amadeus & Booking.com API
- 🛫 **Aéroports** - Via AviationStack API
- 🛩️ **Compagnies aériennes** - Via AviationStack API

**⚡ ZÉRO donnée statique ou fictive - 100% données réelles provenant d'APIs externes**

---

## 🎯 Caractéristiques principales

✅ **Architecture propre** : Controller → Service → Client API → API Externe  
✅ **WebClient réactif** avec timeouts et gestion d'erreurs  
✅ **Mapping JSON automatique** vers DTOs  
✅ **Authentification JWT** pour les endpoints privés  
✅ **Endpoints publics** pour les APIs de voyage  
✅ **Gestion globale des erreurs**  
✅ **Logs détaillés** pour le debugging  
✅ **Code professionnel** et maintenable  

---

## 🚀 Démarrage rapide

### Prérequis

- Java 21+
- Maven 3.8+
- PostgreSQL
- Clés API (gratuites) : Amadeus, AviationStack, RapidAPI

### Installation

1. **Cloner le projet**
```bash
git clone <votre-repo>
cd TravelSmart1
```

2. **Configurer la base de données**
```sql
CREATE DATABASE TravelSmart1;
```

3. **Obtenir vos clés API** (voir QUICK_START.md)

4. **Configurer application.properties**
```properties
# Remplacer par vos vraies clés
amadeus.api.key=VOTRE_CLE
amadeus.api.secret=VOTRE_SECRET
aviationstack.api.key=VOTRE_CLE
rapidapi.booking.key=VOTRE_CLE
```

5. **Lancer l'application**
```bash
mvn spring-boot:run
```

6. **Tester**
```bash
curl "http://localhost:8085/api/airlines/search?airlineName=Emirates"
```

---

## 📚 Documentation

- 📘 **[Guide de démarrage rapide](QUICK_START.md)** - Pour commencer en 5 minutes
- 📗 **[Documentation complète des APIs](API_DOCUMENTATION.md)** - Tous les endpoints et exemples
- 📦 **[Collection Postman](TravelSmart_External_APIs.postman_collection.json)** - Importer dans Postman pour tester

---

## 🔗 Endpoints disponibles

| Endpoint | Description | Exemple |
|----------|-------------|---------|
| `GET /api/airlines/search` | Rechercher compagnies aériennes | `?airlineName=Emirates` |
| `GET /api/airports/search` | Rechercher aéroports | `?country=France` |
| `GET /api/hotels/search` | Rechercher hôtels (Amadeus) | `?cityCode=PAR` |
| `GET /api/hotels/booking` | Rechercher hôtels (Booking) | `?destination=-553173&...` |
| `GET /api/flights/search` | Rechercher vols | `?origin=CDG&destination=JFK&...` |

**Tous ces endpoints sont publics** (pas de token requis)

---

## 🏗️ Architecture technique

```
┌─────────────┐
│   Angular   │  Frontend (à développer)
│  Frontend   │
└──────┬──────┘
       │ HTTP REST
       ▼
┌─────────────────────────────────────────┐
│       Spring Boot Backend (Port 8085)    │
├─────────────────────────────────────────┤
│  Controllers (REST Endpoints)            │
│    ├─── HotelController                 │
│    ├─── FlightController                │
│    ├─── AirlineController               │
│    └─── AirportController               │
├─────────────────────────────────────────┤
│  Services (Business Logic)              │
│    ├─── HotelService                    │
│    ├─── FlightService                   │
│    ├─── AirlineService                  │
│    └─── AirportService                  │
├─────────────────────────────────────────┤
│  API Clients (WebClient)                │
│    ├─── AmadeusApiClient                │
│    ├─── AviationStackApiClient          │
│    └─── BookingApiClient                │
└─────────────────────────────────────────┘
       │ HTTPS
       ▼
┌─────────────────────────────────────────┐
│          APIs Externes                   │
│  ┌──────────────────────────────────┐  │
│  │  Amadeus API                      │  │
│  │  - Vols                           │  │
│  │  - Hôtels                         │  │
│  └──────────────────────────────────┘  │
│  ┌──────────────────────────────────┐  │
│  │  AviationStack API                │  │
│  │  - Compagnies aériennes           │  │
│  │  - Aéroports                      │  │
│  └──────────────────────────────────┘  │
│  ┌──────────────────────────────────┐  │
│  │  Booking.com API (RapidAPI)       │  │
│  │  - Hôtels                         │  │
│  └──────────────────────────────────┘  │
└─────────────────────────────────────────┘
```

---

## 🛠️ Technologies

- **Spring Boot 3.4.7** - Framework principal
- **Spring WebFlux** - WebClient pour appels API asynchrones
- **Spring Security** - Authentification JWT
- **Spring Data JPA** - ORM
- **PostgreSQL** - Base de données
- **Lombok** - Réduction du boilerplate
- **Jackson** - Mapping JSON
- **Reactor Netty** - Client HTTP réactif
- **Maven** - Gestion des dépendances

---

## 📦 Structure du projet

```
TravelSmart1/
├── src/main/java/com/ahmedyassin/TravelSmart/
│   ├── clients/              # Clients API externes
│   ├── config/               # Configurations (WebClient, Security)
│   ├── controllers/
│   │   ├── external/         # Controllers pour APIs externes
│   │   └── ...               # Autres controllers (Auth, User, etc.)
│   ├── dto/
│   │   ├── external/         # DTOs pour APIs externes
│   │   └── ...               # Autres DTOs
│   ├── entities/             # Entités JPA
│   ├── services/
│   │   ├── external/         # Services pour APIs externes
│   │   └── ...               # Autres services
│   ├── repositories/         # Repositories JPA
│   ├── exceptions/           # Gestion des exceptions
│   └── Security/             # Configuration sécurité JWT
├── src/main/resources/
│   └── application.properties  # Configuration
├── API_DOCUMENTATION.md      # Documentation complète
├── QUICK_START.md            # Guide de démarrage
├── TravelSmart_External_APIs.postman_collection.json
└── pom.xml
```

---

## 🔐 Sécurité

- Les **clés API** ne sont jamais exposées au frontend
- Endpoints des APIs externes configurés en **public** (pas de JWT requis)
- Autres endpoints protégés par **JWT Authentication**
- Configuration CORS activée pour le frontend

---

## 📊 Limites des APIs gratuites

| API | Plan gratuit | Recommandation |
|-----|--------------|----------------|
| **Amadeus** | 1000 req/mois | Utiliser pour vols et hôtels |
| **AviationStack** | 100 req/mois | Utiliser pour métadonnées (airlines, airports) |
| **Booking** | 500 req/mois | Utiliser avec parcimonie |

💡 **Conseil** : Implémenter un cache Redis pour optimiser

---

## ✅ TODO / Améliorations futures

- [ ] Cache Redis pour réduire les appels API
- [ ] Rate limiting côté backend
- [ ] Tests unitaires (MockWebServer)
- [ ] Tests d'intégration
- [ ] Swagger/OpenAPI documentation
- [ ] Pagination des résultats
- [ ] Filtres avancés (prix, dates, étoiles, etc.)
- [ ] Webhooks pour mises à jour en temps réel
- [ ] Support multi-langue

---

## 🐛 Débogage

### Logs
Les logs détaillés sont activés. Vérifier la console pour :
- Requêtes API externes
- Erreurs d'authentification
- Timeouts
- Erreurs de mapping JSON

### Problèmes courants

**1. "Failed to authenticate with Amadeus"**
→ Vérifier vos credentials Amadeus

**2. "Connection timeout"**
→ Augmenter les timeouts dans application.properties

**3. "No data found"**
→ Vérifier que les paramètres (codes IATA, dates) sont valides

**4. API quota exceeded**
→ Vous avez dépassé votre limite gratuite, attendre le mois prochain

---

## 📞 Support & Contact

Pour toute question :
1. Consulter la [documentation complète](API_DOCUMENTATION.md)
2. Vérifier les logs dans la console
3. Tester les APIs externes directement
4. Vérifier les quotas restants

---

## 👨‍💻 Auteur

**Ahmed Yassin**  
Projet : Backend TravelSmart  
Date : Décembre 2025

---

## 📄 Licence

Projet privé à usage éducatif.

---

## 🎉 Remerciements

- Amadeus for Developers
- AviationStack
- RapidAPI / Booking.com API
- Spring Boot Team

---

**🚀 Bon développement avec TravelSmart !**

