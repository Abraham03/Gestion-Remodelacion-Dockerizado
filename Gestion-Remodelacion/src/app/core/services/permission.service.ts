import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Permission } from '../models/permission.model'; // Asegúrate de la ruta correcta
import { map } from 'rxjs/operators';
import { BaseService } from './base.service';

@Injectable({
  providedIn: 'root',
})
export class PermissionService extends BaseService<Permission> {
  constructor(http: HttpClient) {
    super(http, `${environment.apiUrl}/permissions`);
  }

  /**
   * Obtiene la lista completa de todos los permisos disponibles en el sistema.
   * Este método es utilizado principalmente por RoleFormComponent para mostrar
   * las opciones de permisos a asignar a un rol.
   */
  getAllPermissions(): Observable<Permission[]> {
    const params = new HttpParams()
      .set('size', '1000') // <-- Es mejor usar un tamaño grande para obtener todos los permisos
      .set('sort', 'name,asc');
    return this.http.get<any>(this.apiUrl, { params }).pipe(
      // 2. Usa el método extractPageData del servicio base
      map((response) => this.extractPageData(response).content)
    );
  }
}
