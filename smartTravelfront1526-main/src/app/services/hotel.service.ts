// src/app/services/hotel.service.ts
import { Injectable } from '@angular/core';
import { CrudService } from '../core/crud.service';
import { HttpService } from '../core/http.service';
import { Hotel, Room } from '../models';

@Injectable({ providedIn: 'root' })
export class HotelService extends CrudService<Hotel> {
  constructor(http: HttpService) { super(http, '/admin/offers/hotels'); }

  addRoom(hotelId: string, room: Room) {
    return this.http.post<Room>(`/admin/offers/hotels/${hotelId}/rooms`, room);
  }
  getRooms(hotelId: string) {
    return this.http.get<Room[]>(`/admin/offers/hotels/${hotelId}/rooms`);
  }
}