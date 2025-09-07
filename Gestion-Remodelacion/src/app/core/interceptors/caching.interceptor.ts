// src/app/core/interceptors/caching.interceptor.ts
import { HttpInterceptorFn, HttpRequest, HttpHandlerFn, HttpEvent, HttpResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { Observable, of } from 'rxjs';
import { tap } from 'rxjs/operators';
import { CacheService } from '../services/cache.service';
import { environment } from '../../../environments/environment';
import { NotificationService } from '../services/notification.service';


export const cachingInterceptor: HttpInterceptorFn = (req: HttpRequest<unknown>, next: HttpHandlerFn): Observable<HttpEvent<unknown>> => {
  const cacheService = inject(CacheService);


  // No cachear peticiones de autenticación
  if (req.url.includes('/api/auth')) {
    return next(req);
  }

  // Lógica de invalidación para métodos de escritura
  if (req.method !== 'GET') {
    console.log(`%c Invalidando TODO el caché debido a una petición ${req.method} a ${req.url} `, 'background: orange; color: black');
    
    cacheService.invalidateAll();
  
    return next(req);
  }

  // Lógica de cacheo para métodos GET
  const cachedResponse = cacheService.get(req.urlWithParams);
  if (cachedResponse) {
    return of(cachedResponse.clone());
  }

  return next(req).pipe(
    tap(event => {
      if (event instanceof HttpResponse) {
        cacheService.set(req.urlWithParams, event);
      }
    })
  );
};