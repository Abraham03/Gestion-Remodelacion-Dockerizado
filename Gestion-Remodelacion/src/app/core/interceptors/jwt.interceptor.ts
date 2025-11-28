import { inject, Injectable } from '@angular/core';
import {
  HttpRequest,
  HttpErrorResponse,
  HttpInterceptorFn,
  HttpHandlerFn,
  HttpEvent
} from '@angular/common/http';
import { BehaviorSubject, EMPTY, Observable, catchError, filter, finalize, switchMap, take, throwError } from 'rxjs';
import { Router } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { MatSnackBar } from '@angular/material/snack-bar';
import { User } from '../models/user.model';


// Variables de Estado
// Se declaran fuera de la funcion para que actuen como singleton
let isRefreshingToken = false;
const refreshTokenSubject = new BehaviorSubject<string | null>(null);

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
      if (error.status === 401 && !req.url.includes('/auth/')) {
        return handle401Error(authReq, next, authService, router, snackBar);
      }

      return throwError(() => error);
    })
  );
};

// --- Funciones Auxiliares ---

function shouldSkipAuth(req: HttpRequest<unknown>): boolean {
  const skipUrls = ['/auth/login', '/auth/refresh', '/register-invitation'];
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

// CASO 1: Si NO se está refrescando actualmente, iniciamos el proceso
  if (!isRefreshingToken) {
    isRefreshingToken = true;
    refreshTokenSubject.next(null); // Ponemos el semáforo en null (bloqueado)

    return authService.refreshToken().pipe(
      switchMap((user: User) => {
        isRefreshingToken = false;
        // Asumimos que authService.refreshToken() devuelve la respuesta con el nuevo token
        // y que internamente actualiza el localStorage/signal.
        
        // Si authService.refreshToken devuelve void o el objeto usuario completo, 
        // asegúrate de obtener el string del token aquí:
        const newToken = user.token;

        if (!newToken) {
          return logoutAndRedirect(authService, router, snackBar);
        }

        // Desbloqueamos el semáforo emitiendo el nuevo token
        refreshTokenSubject.next(newToken);
        
        // Reintentamos la petición original
        return next(addTokenToRequest(req, newToken));
      }),
      catchError((err) => {
        isRefreshingToken = false;
        // Si falla el refresh, cerramos sesión
        return logoutAndRedirect(authService, router, snackBar);
      })
    );

  } else {
    // CASO 2: Ya se está refrescando. Esperamos.
    return refreshTokenSubject.pipe(
      filter(token => token != null), // Esperamos hasta que el token no sea null
      take(1), // Tomamos el primer valor válido y nos desuscribimos
      switchMap(token => {
        // Reintentamos la petición con el token que consiguió la primera petición
        return next(addTokenToRequest(req, token!));
      })
    );
  }
}

function logoutAndRedirect(authService: AuthService, router: Router, snackBar: MatSnackBar): Observable<HttpEvent<any>> {
    authService.logout();
    
    router.navigate(['/auth/login'], {
      state: { sessionExpired: true },
    });

    setTimeout(() => {
      snackBar.open('Tu sesión ha expirado. Por favor, inicia sesión de nuevo.', 'Cerrar', {
        duration: 5000,
        panelClass: ['error-snackbar'],
      });
    });


    return throwError(() => new Error('Session expired'));
}