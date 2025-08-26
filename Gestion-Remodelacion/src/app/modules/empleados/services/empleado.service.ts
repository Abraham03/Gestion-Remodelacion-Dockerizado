// src/app/modules/empleados/services/empleado.service.ts
import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { Empleado } from '../models/empleado.model'; // Assuming Empleado model exists
import { environment } from '../../../../environments/environment';
import { Page } from '../../../core/models/page.model'; // Assuming Page model exists
import { ApiResponse } from '../../../core/models/ApiResponse';
import { BaseService } from '../../../core/services/base.service';

@Injectable({
  providedIn: 'root',
})
export class EmpleadoService extends BaseService<Empleado> {

  constructor(http: HttpClient) {
    super(http, `${environment.apiUrl}/empleados`);
  }

  getApiUrl(): string {
    return this.apiUrl;
  }  

  getEmpleados(
    page: number = 0,
    size: number = 5,
    filter: string = '',
    sort: string = 'nombreCompleto,asc'
  ): Observable<Page<Empleado>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('sort', sort);
    if (filter) {
      params = params.set('filter', filter);
    }
    return this.http.get<ApiResponse<Page<Empleado>>>(this.apiUrl, { params }).pipe(
      map(response => this.extractPageData(response))
    );
  }

  getEmpleadosForDropdown(): Observable<{ id: number; nombre: string }[]> {
    const params = new HttpParams().set('size', '1000'); 
    return this.http
      .get<Page<Empleado>>(`${this.apiUrl}`, { params })
      .pipe(
        map((response: Page<Empleado>) =>
          response.content.map((emp: Empleado) => ({
            id: emp.id!,
            nombre: emp.nombreCompleto,
          }))
        )
      );
  }

  getEmpleado(id: number): Observable<Empleado> {
    return this.http.get<Empleado>(`${this.apiUrl}/${id}`);
  }

  createEmpleado(empleado: Empleado): Observable<Empleado> {
    // Es buena práctica eliminar id si el backend lo autogenera
    const empleadoToSend = { ...empleado };
    delete empleadoToSend.id;
    return this.http.post<Empleado>(this.apiUrl, empleado);
  }

  updateEmpleado(id: number, empleado: Empleado): Observable<Empleado> {
    return this.http.put<Empleado>(`${this.apiUrl}/${id}`, empleado);
  }

  deactivateEmpleado(id: number): Observable<any> {
    return this.http.patch<any>(`${this.apiUrl}/${id}/status`, null, {
      params: new HttpParams().set('activo', 'false'),
    });
  }
}
