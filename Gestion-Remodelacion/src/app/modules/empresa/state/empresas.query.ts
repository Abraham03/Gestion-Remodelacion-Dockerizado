import { QueryEntity } from '@datorama/akita';
import { EmpresaState, EmpresaStore } from './empresas.store';
import { Empresa } from '../model/Empresa';
import { Injectable } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class EmpresaQuery extends QueryEntity<EmpresaState> {
  constructor(protected override store: EmpresaStore) {
    super(store);
  }
}