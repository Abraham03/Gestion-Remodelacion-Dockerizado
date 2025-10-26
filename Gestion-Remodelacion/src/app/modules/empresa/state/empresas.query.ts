import { QueryEntity } from '@datorama/akita';
import { EmpresaState, EmpresaStore } from './empresas.store';
import { Empresa } from '../model/Empresa';
import { Injectable } from '@angular/core';
import { map, Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class EmpresaQuery extends QueryEntity<EmpresaState> {
  constructor(protected override store: EmpresaStore) {
    super(store);
  }

  selectPagination(): Observable<EmpresaState['pagination']> {
    return this.select(state => state.pagination);
  }

  // Selectores individuales
  selectTotalElements(): Observable<number> {
    return this.selectPagination().pipe(map(pagination => pagination?.totalElements ?? 0));
  }

  selectCurrentPage(): Observable<number> {
    return this.selectPagination().pipe(map(p => p?.currentPage ?? 0));
  }

  selectPageSize(): Observable<number> {
    return this.selectPagination().pipe(map(p => p?.pageSize ?? 5)); // Valor por defecto
  }

}