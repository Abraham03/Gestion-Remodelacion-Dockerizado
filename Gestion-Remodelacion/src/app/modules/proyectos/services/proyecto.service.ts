// src/app/modules/proyectos/services/proyectos.service.ts
import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { delay, map, Observable, tap } from 'rxjs';
import { Proyecto, ProyectoDropdown } from '../models/proyecto.model';
import { environment } from '../../../../environments/environment';
import { Page } from '../../../core/models/page.model';
import { DropdownItem } from '../../../core/models/dropdown-item.model';
import { BaseService } from '../../../core/services/base.service';
import { ApiResponse } from '../../../core/models/ApiResponse';
// Imports de Akita
import { ProyectosStore } from '../state/proyecto.store';
import { ProyectosQuery } from '../state/proyecto.query';

@Injectable({
  providedIn: 'root',
})
export class ProyectosService extends BaseService<Proyecto> {

  constructor(
    http: HttpClient,
    private proyectosStore: ProyectosStore,
    private proyectosQuery: ProyectosQuery
  ) {
    super(http, `${environment.apiUrl}/proyectos`);
  }

    getApiUrl(): string {
    return this.apiUrl;
  }  

  getProyectosPaginated(
    page: number = 0,
    size: number = 5,
    filter: string = '',
    sort: string = 'nombreProyecto,asc'
  ): Observable<Page<Proyecto>> {
    // Actualizar el estado de carga
    this.proyectosStore.setLoading(true);

    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('sort', sort);
    if (filter) {
      params = params.set('filter', filter);
    }
    return this.http.get<ApiResponse<Page<Proyecto>>>(this.apiUrl, { params }).pipe(
      map(response => this.extractPageData(response)),
      // Se agrega un delay(0) antes de actualizar el store y loading = false
      delay(0),
      tap(response => {
        // En lugar de retornar los datos, se guardan en el store de Akita
        this.proyectosStore.set(response.content);

        // Actualiza la informacion de paginacion en el store
        this.proyectosStore.update({
          pagination: {
            totalElements: response.totalElements,
            totalPages: response.totalPages,
            currentPage: response.number, // El índice de la página actual (base 0)
            pageSize: response.size,
          },
        });

        // Se desactiva el estado de carga
        this.proyectosStore.setLoading(false);
      })
    );
  }

  // Nuevo método para obtener una lista simplificada de proyectos para dropdowns
  getProyectosForDropdown(): Observable<ProyectoDropdown[]> {
    return this.http
      .get<ApiResponse<ProyectoDropdown[]>>(`${this.apiUrl}/dropdown`).pipe(
        map(response => this.extractSingleData(response))
      );
  }

   getProyectoById(id: number): Observable<Proyecto> {
    // 1. Informa al store que estamos cargando (para la entidad activa)
    this.proyectosStore.setLoading(true);

    return this.http.get<ApiResponse<Proyecto>>(`${this.apiUrl}/${id}`).pipe(
      map(response => this.extractSingleData(response)),
      tap(proyectoEncontrado => {
        /**
         * BUENA PRÁCTICA: Usamos upsert()
         * - Si el proyecto ya existe en el store, lo actualiza.
         * - Si no existe, lo añade.
         * Esto mantiene la "caché" del store fresca.
         */
        this.proyectosStore.upsert(proyectoEncontrado.id, proyectoEncontrado);
        
        // 2. Marcamos este proyecto como "activo" en el store.
        // Un componente (ej. el form) puede suscribirse a 'proyectosQuery.selectActive()'
        this.proyectosStore.setActive(proyectoEncontrado.id);
        
        // 3. Dejamos de cargar
        this.proyectosStore.setLoading(false);
      })
    );
  }

  addProyecto(proyecto: Proyecto): Observable<Proyecto> {
    return this.http.post<ApiResponse<Proyecto>>(this.apiUrl, proyecto).pipe(
      map(response => this.extractSingleData(response)),
      tap(nuevoProyecto => {
        // Añadimos la nueva entidad al store
        this.proyectosStore.add(nuevoProyecto);
      })
    );
  }

  updateProyecto(proyecto: Proyecto): Observable<Proyecto> {
    return this.http.put<ApiResponse<Proyecto>>(`${this.apiUrl}/${proyecto.id}`, proyecto).pipe(
      map(response => this.extractSingleData(response)),
      tap(proyectoActualizado => {
        // Actualizamos la entidad en el store
        this.proyectosStore.update(proyecto.id, proyectoActualizado);
      })
    );
  }

  deleteProyecto(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`).pipe(
      tap(() => {
        // Eliminamos la entidad del store
        this.proyectosStore.remove(id);
      })
    );
  }
}
