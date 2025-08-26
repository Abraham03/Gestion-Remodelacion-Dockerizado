import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { map, Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { DashboardSummary } from '../../../core/models/dashboard-summary.model';
import { DashboardProyecto } from '../models/dashboard-proyecto.model'; // <-- Asegúrate que esta ruta sea correcta y esté importado
import { ApiResponse } from '../../../core/models/ApiResponse';
import { BaseService } from '../../../core/services/base.service';

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

   getProyectosSummary(year: number, month?: number | null): Observable<any> {
    let params = new HttpParams().set('year', year.toString());
    
    if (month) {
      params = params.set('month', month.toString());
    }
    
    // ✅ La clave está aquí: .pipe(map(...)) asegura que el componente reciba solo la data.
    return this.http.get<ApiResponse<any>>(`${this.apiUrl}/summary`, { params }).pipe(
      map(response => this.extractSingleData(response))
    );
  }


    getClientesSummary(year: number, month?: number | null): Observable<any> {
    let params = new HttpParams().set('year', year.toString());
    if (month) {
      params = params.set('month', month.toString());
    }
    return this.http.get<ApiResponse<any>>(`${this.apiUrl}/clientes-summary`, { params }).pipe(
      map(response => this.extractSingleData(response))
    );
  }

  // ✅ CAMBIO: Nuevo método para obtener solo la lista de años.
  getAvailableYears(): Observable<number[]> {
    return this.http.get<ApiResponse<number[]>>(`${this.apiUrl}/years`).pipe(
      map(response => this.extractSingleData(response))
    );
  }  
}