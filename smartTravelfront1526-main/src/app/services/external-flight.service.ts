// src/app/services/external-flight.service.ts
import { Injectable } from '@angular/core';
import { HttpService } from '../core/http.service';
import { Observable } from 'rxjs';
import { FlightDTO } from '../models';

export interface FlightSearchParams {
    origin: string;
    destination: string;
    departureDate: string;
    adults?: number;
}

@Injectable({ providedIn: 'root' })
export class ExternalFlightService {
    constructor(private http: HttpService) { }

    /**
     * Search flights from Amadeus API
     * GET /api/flights/search
     */
    searchFlights(params: FlightSearchParams): Observable<FlightDTO[]> {
        const queryParams = new URLSearchParams({
            origin: params.origin,
            destination: params.destination,
            departureDate: params.departureDate,
            adults: (params.adults || 1).toString()
        });
        return this.http.get<FlightDTO[]>(`/api/flights/search?${queryParams.toString()}`);
    }
}
