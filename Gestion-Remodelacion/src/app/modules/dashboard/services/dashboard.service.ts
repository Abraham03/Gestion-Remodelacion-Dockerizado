import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { map, Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { DashboardSummary } from '../../../core/models/dashboard-summary.model';
import { DashboardProyecto } from '../models/dashboard-proyecto.model'; // <-- Asegúrate que esta ruta sea correcta y esté importado
import { ApiResponse } from '../../../core/models/ApiResponse';
import { BaseService } from '../../../core/services/base.service';
import { DropdownItem } from '../../../core/models/dropdown-item.model';

@Injectable({
  providedIn: 'root'
})
export class DashboardService extends BaseService<any> {

  constructor(http: HttpClient) {
    super(http, `${environment.apiUrl}/dashboard`);
  }

  getApiUrl(): string {
    return this.apiUrl;
  }

  // Método para obtener los datos al dashboard
  getProyectosSummary(year: number, month?: number | null, projectId?: number | null): Observable<any> {
    let params = new HttpParams().set('year', year.toString());

    if (month) {
      params = params.set('month', month.toString());
    }
    if (projectId) {
      params = params.set('projectId', projectId.toString());
    }

    return this.http.get<ApiResponse<any>>(`${this.apiUrl}/summary`, { params }).pipe(
      map(response => this.extractSingleData(response))
    );
  }


  // Método para obtener la lista de proyectos filtrada
  getProjectsForFilter(year: number, month?: number | null): Observable<DropdownItem[]> {
    let params = new HttpParams().set('year', year.toString());
    if (month) {
      params = params.set('month', month.toString());
    }
    return this.http.get<ApiResponse<any>>(`${this.apiUrl}/proyectos`, { params }).pipe(
      map(response => {
        // APLICAR EL MAPEO MANUAL AQUÍ PARA CONVERTIR Object[] a DropdownItem
        return this.extractSingleData(response).map((item: any[]) => ({
          id: item[0], // El ID es el primer elemento del array
          nombre: item[1] // El nombre es el segundo elemento
        }));
      })
    );
  }


  // Método para obtener el resumen de clientes.
  getClientesSummary(year: number, month?: number | null): Observable<any> {
    let params = new HttpParams().set('year', year.toString());
    if (month) {
      params = params.set('month', month.toString());
    }
    return this.http.get<ApiResponse<any>>(`${this.apiUrl}/clientes-summary`, { params }).pipe(
      map(response => this.extractSingleData(response))
    );
  }

  // Método para obtener solo la lista de años en proyectos.
  getAvailableYears(): Observable<number[]> {
    return this.http.get<ApiResponse<number[]>>(`${this.apiUrl}/years`).pipe(
      map(response => this.extractSingleData(response))
    );
  }
}