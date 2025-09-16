// src/app/modules/empresas/services/empresa.service.ts

import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../../../environments/environment';
import { Page } from '../../../core/models/page.model';
import { Empresa, EmpresaDropdown } from '../model/Empresa';
import { ApiResponse } from '../../../core/models/ApiResponse';
import { BaseService } from '../../../core/services/base.service';
import { DropdownItem } from '../../../core/models/dropdown-item.model';

@Injectable({
  providedIn: 'root',
})
export class EmpresaService extends BaseService<Empresa> {
  constructor(http: HttpClient) {
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
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('sort', sort);

    if (filter) {
      params = params.set('filter', filter);
    }

    return this.http.get<ApiResponse<Page<Empresa>>>(this.apiUrl, { params }).pipe(
      map(response => this.extractPageData(response))
    );
  }

  public getEmpresa(id: number): Observable<Empresa> {
    return this.http.get<Empresa>(`${this.apiUrl}/${id}`);
  }

  public createEmpresa(empresa: Empresa): Observable<Empresa> {
    return this.http.post<Empresa>(this.apiUrl, empresa);
  }

  public updateEmpresa(id: number, empresa: Empresa): Observable<Empresa> {
    return this.http.put<Empresa>(`${this.apiUrl}/${id}`, empresa);
  }

  // El super usuario probablemente no elimina, sino que desactiva.
  // Creamos un método para cambiar el estado.
  public changeStatus(id: number, activo: boolean): Observable<void> {
    const params = new HttpParams().set('activo', activo.toString());
    return this.http.patch<void>(`${this.apiUrl}/${id}/status`, null, { params });
  }

  /**
   * MÉTODO PARA SUBIR EL LOGO
   * @param id El ID de la empresa
   * @param file El archivo de imagen a subir
   */
  public uploadLogo(id: number, file: File): Observable<string> {
    const formData = new FormData();
    formData.append('file', file, file.name);

    // Hacemos un POST al nuevo endpoint: /api/empresas/{id}/logo
    return this.http.post(`${this.apiUrl}/${id}/logo`, formData, { responseType: 'text' });
  }  

  getAllEmpresasForForm(): Observable<EmpresaDropdown[]> {
    const params = new HttpParams()
      .set('page', '0')
      .set('size', '1000'); // Pedimos hasta 1000 empresas

    return this.http.get<ApiResponse<Page<Empresa>>>(this.apiUrl, { params }).pipe(
      map(response => {
        const page = this.extractPageData(response);
        // Mapeamos el contenido para que coincida con el modelo EmpresaDropdown
        return page.content.map(empresa => ({
          id: empresa.id!,
          nombreEmpresa: empresa.nombreEmpresa
        }));
      })
    );
  }
}