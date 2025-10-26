// src/app/modules/cliente/services/cliente.service.ts
import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map, tap, delay } from 'rxjs/operators';
import { environment } from '../../../../environments/environment';
import { Page } from '../../../core/models/page.model';
import { Cliente, ClienteDropdownResponse } from '../models/cliente.model'; // Assuming Cliente model exists
import { ApiResponse } from '../../../core/models/ApiResponse';
import { BaseService } from '../../../core/services/base.service';
import { ClientesStore } from '../state/cliente.store';

@Injectable({
  providedIn: 'root',
})
export class ClienteService extends BaseService<Cliente> {
  constructor(
    http: HttpClient,
    private clientesStore: ClientesStore
  ) {
    super(http, `${environment.apiUrl}/clientes`);
  }
  
  getApiUrl(): string {
    return this.apiUrl;
  }

  // If you need pagination and search for a full client list
  getClientes(
    page: number = 0,
    size: number = 5,
    filter: string = '',
    sort: string = 'nombreCliente,asc'
  ): Observable<Page<Cliente>> {
    // Activa el estado de carga
    this.clientesStore.setLoading(true);
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('sort', sort);
    if (filter) {
      params = params.set('filter', filter);
    }
    return this.http.get<ApiResponse<Page<Cliente>>>(this.apiUrl, { params }).pipe(
      map(response => this.extractPageData(response)),
      // Se agrega un delay(0) antes de actualizar el store y loading = false
      delay(0),
      tap(pageResponse => {
        // En lugar de retornar los datos, se guardan en el store de Akita
        this.clientesStore.set(pageResponse.content);

        // Actualiza la informacion de paginacion en el store
        this.clientesStore.update({
          pagination: {
            totalElements: pageResponse.totalElements,
            totalPages: pageResponse.totalPages,
            currentPage: pageResponse.number, // El índice de la página actual (base 0)
            pageSize: pageResponse.size,
          },
        });

        this.clientesStore.setLoading(false);
      })
    );
  }

  // Method to get all clients for a dropdown (only ID and name)
  getClientesForDropdown(): Observable<ClienteDropdownResponse[]> {
    return this.http
    .get<ApiResponse<ClienteDropdownResponse[]>>(`${this.apiUrl}/dropdown`)
    .pipe(
      // Usamos 'extractSingleData' que maneja de forma segura
      // si la respuesta ya fue desenvuelta por el interceptor o no.
      map(response => this.extractSingleData(response))
      );
  }

  getCliente(id: number): Observable<Cliente> {
    this.clientesStore.setLoading(true);
    // 1. Informa al store que estamos cargando (para la entidad activa)
    this.clientesStore.setLoading(true);

    return this.http.get<ApiResponse<Cliente>>(`${this.apiUrl}/${id}`).pipe(
      map(response => this.extractSingleData(response)),
      tap(clienteEncontrado => {
        // Buena práctica: Usamos upsert()
        // - Si el cliente ya existe en el store, lo actualiza.
        // - Si no existe, loañade.
        // Esto mantiene la "caché" del store fresca.
        this.clientesStore.upsert(clienteEncontrado.id, clienteEncontrado);
        // 2. Marcamos este cliente como "activo" en el store.
        // Un componente (ej. el form) puede suscribirse a 'clientesQuery.selectActive()'
        this.clientesStore.setActive(clienteEncontrado.id);
        // 3. Dejamos de cargar
        this.clientesStore.setLoading(false);
      })
    )
  }

  createCliente(cliente: Cliente): Observable<Cliente> {
    return this.http.post<ApiResponse<Cliente>>(this.apiUrl, cliente).pipe(
      map(response => this.extractSingleData(response)),
      tap(nuevoCliente => {
        // Añade la nueva entidad al store
        this.clientesStore.add(nuevoCliente);
      })
    );
  }

  updateCliente(id: number, cliente: Cliente): Observable<Cliente> {
    return this.http.put<ApiResponse<Cliente>>(`${this.apiUrl}/${cliente.id}`, cliente).pipe(
      map(response => this.extractSingleData(response)),
      tap(clienteActualizado => {
        // Actualiza la entidad en el store
        this.clientesStore.update(clienteActualizado.id!, clienteActualizado);
      })
    );
  }

  deleteCliente(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`).pipe(
      tap(() => {
        // 7. Elimina la entidad del store
        this.clientesStore.remove(id);
      })
    );
  }
}
