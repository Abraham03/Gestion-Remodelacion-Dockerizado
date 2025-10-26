import { QueryEntity } from '@datorama/akita';
import { UserStore, UserState } from './users.store';
import { Injectable } from '@angular/core';
import { map, Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class UserQuery extends QueryEntity<UserState> {
  constructor(protected override store: UserStore) {
    super(store);
  }

  selectPagination(): Observable<UserState['pagination']> {
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