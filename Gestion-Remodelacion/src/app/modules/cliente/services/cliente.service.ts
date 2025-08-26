// src/app/modules/cliente/services/cliente.service.ts
import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../../../environments/environment';
import { Page } from '../../../core/models/page.model';
import { Cliente } from '../models/cliente.model'; // Assuming Cliente model exists
import { ApiResponse } from '../../../core/models/ApiResponse';
import { BaseService } from '../../../core/services/base.service';

@Injectable({
  providedIn: 'root',
})
export class ClienteService extends BaseService<Cliente> {
  constructor(http: HttpClient) {
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
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('sort', sort);
    if (filter) {
      params = params.set('filter', filter);
    }
    return this.http.get<ApiResponse<Page<Cliente>>>(this.apiUrl, { params }).pipe(
      map(response => this.extractPageData(response))
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
    return this.http.get<Cliente>(`${this.apiUrl}/${id}`);
  }

  createCliente(cliente: Cliente): Observable<Cliente> {
    return this.http.post<Cliente>(this.apiUrl, cliente);
  }

  updateCliente(id: number, cliente: Cliente): Observable<Cliente> {
    return this.http.put<Cliente>(`${this.apiUrl}/${id}`, cliente);
  }

  deleteCliente(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
