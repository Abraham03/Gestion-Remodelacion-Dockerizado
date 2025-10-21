// src/app/modules/proyectos/services/proyectos.service.ts
import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { map, Observable, tap } from 'rxjs';
import { Proyecto } from '../models/proyecto.model';
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
    filter: string,
    sort: string = 'nombreProyecto,asc'
  ): Observable<Page<Proyecto>> {
    // Actualizar el estado de carga
    this.proyectosStore.setLoading(true);

    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('sort', sort); // Usar el nuevo parámetro

    if (filter) {
      params = params.set('filter', filter);
    }
    return this.http.get<ApiResponse<Page<Proyecto>>>(this.apiUrl, { params }).pipe(
      map(response => this.extractPageData(response)),
      tap(response => {
        // En lugar de retornar los datos, se guardan en el store de Akita
        this.proyectosStore.set(response.content);
        // Se desactiva el estado de carga
        this.proyectosStore.setLoading(false);
      })
    );
  }

  // Nuevo método para obtener una lista simplificada de proyectos para dropdowns
  getProyectosForDropdown(): Observable<DropdownItem[]> {
    const params = new HttpParams()
      .set('size', '1000')
      .set('sort', 'nombreProyecto,asc'); // Pide un tamaño grande y ordena

    return this.http
      .get<Page<Proyecto>>(`${this.apiUrl}`, { params })
      .pipe(
        map((page: Page<Proyecto>) =>
          page.content.map((proj: Proyecto) => ({
            id: proj.id!,
            nombre: proj.nombreProyecto,
          }))
        )
      );
  }

   getProyectoById(id: number): Observable<Proyecto> {
    // 1. Informa al store que estamos cargando (para la entidad activa)
    this.proyectosStore.setLoading(true);

    return this.http.get<Proyecto>(`${this.apiUrl}/${id}`).pipe(
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
    return this.http.post<Proyecto>(this.apiUrl, proyecto).pipe(
      tap(nuevoProyecto => {
        // Añadimos la nueva entidad al store
        this.proyectosStore.add(nuevoProyecto);
      })
    );
  }

  updateProyecto(proyecto: Proyecto): Observable<Proyecto> {
    return this.http.put<Proyecto>(`${this.apiUrl}/${proyecto.id}`, proyecto).pipe(
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
