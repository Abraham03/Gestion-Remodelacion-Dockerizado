import { Injectable } from '@angular/core';
import { environment } from '../../../../environments/environment';
import { HttpClient, HttpParams } from '@angular/common/http';
import { HorasTrabajadas } from '../models/horas-trabajadas';
import { delay, map, Observable, tap } from 'rxjs';
import { Page } from '../../../core/models/page.model';
import { BaseService } from '../../../core/services/base.service';
import { ApiResponse } from '../../../core/models/ApiResponse';
// Imports de Akita
import { HorasTrabajadasStore } from '../state/horas-trabajadas.store';
import { HorasTrabajadasQuery } from '../state/horas-trabajadas.query';

@Injectable({
  providedIn: 'root',
})
export class HorasTrabajadasService  extends BaseService<HorasTrabajadas> {

  constructor(
    http: HttpClient,
    private horasTrabajadasStore: HorasTrabajadasStore,
    private horasTrabajadasQuery: HorasTrabajadasQuery
  ) {
    super(http, `${environment.apiUrl}/horas-trabajadas`);
  }
 getApiUrl(): string {
    return this.apiUrl;
  }  

  /**
   * Obtiene una página de registros de horas trabajadas con opciones de paginación,
   * ordenamiento y filtro.
   */
  getHorasTrabajadasPaginated(
    page: number,
    size: number,
    filter: string,
    sort: string = 'fecha,desc',
  ): Observable<Page<HorasTrabajadas>> {
    // Actualizar el estado de carga
    this.horasTrabajadasStore.setLoading(true);

    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('sort', `${sort}`);
    if (filter) {
      params = params.set('filter', filter); 
    }
    return this.http.get<ApiResponse<Page<HorasTrabajadas>>>(this.apiUrl, { params }).pipe(
      map(response => this.extractPageData(response)),
      // Se agrega un delay(0) antes de actualizar el store y loading = false
      delay(0),
      tap(PageResponse => {
        // En lugar de retornar los datos, se guardan en el store de Akita
        this.horasTrabajadasStore.set(PageResponse.content);

        // Actualiza la informacion de paginacion en el store
        this.horasTrabajadasStore.update({
          pagination: {
            totalElements: PageResponse.totalElements,
            totalPages: PageResponse.totalPages,
            currentPage: PageResponse.number, // El índice de la página actual (base 0)
            pageSize: PageResponse.size,
          },
        });

        // Se desactiva el estado de carga
        this.horasTrabajadasStore.setLoading(false);  
      })
  );
}

  /**
   * Obtiene un registro de horas trabajadas por su ID.
   */
  getHorasTrabajadasById(id: number): Observable<HorasTrabajadas> {
    // 1. Informa al store que estamos cargando (para la entidad activa)
    this.horasTrabajadasStore.setLoading(true);

    return this.http.get<ApiResponse<HorasTrabajadas>>(`${this.apiUrl}/${id}`).pipe(
      map(response => this.extractSingleData(response)),
      tap(horasTrabajadasEncontrada => {
        // BUENA PRÁCTICA: Usamos upsert()
        // - Si la horas trabajadas ya existe en el store, la actualiza.
        // - Si no existe, laañade.
        // Esto mantiene la "caché" del store fresca.
        this.horasTrabajadasStore.upsert(horasTrabajadasEncontrada.id, horasTrabajadasEncontrada);
        // 2. Marcamos esta horas trabajadas como "activa" en el store.
        // Un componente (ej. el form) puede suscribirse a 'horasTrabajadasQuery.selectActive()'
        this.horasTrabajadasStore.setActive(horasTrabajadasEncontrada.id);
        // 3. Dejamos de cargar
        this.horasTrabajadasStore.setLoading(false);
      })
    )
  }

  /**
   * Crea un nuevo registro de horas trabajadas.
   */
  addHorasTrabajadas(horasTrabajadas: HorasTrabajadas): Observable<HorasTrabajadas> {
    return this.http.post<ApiResponse<HorasTrabajadas>>(this.apiUrl, horasTrabajadas).pipe(
      map(response => this.extractSingleData(response)),
      tap(nuevoHorasTrabajadas => {
        // Añadimos el nuevo registro al store
        this.horasTrabajadasStore.add(nuevoHorasTrabajadas);
      })
    );
  }

  /**
   * Actualiza un registro de horas trabajadas existente.
   */
  updateHorasTrabajadas(horasTrabajadas: HorasTrabajadas): Observable<HorasTrabajadas> {
    return this.http.put<ApiResponse<HorasTrabajadas>>(`${this.apiUrl}/${horasTrabajadas.id}`,horasTrabajadas).pipe(
      map(response => this.extractSingleData(response)),
      tap(actualizadoHorasTrabajadas => {
        // Actualizamos el registro en el store
        this.horasTrabajadasStore.update(actualizadoHorasTrabajadas.id, actualizadoHorasTrabajadas);
      })
    );
  }

  /**
   * Elimina un registro de horas trabajadas por su ID.
   */
  deleteHorasTrabajadas(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`).pipe(
      tap(() => {
        // Eliminamos el registro del store
        this.horasTrabajadasStore.remove(id);
      })
    )
  }
}
