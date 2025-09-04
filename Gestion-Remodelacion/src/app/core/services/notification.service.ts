// src/app/core/services/notification.service.ts
import { Injectable } from '@angular/core';
import { Subject } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class NotificationService {
  private dataUpdated = new Subject<void>();
  dataUpdated$ = this.dataUpdated.asObservable();

  notifyDataChange(): void {
    this.dataUpdated.next();
  }
}