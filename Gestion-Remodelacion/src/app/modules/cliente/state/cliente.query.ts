import { QueryEntity } from '@datorama/akita';
import { ClientesStore, ClientesState } from './cliente.store';
import { Injectable } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class ClientesQuery extends QueryEntity<ClientesState> {
  constructor(protected override store: ClientesStore) {
    super(store);
  }
  // Akita ya provee selectAll(), selectLoading(), etc.
}