import { EntityState, EntityStore, StoreConfig } from '@datorama/akita';
import { Cliente } from '../models/cliente.model'; // Asegúrate que la ruta al modelo es correcta
import { Injectable } from '@angular/core';

// Define la estructura del estado
export interface ClientesState extends EntityState<Cliente, number> { }

@Injectable({ providedIn: 'root' })
@StoreConfig({ name: 'clientes' }) // Nombre para las DevTools
export class ClientesStore extends EntityStore<ClientesState> {
  constructor() {
    super(); // Inicializa
  }
}