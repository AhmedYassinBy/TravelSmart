// src/app/services/airport.service.ts
import { Injectable } from '@angular/core';
import { HttpService } from '../core/http.service';
import { Observable } from 'rxjs';
import { AirportDTO } from '../models';

export interface AirportSearchParams {
    airportName?: string;
    country?: string;
}

@Injectable({ providedIn: 'root' })
export class AirportService {
    constructor(private http: HttpService) { }

    /**
     * Search airports from AviationStack API
     * GET /api/airports/search?airportName=X&country=Y
     */
    search(params: AirportSearchParams = {}): Observable<AirportDTO[]> {
        const queryParams = new URLSearchParams();
        if (params.airportName) queryParams.set('airportName', params.airportName);
        if (params.country) queryParams.set('country', params.country);

        const queryString = queryParams.toString();
        return this.http.get<AirportDTO[]>(`/api/airports/search${queryString ? '?' + queryString : ''}`);
    }
}
