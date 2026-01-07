import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { catchError, map } from 'rxjs/operators';

export interface ChatMessage {
  role: 'user' | 'assistant';
  content: string;
}

export interface ChatRequest {
  message: string;
  history: ChatMessage[];
}

export interface ChatResponse {
  message: string;
  success: boolean;
  error?: string;
}

@Injectable({
  providedIn: 'root'
})
export class ChatbotService {
  private readonly apiUrl = 'http://localhost:8085/api/chatbot';

  constructor(private http: HttpClient) {}

  sendMessage(message: string, history: ChatMessage[]): Observable<ChatResponse> {
    const request: ChatRequest = {
      message,
      history
    };

    return this.http.post<ChatResponse>(`${this.apiUrl}/chat`, request).pipe(
      catchError(error => {
        console.error('Chatbot API error:', error);
        return of({
          message: '',
          success: false,
          error: 'Unable to connect to the assistant. Please try again later.'
        });
      })
    );
  }

  checkHealth(): Observable<boolean> {
    return this.http.get(`${this.apiUrl}/health`, { responseType: 'text' }).pipe(
      map(() => true),
      catchError(() => of(false))
    );
  }
}
