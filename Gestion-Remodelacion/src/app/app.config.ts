// src/app/app.config.ts
import { routes } from './app.routes';
import { ApplicationConfig, importProvidersFrom, isDevMode } from '@angular/core'; // 1. Importa isDevMode
import { provideRouter } from '@angular/router';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { jwtInterceptor } from './core/interceptors/jwt.interceptor';
import { responseInterceptor } from './core/interceptors/response.interceptor';

// --- Importaciones para ngx-translate ---
import { TranslateLoader, TranslateModule } from '@ngx-translate/core';
import { TranslateHttpLoader } from '@ngx-translate/http-loader';

// --- Importaciones de Akita ---
import { AkitaNgDevtools } from '@datorama/akita-ngdevtools'; // 2. Importa las DevTools

// --- Función Factory ---
export function HttpLoaderFactory(httpClient: HttpClient): TranslateHttpLoader {
  return new TranslateHttpLoader(httpClient, './assets/i18n/', '.json');
}

export const appConfig: ApplicationConfig = {
  providers: [
    provideRouter(routes),
    provideAnimationsAsync(),
    provideHttpClient(withInterceptors([
      jwtInterceptor,
      responseInterceptor    
    ])),

    // --- Configuración de ngx-translate y Akita DevTools ---
    importProvidersFrom([ // 3. importProvidersFrom ahora es un array
      TranslateModule.forRoot({
        loader: {
          provide: TranslateLoader,
          useFactory: HttpLoaderFactory,
          deps: [HttpClient]
        }
      }),
      
      // 4. Configuración de Akita DevTools
      // Se activa solo si NO estás en producción (isDevMode() es true)
      isDevMode() ? AkitaNgDevtools.forRoot() : []
    ])
  ]
};