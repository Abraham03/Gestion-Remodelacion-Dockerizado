import { QueryEntity } from '@datorama/akita';
import { EmpleadosState, EmpleadosStore } from './empleados.store';
import { Empleado } from '../models/empleado.model';
import { Injectable } from '@angular/core';
import { map, Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class EmpleadosQuery extends QueryEntity<EmpleadosState> {
  constructor(protected override store: EmpleadosStore) {
    super(store);
  }

  selectPagination(): Observable<EmpleadosState['pagination']> {
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