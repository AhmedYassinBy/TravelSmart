import { Injectable } from '@angular/core';
import { HttpService } from './http.service';
import { Observable } from 'rxjs';

@Injectable()
export abstract class CrudService<T, ID = string> {
  constructor(protected http: HttpService, protected base: string) {}

  create(entity: T): Observable<T> { return this.http.post<T>(this.base, entity); }
  findAll(): Observable<T[]> { return this.http.get<T[]>(this.base); }
  findOne(id: ID): Observable<T> { return this.http.get<T>(`${this.base}/${id}`); }
  update(id: ID, entity: Partial<T>): Observable<T> { return this.http.put<T>(`${this.base}/${id}`, entity); }
  delete(id: ID): Observable<void> { return this.http.delete(`${this.base}/${id}`); }
}