import { QueryEntity } from '@datorama/akita';
import { ProyectosStore, ProyectosState } from './proyecto.store';
import { Proyecto } from '../models/proyecto.model';
import { Injectable } from '@angular/core';
import { map, Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class ProyectosQuery extends QueryEntity<ProyectosState> {
  constructor(protected override store: ProyectosStore) {
    super(store);
  }

  selectPagination(): Observable<ProyectosState['pagination']> {
    return this.select('pagination');
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