import { EntityState, EntityStore, StoreConfig } from '@datorama/akita';
import { User } from '../../../core/models/user.model';
import { Injectable } from '@angular/core';

// Define la estructura del estado, incluyendo el tipo de entidad
export interface UserState extends EntityState<User, number> {
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
export function createInitialState(): UserState {
  return {
    pagination: null,
  };
}

@Injectable({ providedIn: 'root' })
@StoreConfig({ name: 'proyectos' }) // El nombre que verás en las DevTools
export class UserStore extends EntityStore<UserState> {
  constructor() {
    super(createInitialState()); // Inicializa el store
  }
}