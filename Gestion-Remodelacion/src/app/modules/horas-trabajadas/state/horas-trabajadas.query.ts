import { QueryEntity } from '@datorama/akita';
import { HorasTrabajadasState, HorasTrabajadasStore } from './horas-trabajadas.store';
import { HorasTrabajadas } from '../models/horas-trabajadas';
import { Injectable } from '@angular/core';
import { map, Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class HorasTrabajadasQuery extends QueryEntity<HorasTrabajadasState> {
  constructor(protected override store: HorasTrabajadasStore) {
    super(store);
  }

  selectPagination(): Observable<HorasTrabajadasState['pagination']> {
    return this.select(state => state.pagination);
  }

  // Selectores individuales
  selectTotalElements(): Observable<number> {
    return this.selectPagination().pipe(map(pagination => pagination?.totalElements ?? 0));
  }

  selectCurrentPage(): Observable<number> {
    return this.selectPagination().pipe(map(pagination => pagination?.currentPage ?? 0));
  }

  selectPageSize(): Observable<number> {
    return this.selectPagination().pipe(map(pagination => pagination?.pageSize ?? 5)); // Valor por defecto
  }

}