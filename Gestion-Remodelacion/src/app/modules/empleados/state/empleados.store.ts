import { EntityState, EntityStore, StoreConfig } from '@datorama/akita';
import { Empleado } from '../models/empleado.model';
import { Injectable } from '@angular/core';

// Define la estructura del estado, incluyendo el tipo de entidad
export interface EmpleadosState extends EntityState<Empleado, number> {
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
export function createInitialState(): EmpleadosState {
  return {
    pagination: null,
  };
}

@Injectable({ providedIn: 'root' })
@StoreConfig({ 
  name: 'empleados',
  resettable: true 
}) // El nombre que verás en las DevTools y resettable = true para resetear el store
export class EmpleadosStore extends EntityStore<EmpleadosState> {
  constructor() {
    super(createInitialState()); // Inicializa el store
  }
}