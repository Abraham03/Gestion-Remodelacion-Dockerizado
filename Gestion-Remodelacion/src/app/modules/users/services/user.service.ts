/* src/app/modules/users/services/user.service.ts */
import { Injectable } from '@angular/core';
import { environment } from '../../../../environments/environment';
import { HttpClient, HttpParams } from '@angular/common/http';
import { delay, map, Observable, tap } from 'rxjs';
import { Page } from '../../../core/models/page.model';
import { User, UserRequest } from '../../../core/models/user.model';
import { BaseService } from '../../../core/services/base.service';
import { ApiResponse } from '../../../core/models/ApiResponse';
// imports de akita
import { UserStore } from '../state/users.store';
import { UserQuery } from '../state/users.query'; 

@Injectable({
  providedIn: 'root',
})
export class UserService extends BaseService<User> {
  constructor(
    http: HttpClient,
    private userStore: UserStore,
    private userQuery: UserQuery
  
  ) {
    super(http, `${environment.apiUrl}/users`);
  }


  getApiUrl(): string {
    return this.apiUrl;
  }

  getUsers(
    page: number = 0,
    size: number = 5,
    filter: string = ''
  ): Observable<Page<User>> {
    // Actualiza el estado de carga
    this.userStore.setLoading(true);

    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    if (filter) {
      params = params.set('filter', filter);
    }

    return this.http.get<ApiResponse<Page<User>>>(this.apiUrl, { params }).pipe(
      map(response => this.extractPageData(response)),
      // Se agrega un delay(0) antes de actualizar el store y loading = false
      delay(0),
      tap(pageResponse => {
        // En lugar de retornar los datos, se guardan en el store de Akita
        this.userStore.set(pageResponse.content);

        // Actualiza la informacion de paginacion en el store
        this.userStore.update({
          pagination: {
            totalElements: pageResponse.totalElements,
            totalPages: pageResponse.totalPages,
            currentPage: pageResponse.number, // El índice de la página actual (base 0)
            pageSize: pageResponse.size,
          },
        });

        // Actualiza el estado de carga
        this.userStore.setLoading(false);

      })
    );
  }

  getUserById(id: number): Observable<User> {
    // 1. Informa al store que estamos cargando (para la entidad activa)
    this.userStore.setLoading(true);

    return this.http.get<ApiResponse<User>>(`${this.apiUrl}/${id}`).pipe(
      map(response => this.extractSingleData(response)),
      tap(userEncontrado => {
        // BUENA PRÁCTICA: Usamos upsert()
        // - Si el user ya existe en el store, lo actualiza.
        // - Si no existe, loañade.
        // Esto mantiene la "caché" del store fresca.
        this.userStore.upsert(userEncontrado.id, userEncontrado);
        // 2. Marcamos este user como "activo" en el store.
        // Un componente (ej. el form) puede suscribirse a 'usersQuery.selectActive()'
        this.userStore.setActive(userEncontrado.id);
        // 3. Dejamos de cargar
        this.userStore.setLoading(false);
      })
    )
  }

  createUser(user: UserRequest): Observable<User> {
    return this.http.post<ApiResponse<User>>(this.apiUrl, user).pipe(
      map(response => this.extractSingleData(response)),
      tap((nuevoUser) => {
        // Añadimos la nueva entidad al store
        this.userStore.add(nuevoUser);
      })
    );
  }

  updateUser(id: number, user: UserRequest): Observable<User> {
    return this.http.put<ApiResponse<User>>(`${this.apiUrl}/${id}`, user).pipe(
      map(response => this.extractSingleData(response)),
      tap((userActualizado) => {
        // Actualizamos la entidad en el store
        this.userStore.update(id, userActualizado);
      })
    );
  }

  deleteUser(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`).pipe(
      tap(() => {
        // Eliminamos la entidad del store
        this.userStore.remove(id);
      })
    );
  }  

}
