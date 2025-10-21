import { EntityState, EntityStore, StoreConfig } from '@datorama/akita';
import { Permission } from '../../../core/models/permission.model';
import { Injectable } from '@angular/core';

// Define la estructura del estado, incluyendo el tipo de entidad
export interface PermissionState extends EntityState<Permission, number> {
    // Aquí puedes añadir otras propiedades de estado si las necesitas
    // Por ejemplo: filtroActivo: string;
}

@Injectable({ providedIn: 'root' })
@StoreConfig({ name: 'proyectos' }) // El nombre que verás en las DevTools
export class PermissionStore extends EntityStore<PermissionState> {
  constructor() {
    super(); // Inicializa el store
  }
}