import { EntityState, EntityStore, StoreConfig } from '@datorama/akita';
import { Cliente } from '../models/cliente.model'; // Asegúrate que la ruta al modelo es correcta
import { Injectable } from '@angular/core';

// Define la estructura del estado
export interface ClientesState extends EntityState<Cliente, number> { 
  // Se añade Paginacion para la tabla
  pagination: {
    totalElements: number;
    totalPages: number;
    currentPage: number; // Basado en el 'number' del Page (índice 0)
    pageSize: number;
  } | null; // inicialmente null
}

// Define el estado inicial
export function createInitialState(): ClientesState {
  return {
    pagination: null,
  };
}

@Injectable({ providedIn: 'root' })
@StoreConfig({ 
  name: 'clientes',
  resettable: true 
}) // Nombre para las DevTools y resettable para limpiar el estado
export class ClientesStore extends EntityStore<ClientesState> {
  constructor() {
    super(createInitialState()); // Inicializa
  }
}