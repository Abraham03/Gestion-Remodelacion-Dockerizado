// src/app/modules/empresas/services/empresa.service.ts

import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { delay, map, tap } from 'rxjs/operators';
import { environment } from '../../../../environments/environment';
import { Page } from '../../../core/models/page.model';
import { Empresa, EmpresaDropdown } from '../model/Empresa';
import { ApiResponse } from '../../../core/models/ApiResponse';
import { BaseService } from '../../../core/services/base.service';
// imports de akita
import { EmpresaStore } from '../state/empresas.store';
import { EmpresaQuery } from '../state/empresas.query';

@Injectable({
  providedIn: 'root',
})
export class EmpresaService extends BaseService<Empresa> {
  constructor(
    http: HttpClient,
    private empresaStore: EmpresaStore,
    private empresaQuery: EmpresaQuery
  ) {
    // Asumimos que el endpoint en el backend será /api/empresas
    super(http, `${environment.apiUrl}/empresas`);
  }

  public getApiUrl(): string {
    return this.apiUrl;
  }

  public getEmpresas(
    page: number = 0,
    size: number = 5,
    filter: string = '',
    sort: string = 'nombreEmpresa,asc'
  ): Observable<Page<Empresa>> {
    // Actualizar el estado de carga
    this.empresaStore.setLoading(true);

    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('sort', sort);

    if (filter) {
      params = params.set('filter', filter);
    }

    return this.http.get<ApiResponse<Page<Empresa>>>(this.apiUrl, { params }).pipe(
      map(response => this.extractPageData(response)),
      // Se agrega un delay(0) antes de actualizar el store y loading = false
      delay(0),
      tap(PageResponse => {
        // En lugar de retornar los datos, se guardan en el store de Akita
        this.empresaStore.set(PageResponse.content);

        // Actualiza la informacion de paginacion en el store
        this.empresaStore.update({
          pagination: {
            totalElements: PageResponse.totalElements,
            totalPages: PageResponse.totalPages,
            currentPage: PageResponse.number, // El índice de la página actual (base 0)
            pageSize: PageResponse.size,
          },
        });

        // Se desactiva el estado de carga
        this.empresaStore.setLoading(false);
      })
    );
  }

  public getEmpresa(id: number): Observable<Empresa> {
    // 1. Informa al store que estamos cargando (para la entidad activa)
    this.empresaStore.setLoading(true);

    return this.http.get<ApiResponse<Empresa>>(`${this.apiUrl}/${id}`).pipe(
      map(response => this.extractSingleData(response)),
      tap(empresaEncontrada => {
        // BUENA PRÁCTICA: Usamos upsert()
        // - Si la empresa ya existe en el store, la actualiza.
        // - Si no existe, laañade.
        // Esto mantiene la "caché" del store fresca.
        this.empresaStore.upsert(empresaEncontrada.id, empresaEncontrada);
        // 2. Marcamos esta empresa como "activa" en el store.
        // Un componente (ej. el form) puede suscribirse a 'empresasQuery.selectActive()'
        this.empresaStore.setActive(empresaEncontrada.id);
        // 3. Dejamos de cargar
        this.empresaStore.setLoading(false);
      })
    )
  }

  public createEmpresa(empresa: Empresa): Observable<Empresa> {
    return this.http.post<ApiResponse<Empresa>>(this.apiUrl, empresa).pipe(
      map(response => this.extractSingleData(response)),
      tap(nuevaEmpresa => {
        // Añadimos la nueva entidad al store
        this.empresaStore.add(nuevaEmpresa);
      })
    )
  }

  public updateEmpresa(id: number, empresa: Empresa): Observable<Empresa> {
    return this.http.put<ApiResponse<Empresa>>(`${this.apiUrl}/${id}`, empresa).pipe(
      map(response => this.extractSingleData(response)),
      tap(empresaActualizada => {
        // Actualizamos la entidad en el store
        this.empresaStore.update(id, empresaActualizada);
      })
    )
  }

  public changeStatus(id: number, activo: boolean): Observable<void> {
    // Prepara los parametros para la API
    const params = new HttpParams().set('activo', activo.toString());
    // Realiza la llamada PATCH
    return this.http.patch<void>(`${this.apiUrl}/${id}/status`, null, { params }).pipe(
      tap(() => {
        // Actualizamos la entidad en el store
        this.empresaStore.update(id, { activo: activo });
      })
    )
  }

  /**
   * MÉTODO PARA SUBIR EL LOGO
   * @param id El ID de la empresa
   * @param file El archivo de imagen a subir
   */
  public uploadLogo(id: number, file: File): Observable<string> {
    // Prepara el FormData para la subida del archivo
    const formData = new FormData();
    formData.append('file', file, file.name);

    // Realiza la llamada POST al endpoint del logo
    return this.http.post(`${this.apiUrl}/${id}/logo`, formData, { responseType: 'text' }).pipe(
      tap(logoUrl => {        
        // Actualizamos la entidad en el store
        this.empresaStore.update(id, { logoUrl: logoUrl });
      })
    );
  }  

  getAllEmpresasForForm(): Observable<EmpresaDropdown[]> {
    return this.http
    .get<ApiResponse<EmpresaDropdown[]>>(`${this.apiUrl}/dropdown`).pipe(
      // Usamos 'extractSingleData' que maneja de forma segura
      // si la respuesta ya fue desenvuelta por el interceptor o no.
      map(response => this.extractSingleData(response))
    );
  }
}