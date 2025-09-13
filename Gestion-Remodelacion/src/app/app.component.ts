import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
// 1. IMPORTAR el servicio de traducción
import { TranslateService } from '@ngx-translate/core';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet],
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss'],
})
export class AppComponent {
  title = 'gestion-remodelacion';

  // 2. INYECTAR el servicio en el constructor y AÑADIR la lógica de inicialización
  constructor(private translate: TranslateService) {
    // Establece 'es' (español) como el idioma por defecto o de respaldo.
    this.translate.setDefaultLang('es');

    // Detecta el idioma del navegador del usuario.
    const browserLang = this.translate.getBrowserLang();
    
    // Usa el idioma del navegador si es 'en' o 'es'; de lo contrario, usa 'es'.
    this.translate.use(browserLang?.match(/en|es/) ? browserLang : 'es');
  }
}