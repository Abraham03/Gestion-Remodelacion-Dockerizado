import { QueryEntity } from '@datorama/akita';
import { EmpleadosState, EmpleadosStore } from './empleados.store';
import { Empleado } from '../models/empleado.model';
import { Injectable } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class EmpleadosQuery extends QueryEntity<EmpleadosState> {
  constructor(protected override store: EmpleadosStore) {
    super(store);
  }
}