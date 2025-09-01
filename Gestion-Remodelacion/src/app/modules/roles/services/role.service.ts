import { Injectable } from '@angular/core';
import { environment } from '../../../../environments/environment';
import { HttpClient, HttpParams } from '@angular/common/http';
import { map, Observable } from 'rxjs';
import { Page } from '../../../core/models/page.model';
import { Role, RoleRequest } from '../../../core/models/role.model';
import { ApiResponse } from '../../../core/models/ApiResponse';
import { BaseService } from '../../../core/services/base.service';

@Injectable({
  providedIn: 'root',
})
export class RoleService extends BaseService<Role>{

  constructor(http: HttpClient) {
    super(http, `${environment.apiUrl}/roles`); // Llamar al constructor de la clase base
  } // Helper method to safely extract data from an ApiResponse wrapper or return the object directly. // We add the 'extends object' constraint to ensure T is an object.

  getRoles(
    page: number,
    size: number,
    sortColumn: string,
    sortDirection: string,
    searchTerm: string
  ): Observable<Page<Role>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    params = params.set('sort', `${sortColumn},${sortDirection}`);
    if (searchTerm) {
      params = params.set('searchTerm', searchTerm);
    }

    return this.http.get<ApiResponse<Page<Role>>>(this.apiUrl, { params }).pipe(
      map((response => this.extractPageData(response)))
    );
  }

  getAllRolesForForm(): Observable<Role[]> {
    const pageableParams = new HttpParams().set('page', '0').set('size', '100');
    return this.http
      .get<ApiResponse<Page<Role>>>(this.apiUrl, { params: pageableParams })
      .pipe(
        map((response) => {
          const processedPage = this.extractPageData(response);
          return processedPage?.content ?? [];
        })
      );
  }

  getRoleById(id: number): Observable<Role> {
    // Aquí también asumimos que la respuesta es ApiResponse<Role> y extraemos 'data'
    return this.http
      .get<ApiResponse<Role>>(`${this.apiUrl}/${id}`)
      .pipe(map((response) => response.data as Role));
  }

  createRole(role: RoleRequest): Observable<Role> {
    return this.http
      .post<ApiResponse<Role>>(`${this.apiUrl}`, role)
      .pipe(map((response) => response.data as Role));
  }

  updateRole(id: number, role: RoleRequest): Observable<Role> {
    return this.http
      .put<ApiResponse<Role>>(`${this.apiUrl}/${id}`, role)
      .pipe(map((response) => response.data as Role));
  }

  deleteRole(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
