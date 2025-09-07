// src/app/core/services/notification.service.ts
import { Injectable } from '@angular/core';
import { Subject } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class NotificationService {
  private dataChangeNotifier  = new Subject<void>();
  dataChanges$ = this.dataChangeNotifier.asObservable();
  private instanceId: number;

  constructor() {
    this.instanceId = Math.random();
    // Este mensaje solo debería aparecer UNA VEZ en la consola al iniciar la app.
    console.log(`%c NotificationService Instanciado con ID: ${this.instanceId} `, 'background: #222; color: #bada55');
  }
  notifyDataChange(): void {
    console.log(`%c NOTIFICANDO CAMBIO desde la instancia: ${this.instanceId} `, 'background: red; color: white');
    this.dataChangeNotifier.next();
  }
}