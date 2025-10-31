import { EntityState, EntityStore, StoreConfig } from '@datorama/akita';
import { Role } from '../../../core/models/role.model';
import { Injectable } from '@angular/core';

// Define la estructura del estado, incluyendo el tipo de entidad
export interface RoleState extends EntityState<Role, number> {
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
export function createInitialState(): RoleState {
  return {
    pagination: null,
  };
}

@Injectable({ providedIn: 'root' })
@StoreConfig({
   name: 'roles',
   resettable: true, 
  }) // El nombre que verás en las DevTools y resettable: true permite resetear el store
export class RoleStore extends EntityStore<RoleState> {
  constructor() {
    super(createInitialState()); // Inicializa el store
  }
}