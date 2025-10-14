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

getAllRolesForForm(empresaId?: number): Observable<Role[]> {
    let url = `${this.apiUrl}/all`;
    let params = new HttpParams();

    if (empresaId) {
        // Si se provee un empresaId, usamos el nuevo endpoint del SUPER_ADMIN
        url = `${this.apiUrl}/dropdown`;
        params = params.set('empresaId', empresaId.toString());
    }

    return this.http.get<ApiResponse<Role[]>>(url, { params }).pipe(
        map(response => this.extractSingleData(response) || [])
    );
}

  findAllForForm(): Observable<Role[]> {
    const params = new HttpParams()
      .set('page', '0')
      .set('size', '1000'); // Pedimos hasta 1000 roles, suficiente para un dropdown

    return this.http.get<ApiResponse<Page<Role>>>(this.apiUrl, { params }).pipe(
      map(response => {
        const page = this.extractPageData(response);
        return page.content; // Extraemos solo el contenido de la página
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
