// src/app/modules/empleados/services/empleado.service.ts
import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map, tap } from 'rxjs/operators';
import { Empleado } from '../models/empleado.model'; // Assuming Empleado model exists
import { environment } from '../../../../environments/environment';
import { Page } from '../../../core/models/page.model'; // Assuming Page model exists
import { ApiResponse } from '../../../core/models/ApiResponse';
import { BaseService } from '../../../core/services/base.service';
import { dropdownItemModeloHorastrabajadas } from '../../../core/models/dropdown-item-modelo-horastrabajadas';
// imports de akita
import { EmpleadosStore } from '../state/empleados.store';
import { EmpleadosQuery } from '../state/empleados.query';

@Injectable({
  providedIn: 'root',
})
export class EmpleadoService extends BaseService<Empleado> {

  constructor(
    http: HttpClient,
    private empleadosStore: EmpleadosStore,
    private empleadosQuery: EmpleadosQuery
  ) {
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
    // Actualizar el estado de carga
    this.empleadosStore.setLoading(true);

    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('sort', sort);
    if (filter) {
      params = params.set('filter', filter);
    }
    return this.http.get<ApiResponse<Page<Empleado>>>(this.apiUrl, { params }).pipe(
      map(response => this.extractPageData(response)),
      tap(pageResponse => {
        // En lugar de retornar los datos, se guardan en el store de Akita
        this.empleadosStore.set(pageResponse.content);

        // Actualiza la informacion de paginacion en el store
        this.empleadosStore.update({
          pagination: {
            totalElements: pageResponse.totalElements,
            totalPages: pageResponse.totalPages,
            currentPage: pageResponse.number, // El índice de la página actual (base 0)
            pageSize: pageResponse.size,
          }
        });

        // Se desactiva el estado de carga
        this.empleadosStore.setLoading(false);
      })
    );
  }

getEmpleadosForDropdown(): Observable<dropdownItemModeloHorastrabajadas[]> {
    
    return this.http
      .get<ApiResponse<dropdownItemModeloHorastrabajadas[]>>(`${this.apiUrl}/dropdown`).pipe(
        // Usamos 'extractSingleData' que maneja de forma segura
        // si la respuesta ya fue desenvuelta por el interceptor o no.
        map(response => this.extractSingleData(response))
      );
  }

  getEmpleado(id: number): Observable<Empleado> {
    // 1. Informa al store que estamos cargando (para la entidad activa)
    this.empleadosStore.setLoading(true);

    return this.http.get<ApiResponse<Empleado>>(`${this.apiUrl}/${id}`).pipe(
      map(response => this.extractSingleData(response)),
      tap(empleadoEncontrado => {
        /**
         * BUENA PRÁCTICA: Usamos upsert()
         * - Si el empleado ya existe en el store, lo actualiza.
         * - Si no existe, lo añade.         
         * Esto mantiene la "caché" del store fresca.
         */
        this.empleadosStore.upsert(empleadoEncontrado.id, empleadoEncontrado);
        
        // 2. Marcamos este empleado como "activo" en el store.
        // Un componente (ej. el form) puede suscribirse a 'empleadosQuery.selectActive()'
        this.empleadosStore.setActive(empleadoEncontrado.id);
        
        // 3. Dejamos de cargar
        this.empleadosStore.setLoading(false);
      })
    );
  }

  createEmpleado(empleado: Empleado): Observable<Empleado> {
    return this.http.post<ApiResponse<Empleado>>(this.apiUrl, empleado).pipe(
      map(response => this.extractSingleData(response)),
      tap(nuevoEmpleado => {
        // Añadimos la nueva entidad al store
        this.empleadosStore.add(nuevoEmpleado);
      })
    );
  }

  updateEmpleado(empleado: Empleado): Observable<Empleado> {
    return this.http.put<ApiResponse<Empleado>>(`${this.apiUrl}/${empleado.id}`, empleado).pipe(
      map(response => this.extractSingleData(response)),
      tap(empleadoActualizado => {
        // Actualizamos la entidad en el store
        this.empleadosStore.update(empleado.id, empleadoActualizado);
      })
    )
  }


  deleteEmpleado(id: number): Observable<any> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`).pipe(
      tap(() => {
        // Eliminamos la entidad del store
        this.empleadosStore.remove(id);
      })
    )
  }

  deactivateEmpleado(id: number, activo: boolean): Observable<any> {
    // Prepara los parametros para la API
    const params = new HttpParams().set('activo', activo.toString());
    // Realiza la llamada PATCH
    return this.http.patch<any>(`${this.apiUrl}/${id}/status`, { params }).pipe(
      tap(() => {
        // Actualizamos la entidad en el store
        this.empleadosStore.update(id, { activo: activo });
      })
    )
  }
}
