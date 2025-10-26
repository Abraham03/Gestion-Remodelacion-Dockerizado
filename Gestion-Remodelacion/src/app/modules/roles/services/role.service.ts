import { Injectable } from '@angular/core';
import { environment } from '../../../../environments/environment';
import { HttpClient, HttpParams } from '@angular/common/http';
import { delay, map, Observable, tap } from 'rxjs';
import { Page } from '../../../core/models/page.model';
import { Role, RoleDropdownResponse, RoleRequest } from '../../../core/models/role.model';
import { ApiResponse } from '../../../core/models/ApiResponse';
import { BaseService } from '../../../core/services/base.service';
// imports de akita
import { RoleStore } from '../state/roles.store';
import { RoleQuery } from '../state/roles.query';

@Injectable({
  providedIn: 'root',
})
export class RoleService extends BaseService<Role>{

  constructor(
    http: HttpClient,
    private roleStore: RoleStore,
    private roleQuery: RoleQuery
  ) {
    super(http, `${environment.apiUrl}/roles`); // Llamar al constructor de la clase base
  }
  
  getApiUrl(): string {
    return this.apiUrl;
  }

  // Método para obtener los roles al modulo de roles
  getRoles(
    page: number,
    size: number,
    sortColumn: string,
    sortDirection: string,
    searchTerm: string
  ): Observable<Page<Role>> {
    // Actualiza el estado de carga
    this.roleStore.setLoading(true);

    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    params = params.set('sort', `${sortColumn},${sortDirection}`);
    if (searchTerm) {
      params = params.set('searchTerm', searchTerm);
    }

    return this.http.get<ApiResponse<Page<Role>>>(this.apiUrl, { params }).pipe(
      map((response => this.extractPageData(response))),
      // Se agrega un delay(0) antes de actualizar el store y loading = false
      delay(0),
      tap(pageResponse => {
        // En lugar de retornar los datos, se guardan en el store de Akita
        this.roleStore.set(pageResponse.content);

        // Actualiza la informacion de paginacion en el store
        this.roleStore.update({
          pagination: {
            totalElements: pageResponse.totalElements,
            totalPages: pageResponse.totalPages,
            currentPage: pageResponse.number, // El índice de la página actual (base 0)
            pageSize: pageResponse.size,
          },
        });

        // Actualiza el estado de carga
        this.roleStore.setLoading(false);
      })
    );
  }

 // Metodo para obtener todos los roles como Administrador de la empresa  
getDropdownRoles(empresaId?: number): Observable<RoleDropdownResponse[]> {
    const url = `${this.apiUrl}/dropdown-by-empresa`;
    let params = new HttpParams();

    // Añade el parámetro 'empresaId' si se proporciona
    if (empresaId) {
        params = params.set('empresaId', empresaId.toString());
    }

    return this.http.get<ApiResponse<RoleDropdownResponse[]>>(url, { params }).pipe(
        map(response => this.extractSingleData(response) || [])
    );
}

  getRoleById(id: number): Observable<Role> {
    // 1. Informa al store que estamos cargando (para la entidad activa)
    this.roleStore.setLoading(true);

    return this.http.get<ApiResponse<Role>>(`${this.apiUrl}/${id}`).pipe(
      map(response => this.extractSingleData(response)),
      tap(roleEncontrado => {
        // BUENA PRÁCTICA: Usamos upsert()
        // - Si el role ya existe en el store, lo actualiza.
        // - Si no existe, loañade.
        // Esto mantiene la "caché" del store fresca.
        this.roleStore.upsert(roleEncontrado.id, roleEncontrado);
        // 2. Marcamos este role como "activo" en el store.
        // Un componente (ej. el form) puede suscribirse a 'rolesQuery.selectActive()'
        this.roleStore.setActive(roleEncontrado.id);
        // 3. Dejamos de cargar
        this.roleStore.setLoading(false);
      })
    );
  }

  createRole(role: Role): Observable<Role> {
    return this.http.post<ApiResponse<Role>>(`${this.apiUrl}`, role).pipe(
        map(response => this.extractSingleData(response)),
        tap((nuevoRole) => {
          // Añadimos la nueva entidad al store
          this.roleStore.add(nuevoRole);
        })
      );
  }

  updateRole(role: Role): Observable<Role> {
    return this.http.put<ApiResponse<Role>>(`${this.apiUrl}/${role.id}`, role).pipe(
      map(response => this.extractSingleData(response)),
      tap((roleActualizado) => {
        // Actualizamos la entidad en el store
        this.roleStore.update(role.id, roleActualizado);
      })
    );
  }

  deleteRole(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`).pipe(
      tap(() => {
        // Eliminamos la entidad del store
        this.roleStore.remove(id);
      })
    )
  }
}
