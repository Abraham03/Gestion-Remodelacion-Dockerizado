import { EntityState, EntityStore, StoreConfig } from '@datorama/akita';
import { Permission } from '../../../core/models/permission.model';
import { Injectable } from '@angular/core';

// Define la estructura del estado, incluyendo el tipo de entidad
export interface PermissionState extends EntityState<Permission, number> {
    // Aquí puedes añadir otras propiedades de estado si las necesitas
    // Se añade Paginacion para la tabla
    pagination: {
      totalElements: number;
      totalPages: number;
      currentPage: number; // Basado en el 'number' del Page (índice 0)
      pageSize: number;
    } | null; // inicialmente null
}

// Define el estado inicial
export function createInitialState(): PermissionState {
  return {
    pagination: null,
  };
}

@Injectable({ providedIn: 'root' })
@StoreConfig({ 
  name: 'permissions',
  resettable: true, 
}) // El nombre que verás en las DevTools y resettable = true para poder limpiar el store
export class PermissionStore extends EntityStore<PermissionState> {
  constructor() {
    super(createInitialState()); // Inicializa el store
  }
}