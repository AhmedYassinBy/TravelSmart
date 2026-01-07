// src/app/services/airline.service.ts
import { Injectable } from '@angular/core';
import { HttpService } from '../core/http.service';
import { Observable } from 'rxjs';
import { AirlineDTO } from '../models';

@Injectable({ providedIn: 'root' })
export class AirlineService {
    constructor(private http: HttpService) { }

    /**
     * Search airlines from AviationStack API
     * GET /api/airlines/search?airlineName=X
     */
    search(airlineName?: string): Observable<AirlineDTO[]> {
        const queryString = airlineName ? `?airlineName=${encodeURIComponent(airlineName)}` : '';
        return this.http.get<AirlineDTO[]>(`/api/airlines/search${queryString}`);
    }
}
