import { QueryEntity } from '@datorama/akita';
import { PermissionState, PermissionStore } from './permission.store';
import { Permission } from '../../../core/models/permission.model';
import { Injectable } from '@angular/core';
import { map, Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class PermissionQuery extends QueryEntity<PermissionState> {
  constructor(protected override store: PermissionStore) {
    super(store);
  }

  selectPagination(): Observable<PermissionState['pagination']> {
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