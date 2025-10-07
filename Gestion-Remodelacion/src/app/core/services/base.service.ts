import { HttpClient } from '@angular/common/http';
import { ApiResponse } from '../models/ApiResponse';
import { Page } from '../models/page.model';

// Este servicio no necesita ser inyectado, es una clase base.
export class BaseService<T> {
  constructor(protected http: HttpClient, protected apiUrl: string) {}

  // Método para extraer datos de un ApiResponse, manejando el formato de paginación.
  protected extractPageData(response: any): Page<T> {
    // Si el backend devuelve ApiResponse
    const apiData = response && 'data' in response ? response.data : response;

    // Si el backend devuelve una estructura anidada { content: [...], page: {...} }
    if (apiData && 'content' in apiData && 'page' in apiData) {
      const pageData = apiData.page;
      return {
        content: response.content,
        totalElements: pageData.totalElements,
        totalPages: pageData.totalPages,
        size: pageData.size,
        number: pageData.number,
        first: pageData.number === 0, // Debes calcular esto o pedirlo al backend
        last: pageData.number === pageData.totalPages - 1, // Debes calcular esto o pedirlo al backend
        empty: response.content.length === 0,
      } as Page<T>;
    }
    
    // Si el backend devuelve Page<T> directamente
    return response as Page<T>;
  }

  protected extractSingleData<U>(response: ApiResponse<U>): U {
    if (response && 'data' in response && response.data) {
      return response.data;
    }
    return response as U;
  }
}