// src/app/core/guards/valid-token.guard.ts
import { CanActivateFn } from '@angular/router';
import { inject } from '@angular/core';
import { AuthService } from '../services/auth.service';
import { Router } from '@angular/router';

export const validTokenGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  const router = inject(Router);
  
  if (!auth.isAuthenticated() || !auth.isTokenValid(auth.getToken()!)) {
    auth.logout();
    return router.parseUrl('/auth/login');
  }
  
  return true;
};