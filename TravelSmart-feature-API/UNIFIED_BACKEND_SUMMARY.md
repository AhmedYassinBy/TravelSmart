# Unified Backend - Implementation Summary

## Overview
This document summarizes the unification of **TravelSmart-feature-API** and **voyageAppBackend-master** into a single, production-ready backend that supports both the Admin Web Application (Angular) and the Mobile Application (Android/Kotlin).

## Decision: Base Backend
**TravelSmart-feature-API** was chosen as the base because:
- Better architecture and code quality
- Production-ready configurations (environment variables, CORS)
- External API integrations (Amadeus, AviationStack, Booking.com)
- Already compatible with the Admin Web Application

## Files Created

### 1. Enums
| File | Purpose |
|------|---------|
| `enums/ReservationStatus.java` | PENDING, CONFIRMED, CANCELLED, COMPLETED, REFUNDED |
| `enums/PaymentStatus.java` | PENDING, COMPLETED, FAILED, REFUNDED, CANCELLED |
| `enums/PaymentMethod.java` | CARD, PAYPAL, WALLET, CASH, BANK_TRANSFER |

### 2. New Entities
| File | Purpose |
|------|---------|
| `entities/Client.java` | Mobile app user (separate from Admin User) |
| `entities/Reservation.java` | Unified booking for hotels, flights, circuits |
| `entities/Payment.java` | Payment tracking |
| `entities/CircuitDay.java` | Daily program for circuits |
| `entities/CircuitActivity.java` | Optional activities with pricing |
| `entities/Pays.java` | Country entity for circuit destinations |

### 3. Updated Entities
| File | Changes |
|------|---------|
| `entities/Hotel.java` | Added: pricePerNight, description, imageUrl, amenities, isActive, latitude, longitude, phone, email, website, checkInTime, checkOutTime, cancellationPolicy |
| `entities/Room.java` | Added: roomType, maxOccupancy, description, isAvailable, viewType, bedType, sizeSqm |
| `entities/Flight.java` | Added: classType, isActive |
| `entities/Circuit.java` | Added: destinations, type, included, highlights, isActive, program (CircuitDay), circuitActivities (CircuitActivity), pays (Pays) |
| `entities/Etape.java` | Updated: JsonBackReference to "circuit-etapes" |

### 4. Repositories
| File | Purpose |
|------|---------|
| `repositories/ClientRepository.java` | Client CRUD with email lookup |
| `repositories/ReservationRepository.java` | Reservation CRUD with client filtering |
| `repositories/PaymentRepository.java` | Payment CRUD with reservation lookup |
| `repositories/CircuitDayRepository.java` | CircuitDay by circuit ID |
| `repositories/CircuitActivityRepository.java` | CircuitActivity by circuit ID |
| `repositories/PaysRepository.java` | Country CRUD |

### 5. Updated Repositories
| File | New Methods |
|------|-------------|
| `HotelRepository.java` | findByIsActiveTrue(), findByCityAndIsActiveTrue(), findByEtoile() |
| `FlightRepository.java` | findByIsActiveTrue(), findByOrigin(), findByDestination() |
| `CircuitRepository.java` | findByIsActiveTrue(), findByType(), findByDuree() |
| `RoomRepository.java` | findByHotelIdAndIsAvailableTrue(), findByHotelIdAndRoomType() |

### 6. DTOs
| File | Purpose |
|------|---------|
| `dto/RegisterClientRequest.java` | Mobile registration request |
| `dto/UpdateProfileRequest.java` | Mobile profile update request |

### 7. New Services
| File | Methods |
|------|---------|
| `services/EmailService.java` | sendEmail(), sendConfirmationEmail(), sendPasswordResetEmail(), sendBookingConfirmation() |
| `services/ClientService.java` | register(), confirmAccount(), forgotPassword(), resetPassword(), login(), getProfile(), updateProfile(), changePassword() |
| `services/ReservationService.java` | getReservationsByEmail(), getReservationDetails(), createReservation(), cancelReservation() |

### 8. Updated Services
| File | New Methods |
|------|-------------|
| `services/CircuitService.java` | getAllActiveCircuits(), getCircuitById(), searchCircuits(), getCircuitProgram(), getCircuitActivities(), calculatePrice() |
| `services/HotelService.java` | getAllActiveHotels(), getHotelById(), getHotelsByCity(), getHotelsByStars(), searchHotels() |
| `services/FlightService.java` | getAllActiveFlights(), getFlightById(), searchFlights() |
| `services/RoomService.java` | getRoomsByHotelId(), getAllRoomsByHotelId(), getRoomById(), createRoom(), updateRoom(), deleteRoom() |

### 9. New Controllers
| File | Endpoints |
|------|-----------|
| `controllers/ClientController.java` | `/api/client/*` - Mobile user authentication and profile |
| `controllers/ReservationController.java` | `/api/reservation/*` - Booking management |
| `controllers/RoomController.java` | `/api/rooms/*` - Room management |
| `controllers/CircuitController.java` | `/api/circuits/*` - Mobile circuit browsing |
| `controllers/MobileHotelController.java` | `/api/mobile/hotels/*` - Mobile hotel browsing |
| `controllers/MobileFlightController.java` | `/api/mobile/flights/*` - Mobile flight browsing |

## API Endpoints Summary

### Public Endpoints (No Auth Required)
```
# Client Authentication
POST   /api/client/register          - Register new mobile user
GET    /api/client/confirm           - Confirm email account
POST   /api/client/login             - Login mobile user
POST   /api/client/forgot-password   - Request password reset
POST   /api/client/reset-password    - Reset password with token

# Hotels (Local Database)
GET    /api/mobile/hotels            - Get all active hotels
GET    /api/mobile/hotels/{id}       - Get hotel by ID
GET    /api/mobile/hotels/city/{city} - Get hotels by city
GET    /api/mobile/hotels/stars/{n}  - Get hotels by star rating
GET    /api/mobile/hotels/search     - Search hotels with filters

# Flights (Local Database)
GET    /api/mobile/flights           - Get all active flights
GET    /api/mobile/flights/{id}      - Get flight by ID
GET    /api/mobile/flights/search    - Search flights with filters

# Circuits
GET    /api/circuits                 - Get all active circuits
GET    /api/circuits/{id}            - Get circuit by ID
GET    /api/circuits/search          - Search circuits with filters
GET    /api/circuits/{id}/program    - Get circuit daily program
GET    /api/circuits/{id}/activities - Get circuit optional activities
POST   /api/circuits/{id}/calculate-price - Calculate circuit price

# Rooms
GET    /api/rooms/hotel/{hotelId}    - Get available rooms for hotel

# External APIs (Existing)
GET    /api/hotels/search            - Search external hotels (Amadeus)
GET    /api/hotels/booking           - Search hotels (Booking.com)
GET    /api/flights/search           - Search external flights (Amadeus)
GET    /api/airlines/search          - Search airlines (AviationStack)
GET    /api/airports/search          - Search airports (AviationStack)
```

### Authenticated Endpoints (JWT Required)
```
# Client Profile
GET    /api/client/profile           - Get user profile
PUT    /api/client/profile           - Update user profile
PUT    /api/client/change-password   - Change password

# Reservations
GET    /api/reservation              - Get user's reservations
GET    /api/reservation/{id}         - Get reservation details
POST   /api/reservation              - Create new reservation
DELETE /api/reservation/{id}         - Cancel reservation

# Rooms (Admin)
GET    /api/rooms/hotel/{hotelId}/all - Get all rooms for hotel
GET    /api/rooms/{id}               - Get room by ID
POST   /api/rooms                    - Create new room
PUT    /api/rooms/{id}               - Update room
DELETE /api/rooms/{id}               - Delete room
```

### Admin Endpoints (JWT + Role Required)
```
# All existing admin endpoints remain unchanged
/api/admin/*
/api/users/*
/api/hotels/*  (CRUD operations)
/api/flights/* (CRUD operations)
/api/circuits/* (CRUD operations via admin controllers)
```

## Configuration Updates

### SecurityConfig.java
Added permissions for all new mobile endpoints:
- `/api/client/register`, `/api/client/login`, etc. - permitAll()
- `/api/mobile/hotels/**`, `/api/mobile/flights/**` - permitAll()
- `/api/circuits/**` - permitAll()
- `/api/rooms/**` - permitAll()
- `/api/client/profile/**`, `/api/reservation/**` - authenticated()

### application.properties
Added:
```properties
# Mobile app backend URL for confirmation links
app.mobile.base-url=${APP_MOBILE_BASE_URL:http://localhost:8085}
```

## Database Schema Changes
The following tables will be created/updated when the application runs:
- `client` - New table for mobile users
- `reservation` - New table for bookings
- `payment` - New table for payment tracking
- `circuit_day` - New table for circuit daily programs
- `circuit_activity` - New table for optional activities
- `pays` - New table for countries
- `hotel` - New columns added
- `room` - New columns added
- `flight` - New columns added
- `circuit` - New columns and relationships added

## Running the Unified Backend

```bash
cd TravelSmart-feature-API
./mvnw spring-boot:run
```

The server will start on port 8085.

## Testing

### Test Mobile Registration
```bash
curl -X POST http://localhost:8085/api/client/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "password123",
    "firstName": "John",
    "lastName": "Doe",
    "telephone": "+1234567890"
  }'
```

### Test Get Circuits
```bash
curl http://localhost:8085/api/circuits
```

### Test Search Hotels
```bash
curl "http://localhost:8085/api/mobile/hotels/search?city=Paris&minPrice=50&maxPrice=200"
```

## Backward Compatibility
- ✅ All Admin Web Application endpoints remain unchanged
- ✅ All Admin authentication (JWT) remains unchanged
- ✅ All existing entities maintain their structure
- ✅ Database schema is additive only (no breaking changes)
- ✅ External API integrations (Amadeus, AviationStack, Booking.com) remain unchanged
