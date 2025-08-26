import { HttpErrorResponse, HttpInterceptorFn } from "@angular/common/http";
import { inject } from "@angular/core";
import { AuthService } from "../services/auth.service";
import { Router } from "@angular/router";
import { MatSnackBar } from "@angular/material/snack-bar";
import { catchError, throwError } from "rxjs";

export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const auth = inject(AuthService);
  const router = inject(Router);
  const snackBar = inject(MatSnackBar);

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      if (error.status === 401) {
        auth.logout();
        router.navigate(['/auth/login'], { 
          queryParams: { expired: 'true' } 
        });
      }
      
      const message = error.error?.message || error.message || 'Error desconocido';
      snackBar.open(message, 'Cerrar', { 
        duration: 5000,
        panelClass: ['error-snackbar']
      });
      
      return throwError(() => error);
    })
  );
};