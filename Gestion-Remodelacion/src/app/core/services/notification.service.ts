// src/app/core/services/notification.service.ts
import { Injectable } from '@angular/core';
import { Subject, Observable } from 'rxjs';
import { shareReplay } from 'rxjs/operators';

/**
 * @description
 * Servicio para notificar cambios de datos entre componentes no relacionados directamente.
 * Utiliza un Subject de RxJS para emitir eventos cuando los datos cambian (ej., después de crear, actualizar o eliminar).
 * Otros componentes (como las listas) pueden suscribirse a 'dataChanges$' para refrescar sus datos.
 *
 * @example
 * // En el componente que realiza el cambio (ej., un formulario después de guardar):
 * this.notificationService.notifyDataChange();
 *
 * // En el componente que necesita reaccionar (ej., una lista):
 * this.notificationService.dataChanges$.subscribe(() => {
 * this.reloadData(); // Llama a tu método para recargar datos
 * });
 */
@Injectable({
  providedIn: 'root', // Disponible globalmente en la aplicación
})
export class NotificationService {
  /**
   * @private
   * Subject interno que emitirá los eventos de notificación.
   * Usamos Subject porque no necesitamos un valor inicial y solo queremos
   * notificar a los suscriptores *después* de que se suscriban.
   */
  private dataChangedSource = new Subject<void>();

  /**
   * @public
   * Observable público al que los componentes se suscribirán para
   * recibir notificaciones de cambio de datos.
   * 'shareReplay(1)' asegura que los suscriptores compartan la misma
   * fuente y que los nuevos suscriptores reciban la última notificación si la hubo.
   * (Aunque con Subject<void>, shareReplay es menos crítico, es buena práctica).
   */
  public dataChanges$: Observable<void> = this.dataChangedSource.asObservable().pipe(
      shareReplay(1) // Comparte la misma instancia del observable entre suscriptores
  );

  constructor() {}

  /**
   * @public
   * Método que los componentes llamarán para indicar que los datos han cambiado.
   * Emite un evento vacío ('void') a través del Subject.
   */
  public notifyDataChange(): void {
    console.log('NotificationService: Data change notified!'); // Log para depuración
    this.dataChangedSource.next();
  }
}