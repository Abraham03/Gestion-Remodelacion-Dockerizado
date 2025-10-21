import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { Permission, PermissionDropdownResponse, PermissionRequest } from '../../../core/models/permission.model';
import { map } from 'rxjs/operators';
import { BaseService } from '../../../core/services/base.service';
import { Page } from '../../../core/models/page.model';
import { ApiResponse } from '../../../core/models/ApiResponse';
// imports de akita
import { PermissionStore } from '../state/permission.store';
import { PermissionQuery } from '../state/permission.query';
@Injectable({
  providedIn: 'root',
})
export class PermissionService extends BaseService<Permission> {
  constructor(
    http: HttpClient,
    private permissionStore: PermissionStore,
    private permissionQuery: PermissionQuery
  ) {
    super(http, `${environment.apiUrl}/permissions`);
  }

/*
  getAllPermissions(): Observable<Permission[]> {
    const params = new HttpParams()
      .set('size', '1000') // <-- Es mejor usar un tamaño grande para obtener todos los permisos
      .set('sort', 'name,asc');
    return this.http.get<any>(this.apiUrl, { params }).pipe(
      // 2. Usa el método extractPageData del servicio base
      map((response) => this.extractPageData(response).content)
    );
  }
*/
  // Metodo para obtener paginado en permission component list
  getPaginated(page: number, size: number): Observable<Page<Permission>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    return this.http.get<any>(this.apiUrl, { params }).pipe(
      map(response => this.extractPageData(response))
    );
  }  

  createPermission(permission: PermissionRequest): Observable<Permission> {
    return this.http.post<ApiResponse<Permission>>(this.apiUrl, permission).pipe(map(res => res.data));
  }

  updatePermission(id: number, permission: PermissionRequest): Observable<Permission> {
    return this.http.put<ApiResponse<Permission>>(`${this.apiUrl}/${id}`, permission).pipe(map(res => res.data));
  }

  deletePermission(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }  

  // Metodo para obtener permisos para PermissionDropdownResponse en Role-form
  getPermissionsForDropdown(): Observable<PermissionDropdownResponse[]> {
    return this.http.get<ApiResponse<PermissionDropdownResponse[]>>(`${this.apiUrl}/dropdown`)
    .pipe(
      map(response => this.extractSingleData(response))
    );
  }

}
