// src/app/core/interceptors/caching.interceptor.ts
import { HttpInterceptorFn, HttpRequest, HttpHandlerFn, HttpEvent, HttpResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { Observable, of } from 'rxjs';
import { tap } from 'rxjs/operators';
import { CacheService } from '../services/cache.service';
import { environment } from '../../../environments/environment';
import { NotificationService } from '../services/notification.service';

// Función auxiliar para obtener el prefijo de la URL
const getCacheKeyPrefix = (url: string): string => {
  const urlParts = url.split('/');
  const apiIndex = urlParts.indexOf('api');
  if (apiIndex > -1 && urlParts.length > apiIndex + 1) {
    const endpoint = urlParts[apiIndex + 1];
    return `${environment.apiUrl}/${endpoint}`;
  }
  return '';
};

export const cachingInterceptor: HttpInterceptorFn = (req: HttpRequest<unknown>, next: HttpHandlerFn): Observable<HttpEvent<unknown>> => {
  const cacheService = inject(CacheService);
  const notificationService = inject(NotificationService); // Inyectamos para notificar cambios

  // No cachear peticiones de autenticación
  if (req.url.includes('/api/auth')) {
    return next(req);
  }

  // Lógica de invalidación para métodos de escritura
  if (req.method !== 'GET') {
    const cacheKey = getCacheKeyPrefix(req.url);
    console.log(`[Cache Interceptor] Invalidando caché para: ${cacheKey}`);
    cacheService.invalidateStartingWith(cacheKey);
  
    // Invalida siempre el cache del dashboard
    cacheService.invalidateStartingWith(`${environment.apiUrl}/dashboard`);
    console.log(`[Cache Interceptor] Invalidando caché para el Dashboard`);

    notificationService.notifyDataChange(); // Notificamos que los datos cambiaron
    return next(req);
  }

  // Lógica de cacheo para métodos GET
  const cachedResponse = cacheService.get(req.urlWithParams);
  if (cachedResponse) {
    console.log(`[Cache Interceptor] Sirviendo desde caché: ${req.urlWithParams}`);
    return of(cachedResponse.clone());
  }

  return next(req).pipe(
    tap(event => {
      if (event instanceof HttpResponse) {
        console.log(`[Cache Interceptor] Guardando en caché: ${req.urlWithParams}`);
        cacheService.set(req.urlWithParams, event);
      }
    })
  );
};