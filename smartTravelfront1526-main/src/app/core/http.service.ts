// src/app/core/http.service.ts
import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class HttpService {
  private readonly api = 'http://localhost:8085';

  constructor(private http: HttpClient) {}

  get<T>(url: string): Observable<T> {
    return this.http.get<T>(`${this.api}${url}`);
  }
  post<T>(url: string, body: any): Observable<T> {
    return this.http.post<T>(`${this.api}${url}`, body);
  }
  put<T>(url: string, body: any): Observable<T> {
    return this.http.put<T>(`${this.api}${url}`, body);
  }
  delete(url: string): Observable<void> {
    return this.http.delete<void>(`${this.api}${url}`);
  }
}