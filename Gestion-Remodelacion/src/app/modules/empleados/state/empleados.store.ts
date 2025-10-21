import { EntityState, EntityStore, StoreConfig } from '@datorama/akita';
import { Empleado } from '../models/empleado.model';
import { Injectable } from '@angular/core';

// Define la estructura del estado, incluyendo el tipo de entidad
export interface EmpleadosState extends EntityState<Empleado, number> {
    // Aquí puedes añadir otras propiedades de estado si las necesitas
    // Por ejemplo: filtroActivo: string;
}

@Injectable({ providedIn: 'root' })
@StoreConfig({ name: 'proyectos' }) // El nombre que verás en las DevTools
export class EmpleadosStore extends EntityStore<EmpleadosState> {
  constructor() {
    super(); // Inicializa el store
  }
}