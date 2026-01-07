// Admin CRUD Models (matching backend entities)
export interface Flight {
    id?: string;
    airline: string;
    flightNumber: string;
    origin: string;
    destination: string;
    departureTime?: string;
    arrivalTime?: string;
    price?: number;
    seatsAvailable?: number;
    createdAt?: string;
    updatedAt?: string;
}

export interface Hotel {
    id?: string;
    name: string;
    address: string;
    city?: string;
    country?: string;
    etoile?: number;
    rooms?: Room[];
    createdAt?: string;
    updatedAt?: string;
}

export interface Room {
    id?: string;
    roomNumber: string;
    type?: string;
    price?: number;
    available?: boolean;
    hotelId?: string;
}

export interface Pays {
    id?: string;
    nom: string;
}

export interface Etape {
    id?: string;
    ordre: number;
    lieu: string;
    description?: string;
    dureeJours: number;
    hotelId?: string;
    hotel?: Hotel;
}

export interface Circuit {
    id?: string;
    title: string;
    description?: string;
    duree?: number;
    prix?: number;
    imageUrl?: string;
    difficulty?: 'EASY' | 'MODERATE' | 'DIFFICULT';
    maxParticipants?: number;
    etapes?: Etape[];
    hotels?: Hotel[];
    activities?: Activity[];
    pays?: Pays[];
    createdAt?: string;
    updatedAt?: string;
}

export interface Activity {
    id?: string;
    name: string;
    description?: string;
    price?: number;
}

export interface User {
    id?: string;
    username: string;
    email: string;
    firstName: string;
    lastName: string;
    role?: 'ROLE_ADMIN' | 'ROLE_USER';
}

// External API DTOs (matching backend external API responses)
export interface FlightDTO {
    flightNumber?: string;
    airlineName?: string;
    airlineCode?: string;
    departureAirport?: string;
    departureAirportCode?: string;
    arrivalAirport?: string;
    arrivalAirportCode?: string;
    departureTime?: string;
    arrivalTime?: string;
    duration?: string;
    price?: number;
    currency?: string;
    availableSeats?: number;
    cabinClass?: string;
}

export interface HotelDTO {
    hotelId?: string;
    name?: string;
    address?: string;
    city?: string;
    country?: string;
    latitude?: number;
    longitude?: number;
    stars?: number;
    minPrice?: number;
    currency?: string;
    rating?: number;
    reviewCount?: number;
    imageUrl?: string;
    amenities?: string[];
}

export interface AirlineDTO {
    airlineCode?: string;
    airlineName?: string;
    iataCode?: string;
    icaoCode?: string;
    country?: string;
    callsign?: string;
    active?: boolean;
}

export interface AirportDTO {
    airportCode?: string;
    airportName?: string;
    city?: string;
    country?: string;
    countryCode?: string;
    latitude?: number;
    longitude?: number;
    timezone?: string;
    iataCode?: string;
    icaoCode?: string;
}
