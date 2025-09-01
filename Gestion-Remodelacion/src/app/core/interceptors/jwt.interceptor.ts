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
import { MatSnackBar } from '@angular/material/snack-bar';

export const jwtInterceptor: HttpInterceptorFn = (req, next) => {
const authService = inject(AuthService);
  const router = inject(Router);
  const snackBar = inject(MatSnackBar);

  // Excluir endpoints que no necesitan token
  if (shouldSkipAuth(req)) {
    return next(req);
  }

  const token = authService.getToken();
  let authReq = req;

  if (token) {
    authReq = addTokenToRequest(req, token);
  }

  return next(authReq).pipe(
    catchError((error: HttpErrorResponse) => {
      // Si el error es 401, intentamos refrescar el token
      if (error.status === 401 && !req.url.includes('auth/refresh')) {
        return handle401Error(authReq, next, authService, router, snackBar);
      }

      return throwError(() => error);
    })
  );
};

// --- Funciones Auxiliares ---

function shouldSkipAuth(req: HttpRequest<unknown>): boolean {
  const skipUrls = ['/auth/login', '/auth/refresh', '/auth/revoke'];
  return skipUrls.some((url) => req.url.includes(url));
}

function addTokenToRequest(req: HttpRequest<unknown>, token: string): HttpRequest<unknown> {
  return req.clone({
    setHeaders: {
      Authorization: `Bearer ${token}`,
    },
  });
}

function handle401Error(
  req: HttpRequest<unknown>,
  next: HttpHandlerFn,
  authService: AuthService,
  router: Router,
  snackBar: MatSnackBar
): Observable<HttpEvent<unknown>> {
  
  return authService.refreshToken().pipe(
    switchMap(() => {
      const newToken = authService.getToken();
      if (!newToken) {
        // Si el refresh no devuelve un token, la sesión es inválida
        return logoutAndRedirect(authService, router, snackBar);
      }
      // Si el refresh es exitoso, reintentamos la petición original con el nuevo token
      return next(addTokenToRequest(req, newToken));
    }),
    catchError((refreshError) => {
      // Si el flujo de refresh falla (p. ej. el refresh token expiró o el reintento falló),
      // entonces sí cerramos la sesión.
      return logoutAndRedirect(authService, router, snackBar);
    })
  );
}

function logoutAndRedirect(authService: AuthService, router: Router, snackBar: MatSnackBar): Observable<HttpEvent<any>> {
    authService.logout();
    router.navigate(['/auth/login'], {
      state: { sessionExpired: true },
    });
    snackBar.open('Tu sesión ha expirado. Por favor, inicia sesión de nuevo.', 'Cerrar', {
      duration: 5000,
      panelClass: ['error-snackbar'],
    });
    return EMPTY; // Detiene la cadena de observables
}