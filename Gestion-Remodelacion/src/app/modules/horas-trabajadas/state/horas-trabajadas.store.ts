import { EntityState, EntityStore, StoreConfig } from '@datorama/akita';
import { HorasTrabajadas } from '../models/horas-trabajadas';
import { Injectable } from '@angular/core';

// Define la estructura del estado, incluyendo el tipo de entidad
export interface HorasTrabajadasState extends EntityState<HorasTrabajadas, number> {
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
export function createInitialState(): HorasTrabajadasState {
  return {
    pagination: null,
  };
}

@Injectable({ providedIn: 'root' })
@StoreConfig({ 
  name: 'horas-trabajadas',
  resettable: true, 
}) // El nombre que verás en las DevTools y resetable para reiniciar el store
export class HorasTrabajadasStore extends EntityStore<HorasTrabajadasState> {
  constructor() {
    super(createInitialState()); // Inicializa el store
  }
}