import { QueryEntity } from '@datorama/akita';
import { HorasTrabajadasState, HorasTrabajadasStore } from './horas-trabajadas.store';
import { HorasTrabajadas } from '../models/horas-trabajadas';
import { Injectable } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class HorasTrabajadasQuery extends QueryEntity<HorasTrabajadasState> {
  constructor(protected override store: HorasTrabajadasStore) {
    super(store);
  }
}