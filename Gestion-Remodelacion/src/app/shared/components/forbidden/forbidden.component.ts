// src/app/shared/components/forbidden/forbidden.component.ts
import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-forbidden',
  standalone: true,
  imports: [CommonModule, MatCardModule, MatButtonModule, RouterModule],
  template: `
    <div class="forbidden-container">
      <mat-card class="forbidden-card">
        <mat-card-header>
          <mat-card-title>Acceso Denegado</mat-card-title>
        </mat-card-header>
        <mat-card-content>
          <p>Lo sentimos, no tienes los permisos necesarios para acceder a esta página.</p>
          <p>Si crees que esto es un error, por favor, contacta al administrador.</p>
        </mat-card-content>
        <mat-card-actions align="end">
          <button mat-raised-button color="primary" routerLink="/dashboard">Ir al Dashboard</button>
        </mat-card-actions>
      </mat-card>
    </div>
  `,
  styles: `
    .forbidden-container {
      display: flex;
      justify-content: center;
      align-items: center;
      min-height: calc(100vh - var(--header-height-desktop) - var(--footer-height-desktop)); /* Ajusta a la altura del contenido principal */
      padding: 24px;
      box-sizing: border-box;
    }
    .forbidden-card {
      max-width: 400px;
      text-align: center;
      padding: 20px;
      box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
    }
    .forbidden-card mat-card-title {
      color: #f44336; /* Color rojo para indicar error/negación */
      font-size: 1.8em;
      margin-bottom: 15px;
    }
    .forbidden-card mat-card-content p {
      margin-bottom: 10px;
      line-height: 1.5;
    }
    .forbidden-card mat-card-actions {
      margin-top: 20px;
    }

    /* Adaptación para dispositivos móviles */
    @media (max-width: 600px) {
      .forbidden-container {
        min-height: calc(100vh - var(--header-height-mobile) - var(--footer-height-mobile));
      }
      .forbidden-card {
        padding: 15px;
      }
      .forbidden-card mat-card-title {
        font-size: 1.5em;
      }
    }
  `
})
export class ForbiddenComponent {}