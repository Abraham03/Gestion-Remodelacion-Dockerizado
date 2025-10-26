import { QueryEntity } from '@datorama/akita';
import { RoleState, RoleStore } from './roles.store';
import { Role } from '../../../core/models/role.model';
import { Injectable } from '@angular/core';
import { map, Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class RoleQuery extends QueryEntity<RoleState> {
  constructor(protected override store: RoleStore) {
    super(store);
  }

  selectPagination(): Observable<RoleState['pagination']> {
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