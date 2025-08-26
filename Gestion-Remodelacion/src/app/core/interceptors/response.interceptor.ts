// src/app/core/interceptors/response.interceptor.ts
import { HttpEvent, HttpHandlerFn, HttpInterceptorFn, HttpRequest, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { ApiResponse } from '../models/ApiResponse';

export const responseInterceptor: HttpInterceptorFn = (req: HttpRequest<unknown>, next: HttpHandlerFn): Observable<HttpEvent<unknown>> => {
  return next(req).pipe(
    map(event => {
      // Solo queremos procesar respuestas exitosas (HttpResponse)
      if (event instanceof HttpResponse) {
        // Verificamos si la respuesta tiene la estructura esperada de ApiResponse.
        // Esto es un chequeo básico; puedes hacerlo más robusto si es necesario.
        const responseBody = event.body;
        if (responseBody && typeof responseBody === 'object' && 'status' in responseBody && 'message' in responseBody && 'data' in responseBody) {
          const apiResponse = responseBody as ApiResponse<any>;

          // Si 'apiResponse.data' es nulo o indefinido, podríamos devolver un error
          // o simplemente el 'body' como null/undefined dependiendo de tu lógica.
          // Para esta implementación, devolveremos apiResponse.data.
          // Clonamos el evento para reemplazar el cuerpo y pasarlo por la tubería.
          return event.clone({ body: apiResponse.data });
        }
      }
      // Si no es un HttpResponse o no tiene la estructura de ApiResponse,
      // simplemente lo dejamos pasar sin modificar.
      return event;
    })
  );
};