import { inject, Injectable } from '@angular/core';
import {
  HttpRequest,
  HttpErrorResponse,
  HttpInterceptorFn,
  HttpHandlerFn,
  HttpEvent
} from '@angular/common/http';
import { EMPTY, Observable, catchError, switchMap, throwError } from 'rxjs';
import { Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

export const jwtInterceptor: HttpInterceptorFn = (req, next) => {
  const auth = inject(AuthService);
  const router = inject(Router);

  // Excluir endpoints de autenticación
  if (shouldSkipInterceptor(req)) {
    return next(req);
  }

  const token = auth.getToken();
  
 let authReq = req;

   if (token) { // Only add Authorization header if a token exists
    authReq = addTokenToRequest(req, token);
  }

  return next(authReq).pipe(
    catchError((error: HttpErrorResponse) => {
      // Manejar error 401 (No autorizado)
      // If it's a 401 AND not a refresh token request itself (to avoid infinite loop)
      if (error.status === 401 && !req.url.includes('auth/refresh')) {
        return handleUnauthorizedError(req, next, auth, router);
      }
      return throwError(() => error);
    })
  );
};

// --- Funciones auxiliares ---

function shouldSkipInterceptor(req: HttpRequest<unknown>): boolean {
  const skipUrls = ['/auth/login', '/auth/refresh', '/auth/revoke'];
  return skipUrls.some(url => req.url.includes(url)) || 
         req.headers.has('Skip-Interceptor');
}

function addTokenToRequest(
  req: HttpRequest<unknown>, 
  token: string
): HttpRequest<unknown> {
  return req.clone({
    setHeaders: {
      Authorization: `Bearer ${token}`
    }
  });
}


function handleUnauthorizedError(
  req: HttpRequest<unknown>,
  next: HttpHandlerFn,
  auth: AuthService,
  router: Router
): 


Observable<HttpEvent<unknown>> {
  // Intento de renovación silenciosa
  console.warn('JWTInterceptor: 401 Unauthorized. Attempting token refresh...'); // Add log
  return auth.refreshToken().pipe(
    switchMap(() => {
      const newToken = auth.getToken();
      if (!newToken) {
        console.error('JWTInterceptor: Token renewal failed, no new token. Logging out.'); // Add log
        throw new Error('Token renewal failed'); // Propagate error to trigger catchError below
      }
      console.log('JWTInterceptor: Token refreshed successfully. Retrying original request.'); // Add log
      return next(addTokenToRequest(req, newToken));
    }),
    catchError((refreshError) => {
      console.error('JWTInterceptor: Refresh token failed. Redirecting to login.', refreshError); // Add log
      auth.logout();
      router.navigate(['/login'], { 
        state: { sessionExpired: true } 
      });
      return EMPTY;
    })
  );
}