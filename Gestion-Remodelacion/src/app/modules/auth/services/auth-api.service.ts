import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { catchError, Observable, tap, throwError } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { AuthResponse } from '../models/auth-response.model';


@Injectable({
  providedIn: 'root',
})
export class AuthApiService {
  private readonly apiUrl = `${environment.apiUrl}/auth`;

  constructor(private http: HttpClient) {}

  /**
   * Realiza el login del usuario
   * @param credentials {username, password}
   * @returns Observable con la respuesta de autenticación
   */
  login(credentials: {username: string, password: string}): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/login`, credentials)
      .pipe(
        catchError(this.handleAuthError)
      );
  }

  /**
   * Renueva el token de acceso usando el refresh token
   * @param refreshToken Token de refresco
   * @returns Observable con nuevos tokens
   */
  refreshToken(refreshToken: string): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(
      `${this.apiUrl}/refresh`, 
      { refreshToken },
      { headers: this.getSkipInterceptorHeader() }
    ).pipe(
      catchError(this.handleAuthError)
    );
  }

  /**
   * Revoca un refresh token en el backend
   * @param refreshToken Token a revocar
   */
  revokeToken(refreshToken: string): Observable<void> {
    return this.http.post<void>(
      `${this.apiUrl}/logout`, 
      { refreshToken }
    ).pipe(
      catchError(this.handleAuthError)
    );
  }

  private getSkipInterceptorHeader(): HttpHeaders {
    return new HttpHeaders({ 'Skip-Interceptor': 'true' });
  }

  private handleAuthError(error: any): Observable<never> {
    let errorMessage = 'Error de autenticación';
    if (error.error?.message) {
      errorMessage = error.error.message;
    } else if (error.message) {
      errorMessage = error.message;
    }
    return throwError(() => ({
      message: errorMessage,
      status: error.status || 500
    }));
  }
}