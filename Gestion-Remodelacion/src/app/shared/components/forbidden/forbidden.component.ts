// src/app/shared/components/forbidden/forbidden.component.ts
import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon'; // 👈 1. Importa MatIconModule
import { RouterModule } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core'; // 👈 2. Importa TranslateModule

@Component({
  selector: 'app-forbidden',
  standalone: true,
  // 👇 3. Agrega los nuevos módulos a los imports
  imports: [
    CommonModule,
    MatCardModule,
    MatButtonModule,
    RouterModule,
    MatIconModule,
    TranslateModule,
  ],
  template: `
    <div class="forbidden-container">
      <mat-card class="forbidden-card">
        <mat-card-content>
          <mat-icon class="forbidden-icon" aria-hidden="false" aria-label="Lock icon">lock</mat-icon>
          
          <h1 class="forbidden-title">{{ 'FORBIDDEN.ACCESS_DENIED' | translate }}</h1>
          
          <p class="forbidden-message">{{ 'FORBIDDEN.MESSAGE_1' | translate }}</p>
          <p class="forbidden-message">{{ 'FORBIDDEN.MESSAGE_2' | translate }}</p>

        </mat-card-content>
      </mat-card>
    </div>
  `,
  styles: `
    /* 5. Estilos mejorados para un look más profesional */
    :host {
      --warn-color: #f44336;
      --text-primary: #333;
      --text-secondary: #6c757d;
      --card-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
    }

    .forbidden-container {
      display: flex;
      justify-content: center;
      align-items: center;
      min-height: calc(100vh - var(--header-height-desktop, 128px) - var(--footer-height-desktop, 64px));
      padding: 24px;
      box-sizing: border-box;
      background-color: #f8f9fa;
    }

    .forbidden-card {
      max-width: 480px;
      width: 100%;
      text-align: center;
      padding: 40px 32px;
      border-radius: 12px;
      box-shadow: var(--card-shadow);
      border: 1px solid #e9ecef;
    }

    .forbidden-card mat-card-content {
      display: flex;
      flex-direction: column;
      align-items: center;
    }

    .forbidden-icon {
      font-size: 80px;
      width: 80px;
      height: 80px;
      color: var(--warn-color);
      margin-bottom: 24px;
    }

    .forbidden-title {
      font-size: 2em;
      font-weight: 600;
      margin: 0 0 16px;
      color: var(--text-primary);
    }

    .forbidden-message {
      margin: 0 0 8px;
      line-height: 1.6;
      color: var(--text-secondary);
      max-width: 380px; /* Evita que el texto sea demasiado ancho */
    }

    .forbidden-message:last-of-type {
      margin-bottom: 32px;
    }

    .forbidden-card button {
      min-width: 200px;
      padding: 8px 0;
      font-size: 1em;
    }

    /* Adaptación para dispositivos móviles */
    @media (max-width: 600px) {
      .forbidden-container {
        min-height: calc(100vh - var(--header-height-mobile, 112px) - var(--footer-height-mobile, 56px));
      }
      .forbidden-card {
        padding: 32px 24px;
      }
      .forbidden-icon {
        font-size: 64px;
        width: 64px;
        height: 64px;
      }
      .forbidden-title {
        font-size: 1.6em;
      }
    }
  `
})
export class ForbiddenComponent {}