// src/app/core/services/auth.service.ts
import { computed, Injectable, OnDestroy, signal } from '@angular/core';
import { AuthApiService } from '../../modules/auth/services/auth-api.service';
import { Router } from '@angular/router';
import { catchError, filter, interval, map, Observable, Subject, takeUntil, tap, throwError } from 'rxjs';
import { User, UserRequest } from '../models/user.model';
import { MatSnackBar } from '@angular/material/snack-bar';
import { AuthResponse } from '../../modules/auth/models/auth-response.model';
import { Role } from '../models/role.model';
import { resetStores } from '@datorama/akita';

@Injectable({ providedIn: 'root' })
export class AuthService implements OnDestroy {
  private _currentUser = signal<User | null>(null);
  private destroy$ = new Subject<void>();
  private readonly SESSION_WARNING_TIME = 5 * 60 * 1000;
  private readonly CHECK_INTERVAL = 60 * 1000;
  private sessionWarningShown = false;


  
  
  currentUser = this._currentUser.asReadonly();
  // Si el usuario está autenticado
  isAuthenticated = computed(() => !!this._currentUser());
  // Si el usuario tiene permisos
  userPermissions = computed(() => this._currentUser()?.authorities || []);
  // Si el usuario tiene roles
  userRoles = computed(() => this._currentUser()?.roles || []); 
  // Informacion de la empresa, Logo, Plan , Id y nombre de la empresa
  currentUserEmpresaLogo = computed(() => this._currentUser()?.logoUrl || null);
  currentUserPlan = computed(() => this._currentUser()?.plan || null);
  currentUserEmpresaId = computed(() => this._currentUser()?.empresaId || null);
  currentUserEmpresaNombre = computed(() => this._currentUser()?.nombreEmpresa || '');




  constructor(
    private authApi: AuthApiService,
    private router: Router,
    private snackBar: MatSnackBar,
  ) {
    this.loadUserFromStorage();
    this.startTokenValidation();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  login(credentials: { username: string; password: string }): Observable<User> {
    return this.authApi.login(credentials).pipe(
      tap((response) => {
        this.updateAuthState(response);
        this.router.navigateByUrl('/dashboard');
      }),
      map((response) => this.mapToUser(response)),
      catchError((error) => {
        this.handleAuthError(error);
        return throwError(() => error);
      })
    );
  }

  refreshToken(): Observable<User> {
    const refreshToken = this._currentUser()?.refreshToken;
    if (!refreshToken) {
      this.logout();
      return throwError(() => new Error('No hay token de refresco disponible'));
    }

    return this.authApi.refreshToken(refreshToken).pipe(
      tap((response) => this.updateAuthState(response)),
      map((response) => this.mapToUser(response)),
      catchError((error) => {
        this.handleAuthError(error);
        this.logout();
        return throwError(() => error);
      })
    );
  }

  logout(): void {
   const refreshToken = this._currentUser()?.refreshToken;
    if (refreshToken) {
      // Se intenta revocar el token en el backend.
      // Se usa catchError para que la limpieza del frontend ocurra incluso si esta llamada falla.
      this.authApi.revokeToken(refreshToken).pipe(
        catchError(() => []) // Ignora errores en el logout para no bloquear la limpieza.
      ).subscribe();
    }

    // 1. Limpia el estado de autenticación (localStorage y signal).
    this.clearAuthState();
    console.log('AuthService: Clearing auth state and resetting stores...');

    // 3. Redirige al login.
    this.router.navigate(['/login']);
  }

  getToken(): string | null {
    return this._currentUser()?.token || null;
  }

  isTokenValid(token: string): boolean {
    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      return payload.exp && payload.exp * 1000 > Date.now();
    } catch {
      return false;
    }
  }

  public get isSuperAdmin(): boolean {
    return this.hasRole('ROLE_SUPER_ADMIN');
  }

  hasRole(roleName: string): boolean {
    // Buscar si algún objeto Role en el array tiene ese nombre
    return this.userRoles().some(role => role.name === roleName);
  }

  hasPermission(permission: string): boolean {
    if (this.hasRole('ROLE_SUPER_ADMIN')) {
      return true;
    }
    return this.userPermissions().includes(permission);
  }

  private loadUserFromStorage(): void {
    const userData = localStorage.getItem('user_data');
    if (userData) {
      try {
        const parsedData = JSON.parse(userData);
        if (parsedData?.token && parsedData?.authorities && parsedData?.roles) {
          this._currentUser.set(parsedData);
        } else {
          this.clearAuthState();
        }
      } catch (e) {
        console.error('Error al parsear datos de usuario desde localStorage', e);
        this.clearAuthState();
      }
    }
  }

  private updateAuthState(response: AuthResponse): void {
    const userData = this.mapToUser(response);
    localStorage.setItem('user_data', JSON.stringify(userData));
    this._currentUser.set(userData);
  }

  /**
   * Este método se ha modificado para realizar la conversión.
   * Transforma la lista de strings de roles del backend en una lista de objetos Role.
   */
  private mapToUser(response: AuthResponse): User {
    // Mapea el string[] de roles del backend a un Role[] en el frontend
    const userRoles: Role[] = (response.roles || []).map(roleName => ({
      id: 0, // Un ID temporal o nulo, ya que no lo recibes del backend
      name: roleName,
      description: '', // Una descripción vacía
      permissions: [] // Un array de permisos vacío
    }));

    return {
      id: response.id,
      username: response.username,
      email: response.email,
      authorities: response.authorities,
      token: response.token,
      refreshToken: response.refreshToken,
      expirationDate: response.expirationDate,
      type: response.type,
      enabled: response.enabled ?? true,
      roles: userRoles, 
      accountNonExpired: true,
      accountNonLocked: true,
      credentialsNonExpired: true,
      empresaId: response.empresaId,
      plan: response.plan,
      logoUrl: response.logoUrl,
      nombreEmpresa: response.nombreEmpresa
    };
  }

  private clearAuthState(): void {
    localStorage.removeItem('user_data');
    this._currentUser.set(null);
    this.sessionWarningShown = false;
    resetStores();
  }
  
  // El resto del código de validación de token y manejo de errores se mantiene igual...
  private startTokenValidation(): void {
    interval(this.CHECK_INTERVAL).pipe(
      filter(() => this.isAuthenticated()),
      takeUntil(this.destroy$)
    ).subscribe(() => {
      const userData = this._currentUser();
      if (!userData?.token) {
        this.handleSessionExpiration();
        return;
      }
      const isTokenExpiring = this.isTokenExpiringSoon(userData.token);
      const isRefreshExpired = this.isRefreshTokenExpired();
  
      if (isRefreshExpired) {
        this.handleSessionExpiration();
      } else if (isTokenExpiring && !this.sessionWarningShown) {
        this.showSessionWarning();
      }
    });
  }
  
  private isTokenExpiringSoon(token: string): boolean {
    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      const expiresIn = payload.exp * 1000 - Date.now();
      return expiresIn < this.SESSION_WARNING_TIME;
    } catch {
      return true;
    }
  }
  
  private isRefreshTokenExpired(): boolean {
    const userData = this._currentUser();
    if (!userData?.refreshToken || !userData.expirationDate) return true;
    return new Date(userData.expirationDate).getTime() <= Date.now();
  }
  
  private showSessionWarning(): void {
    this.sessionWarningShown = true;
    this.snackBar.open(
      'Tu sesión está por expirar. ¿Deseas extenderla?',
      'Extender',
      { duration: 10000 }
    ).onAction().subscribe(() => {
      this.refreshToken().subscribe({
        next: () => {
          this.snackBar.open('Sesión extendida correctamente.', 'Cerrar', { duration: 3000 });
          this.sessionWarningShown = false;
        },
        error: (err) => {
          console.error('Error al extender la sesión:', err);
          this.sessionWarningShown = false;
        }
      });
    });
  }
  
  private handleSessionExpiration(): void {
    this.snackBar.open(
      'Sesión expirada. Serás redirigido al login',
      'Entendido',
      { duration: 5000 }
    );
    this.logout();
  }
  
  private handleAuthError(error: any): void {
    const message = error.message || 'Error de autenticación';
    this.snackBar.open(message, 'Cerrar', {
      duration: 5000,
      panelClass: ['error-snackbar']
    });
  }
  
}