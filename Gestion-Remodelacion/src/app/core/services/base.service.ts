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
        content: apiData.content,
        totalElements: pageData.totalElements,
        totalPages: pageData.totalPages,
        size: pageData.size,
        number: pageData.number,
        first: pageData.number === 0, // Debes calcular esto o pedirlo al backend
        last: pageData.number === pageData.totalPages - 1, // Debes calcular esto o pedirlo al backend
        empty: apiData.content.length === 0,
      } as Page<T>;
    }
    
    // Si el backend devuelve Page<T> directamente
    return response as Page<T>;
  }

  protected extractSingleData<U>(response: ApiResponse<U>): U {
// Esta lógica es "segura" contra interceptors.
    // Si 'response' ya es la data (ej. un array), no tendrá '.data' y lo devolverá.
    // Si 'response' es el ApiResponse, entrará al if y devolverá 'response.data'.
    if (response && typeof response === 'object' && 'data' in response && response.data) {
      return response.data;
    }
    // Si no, asumimos que ya es la data.
    return response as U;
  }
}