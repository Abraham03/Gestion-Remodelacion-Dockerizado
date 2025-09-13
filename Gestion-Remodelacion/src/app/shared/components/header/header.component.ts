import { Component, inject, Output, EventEmitter, OnDestroy } from '@angular/core';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatMenuModule } from '@angular/material/menu';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { SidebarService } from '../sidebar/sidebar.service';
import { CommonModule, TitleCasePipe } from '@angular/common'; // Agregamos TitleCasePipe
import { AuthService } from '../../../core/services/auth.service';
import { Router } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatDivider } from '@angular/material/divider';
import { TranslateService, TranslateModule } from '@ngx-translate/core';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [
    CommonModule,
    MatToolbarModule,
    MatIconModule,
    MatButtonModule,
    MatMenuModule,
    TitleCasePipe,
    MatDivider,
    TranslateModule 
  ],
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.scss'],
})
export class HeaderComponent implements OnDestroy {
  private sidebarService = inject(SidebarService);
  private router = inject(Router);
  private snackBar = inject(MatSnackBar);
  // INYECCIÓN DEL SERVICIO Y ACCESO DIRECTO A SIGNALS
  authService = inject(AuthService);

  private transtale = inject(TranslateService);
  @Output() toggleSidebarEvent = new EventEmitter<void>();

  constructor() {}

  // LÓGICA DE ROLES Y NOMBRES AHORA CON SIGNALS DIRECTOS
  // Puedes acceder a ellos directamente en el template: `authService.currentUser()?.username`

  onToggleSidebar() {
    this.toggleSidebarEvent.emit();
  }

  changeLanguage(lang: string): void {
    this.transtale.use(lang);
  }
  onLogout() {
    this.authService.logout();
    const message = this.transtale.instant('LOGIN.SESSION_EXPIRED');
    this.snackBar.open(message,this.transtale.instant('LOGIN.CLOSE'), {
      duration: 3000,
      panelClass: ['success-snackbar']
    });
    this.router.navigate(['/login']);
  }

  ngOnDestroy(): void {
    // Ya no se necesitan suscripciones, así que `subscriptions` y `ngOnDestroy` pueden ser eliminados.
  }
}