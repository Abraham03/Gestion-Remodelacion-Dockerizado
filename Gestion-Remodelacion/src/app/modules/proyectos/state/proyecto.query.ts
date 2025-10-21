import { QueryEntity } from '@datorama/akita';
import { ProyectosStore, ProyectosState } from './proyecto.store';
import { Proyecto } from '../models/proyecto.model';
import { Injectable } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class ProyectosQuery extends QueryEntity<ProyectosState> {
  constructor(protected override store: ProyectosStore) {
    super(store);
  }
}