import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { Permission, PermissionDropdownResponse, PermissionRequest } from '../../../core/models/permission.model';
import { delay, map, tap } from 'rxjs/operators';
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

  // Metodo para obtener paginado en permission component list
  getPaginated(
    page: number, 
    size: number, 
    sort: string = 'name,asc'
  ): Observable<Page<Permission>> {
    // Actualizar el estado de carga
    this.permissionStore.setLoading(true);

    let  params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    return this.http.get<ApiResponse<Page<Permission>>>(this.apiUrl, { params }).pipe(
      map(response => this.extractPageData(response)),
      // Se agrega un delay(0) antes de actualizar el store y loading = false
      delay(0),
      tap(PageResponse => {
        // En lugar de retornar los datos, se guardan en el store de Akita
        this.permissionStore.set(PageResponse.content);

        // Actualiza la informacion de paginacion en el store
        this.permissionStore.update({
          pagination: {
            totalElements: PageResponse.totalElements,
            totalPages: PageResponse.totalPages,
            currentPage: PageResponse.number, // El índice de la página actual (base 0)
            pageSize: PageResponse.size,
          },
        });

        // Se desactiva el estado de carga
        this.permissionStore.setLoading(false);
      })
    );
  }  

  // Metodo para obtener permisos para PermissionDropdownResponse en Role-form
  getPermissionsForDropdown(): Observable<PermissionDropdownResponse[]> {
    return this.http.get<ApiResponse<PermissionDropdownResponse[]>>(`${this.apiUrl}/dropdown`).pipe(
      map(response => this.extractSingleData(response))
    );
  }

  createPermission(permission: Permission): Observable<Permission> {
    return this.http.post<ApiResponse<Permission>>(this.apiUrl, permission).pipe(
      map(response => this.extractSingleData(response)),
      tap(nuevoPermission =>{
        // Añadir la nueva entidad al Store
        this.permissionStore.add(nuevoPermission);
      })
    )
  }

  updatePermission(permission: Permission): Observable<Permission> {
    return this.http.put<ApiResponse<Permission>>(`${this.apiUrl}/${permission.id}`, permission).pipe(
      map(response => this.extractSingleData(response)),
      tap(permissionActualizado =>{
        // Actualizar la entidad en el store
        this.permissionStore.update(permission.id, permissionActualizado);
      })
    )
  }

  deletePermission(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`).pipe(
      tap(() => {
        // Eliminamos la entidad del store
        this.permissionStore.remove(id);
      })
    )
  }  

}
