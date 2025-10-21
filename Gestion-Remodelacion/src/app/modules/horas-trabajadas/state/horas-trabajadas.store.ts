import { EntityState, EntityStore, StoreConfig } from '@datorama/akita';
import { HorasTrabajadas } from '../models/horas-trabajadas';
import { Injectable } from '@angular/core';

// Define la estructura del estado, incluyendo el tipo de entidad
export interface HorasTrabajadasState extends EntityState<HorasTrabajadas, number> {
    // Aquí puedes añadir otras propiedades de estado si las necesitas
    // Por ejemplo: filtroActivo: string;
}

@Injectable({ providedIn: 'root' })
@StoreConfig({ name: 'proyectos' }) // El nombre que verás en las DevTools
export class HorasTrabajadasStore extends EntityStore<HorasTrabajadasState> {
  constructor() {
    super(); // Inicializa el store
  }
}