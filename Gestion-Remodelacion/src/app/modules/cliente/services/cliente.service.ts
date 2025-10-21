// src/app/modules/cliente/services/cliente.service.ts
import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map, tap } from 'rxjs/operators';
import { environment } from '../../../../environments/environment';
import { Page } from '../../../core/models/page.model';
import { Cliente } from '../models/cliente.model'; // Assuming Cliente model exists
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
      tap(response => {
        this.clientesStore.set(response.content);
        this.clientesStore.setLoading(false);
      })
    );
  }

  // Method to get all clients for a dropdown (only ID and name)
  getClientesForDropdown(): Observable<{ id: number; nombre: string }[]> {
    const params = new HttpParams().set('size', '1000'); // Request a large number of items
    return this.http
      .get<Page<Cliente>>(`${this.apiUrl}`, { params })
      .pipe(
        // <-- Type as Page<Cliente> here
        map((response: Page<Cliente>) =>
          response.content.map((cliente: Cliente) => ({
            id: cliente.id!,
            nombre: cliente.nombreCliente,
          }))
        )
      );
  }

  getCliente(id: number): Observable<Cliente> {
    this.clientesStore.setLoading(true);
    return this.http.get<Cliente>(`${this.apiUrl}/${id}`).pipe(
      tap(clienteEncontrado => {
        this.clientesStore.upsert(clienteEncontrado.id!, clienteEncontrado);
        this.clientesStore.setActive(clienteEncontrado.id!);
        this.clientesStore.setLoading(false);
      })
    )
  }

  createCliente(cliente: Cliente): Observable<Cliente> {
    return this.http.post<Cliente>(this.apiUrl, cliente).pipe(
      tap(nuevoCliente => {
        // Añade la nueva entidad al store
        this.clientesStore.add(nuevoCliente);
      })
    );
  }

  updateCliente(id: number, cliente: Cliente): Observable<Cliente> {
    return this.http.put<Cliente>(`${this.apiUrl}/${cliente.id}`, cliente).pipe(
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
