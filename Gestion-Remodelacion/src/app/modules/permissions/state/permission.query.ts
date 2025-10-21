import { QueryEntity } from '@datorama/akita';
import { PermissionState, PermissionStore } from './permission.store';
import { Permission } from '../../../core/models/permission.model';
import { Injectable } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class PermissionQuery extends QueryEntity<PermissionState> {
  constructor(protected override store: PermissionStore) {
    super(store);
  }
}