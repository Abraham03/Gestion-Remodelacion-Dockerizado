// src/app/shared/components/sidebar/sidebar.component.ts
import { Component, Input, inject, OnInit, OnDestroy, signal, computed } from '@angular/core'; // Agrega 'computed'
import { MatListModule } from '@angular/material/list';
import { MatIconModule } from '@angular/material/icon';
import { RouterModule } from '@angular/router';
import { MenuItem } from '../../models/menu-item.model';
import { CommonModule, TitleCasePipe } from '@angular/common';
import { AuthService } from '../../../core/services/auth.service';
import { SidebarService } from './sidebar.service';
import { Subscription, Observable, Subject, map, shareReplay, takeUntil } from 'rxjs';
import { toObservable } from '@angular/core/rxjs-interop';

import { BreakpointObserver, Breakpoints } from '@angular/cdk/layout';

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [MatListModule, MatIconModule, RouterModule, CommonModule],
  templateUrl: './sidebar.component.html',
  styleUrls: ['./sidebar.component.scss'],
})
export class SidebarComponent implements OnInit, OnDestroy {
  @Input() isSidebarOpen = true;

  private authService = inject(AuthService);
  private sidebarService = inject(SidebarService);
  private breakpointObserver = inject(BreakpointObserver);

  private destroy$ = new Subject<void>();

  // Use computed to derive userName and userRole reactively
  // This is the correct and more reactive way to handle them with Signals
  userName = computed(() => this.authService.currentUser()?.username || 'Invitado'); // Fixes Object is possibly 'null'.
  userRole = computed(() => {
    const roles = this.authService.userRoles();
    // Verifica si hay roles antes de intentar acceder a roles[0]
    return roles.length > 0 ? roles[0].name.replace('ROLE_', '') : null;
  });

  isHandset$: Observable<boolean>;
  private subscriptions = new Subscription();

  menuItems: MenuItem[] = [
    { label: 'Inicio', icon: 'home', route: '/dashboard', permission: 'DASHBOARD_VIEW' },
    { label: 'Empleados', icon: 'people', route: '/empleados', permission: 'EMPLEADO_READ' },
    { label: 'Clientes', icon: 'assignment_ind', route: '/clientes', permission: 'CLIENTE_READ' },    
    { label: 'Proyectos', icon: 'work', route: '/proyectos', permission: 'PROYECTO_READ' },
    { label: 'Horas Trabajadas', icon: 'schedule', route: '/horas-trabajadas', permission: 'HORASTRABAJADAS_READ' },
    { label: 'Reportes', icon: 'assessment', route: '/reportes', permission: 'REPORTE_READ' },
    { label: 'Roles', icon: 'security', route: '/roles', permission: 'ROLE_READ' },
    { label: 'Usuarios', icon: 'manage_accounts', route: '/usuarios', permission: 'USER_READ' },
  ];

  constructor() {
    this.isHandset$ = this.breakpointObserver.observe(Breakpoints.Handset)
      .pipe(
        map(result => result.matches),
        shareReplay()
      );

    // If you need to react to changes and execute side effects (like console logs),
    // you can still use toObservable and subscribe, but for simply displaying the values,
    // the computed signals are sufficient and preferred.
    // The previous subscription to `toObservable(this.authService.currentUser)` is no longer strictly necessary
    // for updating `userName` and `userRole` because they are now `computed` signals.
    // This makes the code cleaner and more Angular Signals-centric.
  }

  ngOnInit() {
    this.subscriptions.add(
      this.sidebarService.isSidebarOpen$.subscribe(isOpen => {
        this.isSidebarOpen = isOpen;
      })
    );
  }

  /**
   * Checks if the user has a specific permission.
   * @param permission The required permission for the menu item.
   * @returns `true` if the user has the permission or if the item does not require permission, `false` otherwise.
   */
  hasPermission(permission?: string): boolean {
    if (!permission) {
      return true;
    }
    // Delegate permission check to AuthService
    return this.authService.hasPermission(permission);
  }

  onMenuItemClick() {
    this.isHandset$.subscribe(isHandset => {
      if (isHandset) {
        this.sidebarService.closeSidebar();
      }
    }).unsubscribe();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
    this.subscriptions.unsubscribe();
  }
}