import { Injectable } from '@angular/core';
import { Query } from '@datorama/akita';
import { DashboardStore } from './dashboard.store';
import { DashboardClientes, DashboardState, DashboardSummary } from '../models/dashboard.model';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class DashboardQuery extends Query<DashboardState> {

// 1. Declaramos los tipos de los Observables (sin inicializar con =)
  public summary$: Observable<DashboardSummary | null>;
  public clientesSummary$: Observable<DashboardClientes | null>;
  public years$: Observable<number[]>;
  public isLoading$: Observable<boolean>;

constructor(protected override store: DashboardStore) {
    super(store);
    // 2. Inicializamos los selectores AQUÍ, después de super(store)
    this.summary$ = this.select(state => state.summary);
    this.clientesSummary$ = this.select(state => state.clientesSummary);
    this.years$ = this.select(state => state.availableYears);
    this.isLoading$ = this.select(state => state.isLoading);
  }
}