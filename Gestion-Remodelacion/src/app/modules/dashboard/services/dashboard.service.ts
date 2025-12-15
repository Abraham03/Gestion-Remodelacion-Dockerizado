import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { finalize, map, Observable, tap } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { ApiResponse } from '../../../core/models/ApiResponse';
import { BaseService } from '../../../core/services/base.service';
import { DropdownItem } from '../../../core/models/dropdown-item.model';
import { DashboardClientes, DashboardSummary } from '../models/dashboard.model';
import { DashboardStore } from '../state/dashboard.store';

@Injectable({
  providedIn: 'root'
})
export class DashboardService extends BaseService<any> {

  constructor(
    http: HttpClient,
    private dashboardStore: DashboardStore
  ) {
    super(http, `${environment.apiUrl}/dashboard`);
  }

  getApiUrl(): string {
    return this.apiUrl;
  }

// Carga los datos principales y actualiza el Store
  loadProyectosSummary(year: number, month?: number | null, projectId?: number | null): Observable<void> {
    this.dashboardStore.setLoading(true);

    let params = new HttpParams().set('year', year.toString());
    if (month) params = params.set('month', month.toString());
    if (projectId) params = params.set('projectId', projectId.toString());

    return this.http.get<ApiResponse<DashboardSummary>>(`${this.apiUrl}/summary`, { params }).pipe(
      map(response => this.extractSingleData(response)),
      tap(data => {
        this.dashboardStore.update({ summary: data });
      }),
      finalize(() => this.dashboardStore.setLoading(false)),
      map(() => void 0) // Retorna void
    );
  }


// Este método se queda igual, es auxiliar para un dropdown efímero
  getProjectsForFilter(year: number, month?: number | null): Observable<DropdownItem[]> {
    let params = new HttpParams().set('year', year.toString());
    if (month) params = params.set('month', month.toString());
    
    return this.http.get<ApiResponse<any>>(`${this.apiUrl}/proyectos`, { params }).pipe(
      map(response => {
        const rawData = this.extractSingleData(response);
        return rawData.map((item: any[]) => ({
          id: item[0], 
          nombre: item[1] 
        }));
      })
    );
  }


// Carga los datos de clientes y actualiza el Store
  loadClientesSummary(year: number, month?: number | null): Observable<void> {
    // Nota: No activamos loading global aquí para que sea independiente o paralelo
    let params = new HttpParams().set('year', year.toString());
    if (month) params = params.set('month', month.toString());

    return this.http.get<ApiResponse<DashboardClientes>>(`${this.apiUrl}/clientes-summary`, { params }).pipe(
      map(response => this.extractSingleData(response)),
      tap(data => {
        this.dashboardStore.update({ clientesSummary: data });
      }),
      map(() => void 0)
    );
  }

// Carga los años disponibles y actualiza el Store
  loadAvailableYears(): Observable<number[]> {
    return this.http.get<ApiResponse<number[]>>(`${this.apiUrl}/years`).pipe(
      map(response => this.extractSingleData(response)),
      tap(years => {
        this.dashboardStore.update({ availableYears: years });
      })
    );
  }
}