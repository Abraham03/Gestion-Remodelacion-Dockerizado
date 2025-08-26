// src/app/shared/components/layout/layout.component.ts
import { Component, inject, ViewChild, AfterViewInit, OnInit, OnDestroy /* ELIMINAR: HostBinding */ } from '@angular/core';
import { HeaderComponent } from '../header/header.component';
import { SidebarComponent } from '../sidebar/sidebar.component';
import { FooterComponent } from '../footer/footer.component';
import { RouterOutlet } from '@angular/router';
import { MatSidenavModule, MatSidenav } from '@angular/material/sidenav';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { CommonModule } from '@angular/common';
import { SidebarService } from '../sidebar/sidebar.service';
import { BreakpointObserver, Breakpoints } from '@angular/cdk/layout';
import { Observable, Subscription, combineLatest } from 'rxjs'; // Import combineLatest
import { map, shareReplay, distinctUntilChanged } from 'rxjs/operators';

@Component({
  selector: 'app-layout',
  standalone: true,
  imports: [
    HeaderComponent,
    SidebarComponent,
    FooterComponent,
    RouterOutlet,
    MatSidenavModule,
    MatIconModule,
    MatButtonModule,
    CommonModule,
  ],
  templateUrl: './layout.component.html',
  styleUrls: ['./layout.component.scss'],
})
export class LayoutComponent implements OnDestroy {
  private sidebarService = inject(SidebarService);
  private breakpointObserver = inject(BreakpointObserver);

  @ViewChild('sidenav') sidenav!: MatSidenav;

  isSidebarOpen$: Observable<boolean> = this.sidebarService.isSidebarOpen$;
  isHandset$: Observable<boolean> = this.breakpointObserver.observe(Breakpoints.Handset).pipe(
    map(result => result.matches),
    shareReplay(1) // shareReplay(1) para asegurar que los suscriptores reciban el último valor
  );

  private subscriptions = new Subscription();



  constructor() {}

  // Agrega este método para sincronizar el estado del servicio cuando se cierra el sidenav en modo over.
  onSidenavClosed() {
    this.sidebarService.closeSidebar();
  }

  ngOnDestroy(): void {
    this.subscriptions.unsubscribe();
  }

  onToggleSidebar() {
    this.sidebarService.toggleSidebar();
  }
}