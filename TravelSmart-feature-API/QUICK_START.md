# 🚀 Guide de Démarrage Rapide - TravelSmart API

## ⚡ Configuration en 3 étapes

### Étape 1 : Obtenir vos clés API

#### Amadeus (GRATUIT - 1000 requêtes/mois)
1. Aller sur https://developers.amadeus.com/register
2. Créer un compte et une application
3. Copier votre **API Key** et **API Secret**

#### AviationStack (GRATUIT - 100 requêtes/mois)
1. Aller sur https://aviationstack.com/product
2. Créer un compte gratuit
3. Copier votre **Access Key**

#### RapidAPI Booking (GRATUIT - 500 requêtes/mois)
1. Aller sur https://rapidapi.com/apidojo/api/booking-com
2. S'inscrire et s'abonner au plan gratuit
3. Copier votre **RapidAPI Key**

---

### Étape 2 : Configurer le projet

Éditer le fichier : `src/main/resources/application.properties`

Remplacer les valeurs suivantes :

```properties
# Amadeus API
amadeus.api.key=VOTRE_CLE_AMADEUS_ICI
amadeus.api.secret=VOTRE_SECRET_AMADEUS_ICI

# AviationStack API
aviationstack.api.key=VOTRE_CLE_AVIATIONSTACK_ICI

# Booking.com API via RapidAPI
rapidapi.booking.key=VOTRE_CLE_RAPIDAPI_ICI
```

---

### Étape 3 : Lancer l'application

```bash
# Dans le terminal, à la racine du projet :
mvn spring-boot:run
```

Attendez le message : `Started TravelSmartApplication in X seconds`

L'API est maintenant accessible sur : **http://localhost:8085**

---

## 🧪 Tester immédiatement

### Test 1 : Rechercher des compagnies aériennes

```bash
curl "http://localhost:8085/api/airlines/search?airlineName=Emirates"
```

**Pourquoi ce test ?** Il ne nécessite que AviationStack et est rapide à répondre.

### Test 2 : Rechercher des aéroports en France

```bash
curl "http://localhost:8085/api/airports/search?country=France"
```

### Test 3 : Rechercher des hôtels à Paris (Amadeus)

```bash
curl "http://localhost:8085/api/hotels/search?cityCode=PAR"
```

### Test 4 : Rechercher des vols Paris → New York

```bash
curl "http://localhost:8085/api/flights/search?origin=CDG&destination=JFK&departureDate=2025-12-31&adults=1"
```

---

## 📋 Endpoints disponibles

| Endpoint | Méthode | Description | Exemple |
|----------|---------|-------------|---------|
| `/api/airlines/search` | GET | Chercher compagnies | `?airlineName=Air France` |
| `/api/airports/search` | GET | Chercher aéroports | `?country=France` |
| `/api/hotels/search` | GET | Chercher hôtels (Amadeus) | `?cityCode=PAR` |
| `/api/hotels/booking` | GET | Chercher hôtels (Booking) | `?destination=-553173&checkinDate=2025-01-15&checkoutDate=2025-01-20` |
| `/api/flights/search` | GET | Chercher vols | `?origin=CDG&destination=JFK&departureDate=2025-12-31&adults=1` |

---

## 🔍 Codes IATA courants

### Villes (pour hôtels)
- PAR = Paris
- LON = Londres  
- NYC = New York
- DXB = Dubaï
- TYO = Tokyo
- ROM = Rome
- BCN = Barcelone

### Aéroports (pour vols)
- CDG = Paris Charles de Gaulle
- ORY = Paris Orly
- LHR = London Heathrow
- JFK = New York JFK
- DXB = Dubai International
- NRT = Tokyo Narita
- FCO = Rome Fiumicino

---

## ❗ Résolution de problèmes

### Erreur : "Failed to authenticate with Amadeus API"
➡️ Vérifier que `amadeus.api.key` et `amadeus.api.secret` sont corrects

### Erreur : "AviationStack API server error"
➡️ Vérifier que `aviationstack.api.key` est correct

### Erreur : "Booking API 4xx error"
➡️ Vérifier que `rapidapi.booking.key` est correct

### L'application ne démarre pas
➡️ Vérifier que PostgreSQL est lancé et que la base `TravelSmart1` existe

### Erreur 401 Unauthorized sur un endpoint
➡️ Les endpoints `/api/hotels/**`, `/api/flights/**`, `/api/airlines/**`, `/api/airports/**` sont publics
➡️ Pas besoin de token Bearer

---

## 📊 Limites des plans gratuits

| API | Limite gratuite | Conseil |
|-----|-----------------|---------|
| Amadeus | 1000 req/mois | Utiliser pour les vols principalement |
| AviationStack | 100 req/mois | Utiliser pour les métadonnées (airlines, airports) |
| Booking via RapidAPI | 500 req/mois | Utiliser avec parcimonie |

**💡 Astuce :** Implémentez un cache Redis pour réduire les appels API répétitifs !

---

## 🎯 Prochaines étapes

1. **Tester tous les endpoints** avec Postman ou curl
2. **Connecter votre frontend Angular** à l'API
3. **Implémenter un cache** pour optimiser
4. **Ajouter des filtres** supplémentaires (prix, date, etc.)
5. **Créer une interface** pour les administrateurs

---

## 📞 Support

- Documentation complète : `API_DOCUMENTATION.md`
- En cas de problème, vérifier les logs dans la console
- Tester les APIs directement sur leur site pour valider les clés

---

**✅ Vous êtes prêt ! Bon développement !**

