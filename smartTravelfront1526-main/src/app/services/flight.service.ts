// src/app/services/flight.service.ts
import { Injectable } from '@angular/core';
import { CrudService } from '../core/crud.service';
import { HttpService } from '../core/http.service';
import { Flight } from '../models';

@Injectable({ providedIn: 'root' })
export class FlightService extends CrudService<Flight> {
  constructor(http: HttpService) { super(http, '/admin/offers/flights'); }
}