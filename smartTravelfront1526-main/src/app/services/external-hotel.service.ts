// src/app/services/external-hotel.service.ts
import { Injectable } from '@angular/core';
import { HttpService } from '../core/http.service';
import { Observable } from 'rxjs';
import { HotelDTO } from '../models';

export interface AmadeusHotelSearchParams {
    cityCode: string;
}

export interface BookingHotelSearchParams {
    destination: string;
    checkinDate: string;
    checkoutDate: string;
}

@Injectable({ providedIn: 'root' })
export class ExternalHotelService {
    constructor(private http: HttpService) { }

    /**
     * Search hotels from Amadeus API by city code
     * GET /api/hotels/search?cityCode=PAR
     */
    searchByCity(cityCode: string): Observable<HotelDTO[]> {
        return this.http.get<HotelDTO[]>(`/api/hotels/search?cityCode=${cityCode}`);
    }

    /**
     * Search hotels from Booking.com API
     * GET /api/hotels/booking?destination=X&checkinDate=Y&checkoutDate=Z
     */
    searchViaBooking(params: BookingHotelSearchParams): Observable<HotelDTO[]> {
        const queryParams = new URLSearchParams({
            destination: params.destination,
            checkinDate: params.checkinDate,
            checkoutDate: params.checkoutDate
        });
        return this.http.get<HotelDTO[]>(`/api/hotels/booking?${queryParams.toString()}`);
    }
}
