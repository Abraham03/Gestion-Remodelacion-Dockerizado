import { CanActivateFn } from '@angular/router';
import { inject } from '@angular/core';
import { AuthService } from '../services/auth.service';
import { Router } from '@angular/router';

export const roleGuard: CanActivateFn = (route, state) => {
  const auth = inject(AuthService);
  const router = inject(Router);

  const requiredRoles = route.data['roles'] as string[];
  const requiredPermission = route.data['permission'] as string;
  const fullPath = state.url;

  if (!auth.isAuthenticated()) {
    return router.parseUrl('/login');
  }

  // Devuelve Role[], por lo que accedemos a `role.name`
  const userRoles = auth.userRoles();
  const userPermissions = auth.userPermissions(); // Asume que tienes este signal en AuthService

  // 1. Check for roles if defined
  if (requiredRoles && requiredRoles.length > 0) {
    // Usar `some` para verificar si el `name` del objeto `Role` está en la lista de `requiredRoles`
    const hasRequiredRole = requiredRoles.some((roleName: string) =>
        userRoles.some(userRole => userRole.name === `ROLE_${roleName}`)
    );

    // ⭐️ ADICIÓN: Verificar si el usuario tiene el rol ADMIN, ya que podría estar en el `userRoles`
    const isAdmin = userRoles.some(userRole => userRole.name === 'ROLE_ADMIN');

    if (!hasRequiredRole && !isAdmin) {
      console.warn(`RoleGuard: Access DENIED for roles on route: ${fullPath}. Required: ${requiredRoles.join(', ')}`);
      return router.parseUrl('/forbidden');
    }
  }

  // 2. Check for specific permission if defined
  if (requiredPermission) {
    // Se utiliza el signal `userPermissions` para chequear los permisos
    const hasRequiredPermission = userPermissions.includes(requiredPermission);

    // Verificar si el usuario tiene el rol ADMIN, ya que podría estar en el `userRoles`
    const isAdmin = userRoles.some(userRole => userRole.name === 'ROLE_ADMIN');

    if (!hasRequiredPermission && !isAdmin) {
      console.warn(`RoleGuard: Access DENIED for permission on route: ${fullPath}. Required: ${requiredPermission}`);
      return router.parseUrl('/forbidden');
    }
  }

  return true;
};