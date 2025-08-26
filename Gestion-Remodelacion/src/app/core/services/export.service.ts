// src/app/core/services/export.service.ts
import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class ExportService {
  constructor(private http: HttpClient) {}

  /**
   * Método genérico para exportar datos a Excel.
   * @param apiUrl La URL del endpoint de exportación (ej. 'http://localhost:8080/api/clientes/export/excel').
   * @param filter El valor del filtro de búsqueda.
   * @param sort El criterio de ordenamiento.
   * @returns Un Observable que emite un Blob con los datos del archivo.
   */
  exportToExcel(apiUrl: string, filter: string, sort: string): Observable<Blob> {
    let params = new HttpParams();
    if (filter) {
      params = params.set('filter', filter);
    }
    if (sort) {
      params = params.set('sort', sort);
    }
    return this.http.get(apiUrl, { params, responseType: 'blob' });
  }

  /**
   * Método genérico para exportar datos a PDF.
   * @param apiUrl La URL del endpoint de exportación (ej. 'http://localhost:8080/api/clientes/export/pdf').
   * @param filter El valor del filtro de búsqueda.
   * @param sort El criterio de ordenamiento.
   * @returns Un Observable que emite un Blob con los datos del archivo.
   */
  exportToPdf(apiUrl: string, filter: string, sort: string): Observable<Blob> {
    let params = new HttpParams();
    if (filter) {
      params = params.set('filter', filter);
    }
    if (sort) {
      params = params.set('sort', sort);
    }
    return this.http.get(apiUrl, { params, responseType: 'blob' });
  }

  /**
   * Método genérico para descargar el archivo.
   * @param data El Blob con los datos del archivo.
   * @param filename El nombre del archivo a guardar.
   * @param filetype El tipo MIME del archivo.
   */
  downloadFile(data: Blob, filename: string, filetype: string): void {
    const blob = new Blob([data], { type: filetype });
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = filename;
    document.body.appendChild(a);
    a.click();
    window.URL.revokeObjectURL(url);
    document.body.removeChild(a);
  }
}