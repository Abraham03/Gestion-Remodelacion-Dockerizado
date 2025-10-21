import { EntityState, EntityStore, StoreConfig } from '@datorama/akita';
import { Empresa } from '../model/Empresa';
import { Injectable } from '@angular/core';

// Define la estructura del estado, incluyendo el tipo de entidad
export interface EmpresaState extends EntityState<Empresa, number> {
    // Aquí puedes añadir otras propiedades de estado si las necesitas
    // Por ejemplo: filtroActivo: string;
}

@Injectable({ providedIn: 'root' })
@StoreConfig({ name: 'proyectos' }) // El nombre que verás en las DevTools
export class EmpresaStore extends EntityStore<EmpresaState> {
  constructor() {
    super(); // Inicializa el store
  }
}