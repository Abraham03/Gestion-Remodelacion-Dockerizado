import { EntityState, EntityStore, StoreConfig } from '@datorama/akita';
import { Proyecto } from '../models/proyecto.model';
import { Injectable } from '@angular/core';

// Define la estructura del estado, incluyendo el tipo de entidad
export interface ProyectosState extends EntityState<Proyecto, number> {
    // Aquí puedes añadir otras propiedades de estado si las necesitas
    // Por ejemplo: filtroActivo: string;
}

@Injectable({ providedIn: 'root' })
@StoreConfig({ name: 'proyectos' }) // El nombre que verás en las DevTools
export class ProyectosStore extends EntityStore<ProyectosState> {
  constructor() {
    super(); // Inicializa el store
  }
}