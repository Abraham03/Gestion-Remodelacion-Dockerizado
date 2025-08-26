import { Router, Routes } from '@angular/router';
import { LoginComponent } from './modules/auth/components/login/login.component';
import { LayoutComponent } from './shared/components/layout/layout.component';
import { authGuard } from './core/guards/auth.guard';
import { AuthService } from './core/services/auth.service';
import { inject } from '@angular/core';
import { validTokenGuard } from './core/guards/valid-token.guard';
import { roleGuard } from './core/guards/role.guard';

export const routes: Routes = [
  {
    path: 'login',
    component: LoginComponent,
    // Este canActivate asegura que si ya estás logueado, te redirija al dashboard
    canActivate: [() => !inject(AuthService).isAuthenticated() ? true : inject(Router).parseUrl('/dashboard')]
  },
  {
    path: '',
    component: LayoutComponent,
    // authGuard asegura que el usuario esté logueado.
    // validTokenGuard no es estrictamente necesario si tu AuthService y los interceptores manejan la renovación
    // y expiración del token de manera proactiva. Si quieres una capa adicional, puedes mantenerlo.
    // Para la lógica actual de AuthService, authGuard es suficiente para asegurar la autenticación.    
    canActivate: [authGuard, validTokenGuard], // Protege las rutas internas con ambos guards
    children: [
      {
        path: 'dashboard',
        loadComponent: () => import('./modules/dashboard/components/dashboard/dashboard.component').then(m => m.DashboardComponent),
        canActivate: [roleGuard],
        data: { permission: 'DASHBOARD_VIEW' } // Ejemplo de roles y permiso
      },
      {
        path: 'empleados',
        loadComponent: () => import('./modules/empleados/components/empleado-list/empleado-list.component').then(m => m.EmpleadoListComponent),
        canActivate: [roleGuard],
        data: { permission: 'EMPLEADO_READ' } // Solo ADMIN y MANAGER pueden leer empleados
      },
      {
        path: 'clientes',
        loadComponent: () => import('./modules/cliente/components/cliente-list/cliente-list.component').then(m => m.ClienteListComponent),
        canActivate: [roleGuard],
        data: { permission: 'CLIENTE_READ' } // Solo ADMIN y MANAGER pueden leer proyectos
      },      
      {
        path: 'proyectos',
        loadComponent: () => import('./modules/proyectos/components/proyecto-list/proyecto-list.component').then(m => m.ProyectosListComponent),
        canActivate: [roleGuard],
        data: { permission: 'PROYECTO_READ' } // Solo ADMIN y MANAGER pueden leer proyectos
      },
      {
        path: 'reportes',
        loadComponent: () => import('./modules/reportes/components/reporte-list/reporte-list.component').then(m => m.ReportesListComponent),
        canActivate: [roleGuard],
        data: { permission: 'REPORTE_READ' } // Ejemplo de permiso para reportes
      },      
      {
        path: 'horas-trabajadas',
        loadComponent: () => import('./modules/horas-trabajadas/components/horas-trabajadas-list/horas-trabajadas-list.component').then(m => m.HorasTrabajadasListComponent),
        canActivate: [roleGuard],
        data: { permission: 'HORASTRABAJADAS_READ' } // Permiso para horas trabajadas
      },
            {
        path: 'usuarios', // Nueva ruta para la gestión de usuarios
        loadComponent: () => import('./modules/users/components/user-list/user-list/user-list.component').then(m => m.UserListComponent),
        canActivate: [roleGuard],
        data: { permission: 'USER_READ' } // Permiso para ver la lista de usuarios
      },
      {
        path: 'roles',
        loadComponent: () => import('./modules/roles/components/role-list/role-list/role-list.component').then(m => m.RoleListComponent),
        canActivate: [roleGuard],
        data: { permission: 'ROLE_READ' } // Permiso para roles
      },
            {
        path: 'forbidden', // Ruta para acceso denegado
        loadComponent: () => import('./shared/components/forbidden/forbidden.component').then(m => m.ForbiddenComponent)
      },
      // ----------------------------------------

      // Las rutas de formularios como 'empleados/nuevo' y 'empleados/editar/:id'
      // no son necesarias si abres los formularios como MatDialogs desde la vista de lista.
      // Si aún los necesitas como rutas directas, deberás gestionarlos con roleGuard y data.

      { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
      { path: '**', redirectTo: 'dashboard' } // O una página de 404
    ]
  }
];