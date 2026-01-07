// src/app/core/auth.interceptor.ts
import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';

/**
 * HTTP Interceptor that:
 * 1. Adds JWT token to all outgoing requests
 * 2. Handles 401 responses by clearing session and redirecting to login
 */
export const authInterceptor: HttpInterceptorFn = (req, next) => {
    const router = inject(Router);
    const token = localStorage.getItem('token');

    // Clone request with auth header if token exists
    const authReq = token
        ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } })
        : req;

    return next(authReq).pipe(
        catchError((error: HttpErrorResponse) => {
            // Handle 401 Unauthorized - token expired or invalid
            if (error.status === 401 && !req.url.includes('/api/auth/login')) {
                // Clear all auth data
                localStorage.removeItem('token');
                localStorage.removeItem('username');
                localStorage.removeItem('role');

                // Redirect to login
                router.navigate(['/log']);
            }
            return throwError(() => error);
        })
    );
};

