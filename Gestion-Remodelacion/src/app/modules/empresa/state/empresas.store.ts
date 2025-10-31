import { EntityState, EntityStore, StoreConfig } from '@datorama/akita';
import { Empresa } from '../model/Empresa';
import { Injectable } from '@angular/core';

// Define la estructura del estado, incluyendo el tipo de entidad
export interface EmpresaState extends EntityState<Empresa, number> {
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
export function createInitialState(): EmpresaState {
  return {
    pagination: null, // Inicialmente null
  };
}

@Injectable({ providedIn: 'root' })
@StoreConfig({ 
  name: 'empresas',
  resettable: true,
 }) // El nombre que verás en las DevTools y resettable: true para que puedas restablecer el estado
export class EmpresaStore extends EntityStore<EmpresaState> {
  constructor() {
    super(createInitialState()); // Inicializa el store
  }
}