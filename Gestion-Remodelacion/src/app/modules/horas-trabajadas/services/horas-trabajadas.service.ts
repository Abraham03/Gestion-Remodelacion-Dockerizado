import { Injectable } from '@angular/core';
import { environment } from '../../../../environments/environment';
import { HttpClient, HttpParams } from '@angular/common/http';
import { HorasTrabajadas } from '../models/horas-trabajadas';
import { map, Observable } from 'rxjs';
import { Page } from '../../../core/models/page.model';
import { BaseService } from '../../../core/services/base.service';
import { ApiResponse } from '../../../core/models/ApiResponse';

@Injectable({
  providedIn: 'root',
})
export class HorasTrabajadasService  extends BaseService<HorasTrabajadas> {

  constructor(http: HttpClient) {
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
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('sort', `${sort}`);
    if (filter) {
      params = params.set('filter', filter); 
    }
    return this.http.get<ApiResponse<Page<HorasTrabajadas>>>(this.apiUrl, { params }).pipe(
      map(response => this.extractPageData(response))
  );
}

  /**
   * Obtiene un registro de horas trabajadas por su ID.
   */
  getHorasTrabajadasById(id: number): Observable<HorasTrabajadas> {
    return this.http.get<HorasTrabajadas>(`${this.apiUrl}/${id}`);
  }

  /**
   * Crea un nuevo registro de horas trabajadas.
   * Se eliminan campos que el backend debe gestionar (id, nombres, fechaRegistro).
   */
  addHorasTrabajadas(
    horasTrabajadas: HorasTrabajadas
  ): Observable<HorasTrabajadas> {
    const dataToSend: Partial<HorasTrabajadas> = { ...horasTrabajadas };
    delete dataToSend.id;
    delete dataToSend.nombreEmpleado;
    delete dataToSend.nombreProyecto;
    delete dataToSend.fechaRegistro; // El backend asigna la fecha de registro

    return this.http.post<HorasTrabajadas>(this.apiUrl, dataToSend);
  }

  /**
   * Actualiza un registro de horas trabajadas existente.
   * Se eliminan campos que el backend debe gestionar (nombres, fechaRegistro).
   */
  updateHorasTrabajadas(
    horasTrabajadas: HorasTrabajadas
  ): Observable<HorasTrabajadas> {
    const dataToSend: Partial<HorasTrabajadas> = { ...horasTrabajadas };
    // 'id' se necesita en la URL, no se borra
    delete dataToSend.nombreEmpleado;
    delete dataToSend.nombreProyecto;
    delete dataToSend.fechaRegistro;

    return this.http.put<HorasTrabajadas>(
      `${this.apiUrl}/${horasTrabajadas.id}`,
      dataToSend
    );
  }

  /**
   * Elimina un registro de horas trabajadas por su ID.
   */
  deleteHorasTrabajadas(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
