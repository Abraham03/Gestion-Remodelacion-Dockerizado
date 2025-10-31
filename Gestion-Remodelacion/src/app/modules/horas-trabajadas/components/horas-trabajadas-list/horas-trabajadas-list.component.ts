import { CommonModule, DatePipe, DecimalPipe } from '@angular/common';
import { AfterViewInit, ChangeDetectorRef, Component, inject, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatPaginator, MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatTableModule } from '@angular/material/table';
import { MatTooltipModule } from '@angular/material/tooltip';
import { Subject, take, takeUntil } from 'rxjs';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatSort, MatSortModule, Sort } from '@angular/material/sort';
import { HttpErrorResponse } from '@angular/common/http';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { NotificationService } from '../../../../core/services/notification.service';
import { HorasTrabajadas } from '../../models/horas-trabajadas';
import { HorasTrabajadasService } from '../../services/horas-trabajadas.service';
import { HorasTrabajadasFormComponent } from '../horas-trabajadas-form/horas-trabajadas-form.component';
import { AuthService } from '../../../../core/services/auth.service';
import { ExportService } from '../../../../core/services/export.service';

import { HorasTrabajadasQuery } from '../../state/horas-trabajadas.query';
import { Empleado } from '../../../empleados/models/empleado.model';
import { EmpleadosQuery } from '../../../empleados/state/empleados.query';
import { EmpleadoService } from '../../../empleados/services/empleado.service';
import { Proyecto } from '../../../proyectos/models/proyecto.model';
import { ProyectosQuery } from '../../../proyectos/state/proyecto.query';
import { ProyectosService } from '../../../proyectos/services/proyecto.service';
import { AsyncPipe } from '@angular/common';
import { Observable, Subscription, combineLatest, of } from 'rxjs';
import { map, switchMap } from 'rxjs/operators';

// Interfaz para el viewModel combinando que usara la tabla
  interface HorasTrabajadasViewModel {
    nombreEmpleadoActualizado: string;
    nombreProyectoActualizado: string;
  }

@Component({
  selector: 'app-horas-trabajadas-list',
  standalone: true,
  imports: [
    CommonModule, MatTableModule, MatButtonModule, MatIconModule, MatTooltipModule,
    MatPaginatorModule, MatFormFieldModule, MatInputModule, MatDialogModule,
    DatePipe, MatSortModule, TranslateModule, MatProgressBarModule, AsyncPipe
  ],
  providers: [DatePipe],
  templateUrl: './horas-trabajadas-list.component.html',
  styleUrl: './horas-trabajadas-list.component.scss'
})
export class HorasTrabajadasListComponent implements OnInit, AfterViewInit, OnDestroy {
  // Propiedades de permisos
  canExportExcel = false;
  canExportPdf = false;
  canCreate = false;
  canEdit = false;
  canDelete = false;



  // Propiedades de la tabla y paginación
  displayedColumns: string[] = ['fecha', 'nombreEmpleado', 'nombreProyecto', 'horas', 'montoTotal', 'actividadRealizada', 'acciones'];

  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;

  // Inyección de servicios
  private horasTrabajadasService = inject(HorasTrabajadasService);
  private dialog = inject(MatDialog);
  private snackBar = inject(MatSnackBar);
  private exportService = inject(ExportService);
  private authService = inject(AuthService);
  private translate = inject(TranslateService);
  private notificationService = inject(NotificationService);

  // Se inyecta las querys y servicios pra EMpleados y Proyectos
  private horasTrabajadasQuery = inject(HorasTrabajadasQuery);
  private empleadosQuery = inject(EmpleadosQuery);
  private empleadosService = inject(EmpleadoService);
  private proyectosQuery = inject(ProyectosQuery);
  private proyectosService = inject(ProyectosService);
  
  // Se define Observables para los datos y el estado de carga
  horasTrabajadas$: Observable<HorasTrabajadas[]> = this.horasTrabajadasQuery.selectAll();
  loading$: Observable<boolean> = this.horasTrabajadasQuery.selectLoading();
  // Observables para la paginación (directos al HTML con async pipe)
  totalElements$: Observable<number> = this.horasTrabajadasQuery.selectTotalElements();
  pageSize$: Observable<number> = this.horasTrabajadasQuery.selectPageSize();
  currentPage$: Observable<number> = this.horasTrabajadasQuery.selectCurrentPage();

  // Observable que combina HorasTrabajadas con Empleados y Proyectos actualizados
  horasTrabajadasViewModel$: Observable<HorasTrabajadasViewModel[]> = combineLatest([
    this.horasTrabajadasQuery.selectAll(), // Stream 1: HorasTrabajadas (contiene ids de empleados y proyectos)
    this.empleadosQuery.selectAll({ asObject: true }), // Stream 2: Objeto de empleados
    this.proyectosQuery.selectAll({ asObject: true }) // Stream 3: Objeto de proyectos
  ]).pipe(
    map(([horasArray, empleadosMap, proyectosMap]) => {
      // Si alguno de los mapas esta vacio (aun no cargado), se retorna un array vacio
      // Para evitar errores y mostrar "No hay registros" temporalmente.
      if (Object.keys(empleadosMap).length === 0 || Object.keys(proyectosMap).length === 0) {
          // Podrias devolver horasArray aqui si prefieres mostrar los nombres viejos temporalmente
          // return horasArray; // Opcional: Mostrar datos viejos mientras carga los nuevos
          return [];
      }

      // Por cada hora trabajada, busca el empleado y proyecto mas recientes en sus respectivos mapas
      return horasArray.map(ht => {
        const empleadoActual = empleadosMap[ht.idEmpleado];
        const proyectoActual = proyectosMap[ht.idProyecto];
        return {
          ...ht,// copia todas las propiedades originales de HorasTrabajadas
          // Añade / Sobreescribe los nombres actualizados de los stores correspondientes
          nombreEmpleadoActualizado: empleadoActual?.nombreCompleto ?? ht.nombreEmpleado ?? 'Empleado no encontrado',
          nombreProyectoActualizado: proyectoActual?.nombreProyecto ?? ht.nombreProyecto ?? 'Proyecto no encontrado'
        };

      });
    })
  );

  private destroy$ = new Subject<void>();

  pageSizeLocal = 5;
  currentPageLocal = 0;
  filterValue = '';
  currentSort = 'fecha';
  sortDirection = 'desc';

  // Suscripcion para actualizar paginator
  private paginatorSubscription: Subscription | null = null;

  ngOnInit(): void {
    this.setPermissions();

  this.notificationService.dataChanges$.pipe(
        takeUntil(this.destroy$) // Desuscribirse automáticamente
      ).subscribe(() => {
        console.log('HorasTrabajadasList: Received data change notification, reloading...');
        // Llama a tu método de carga para refrescar la lista desde la API
        this.loadHorasTrabajadas();
      });

  }

  ngAfterViewInit() {
    this.horasTrabajadasQuery.selectHasCache().pipe(take(1) // Solo se necesita verificar una vez al cargar
    ).subscribe(
      hasCache => {
        if (!hasCache) {
          this.loadHorasTrabajadas();
        }
      }
    );


    // Carga Empleados si es necesario (para tener los nombres actualizados en la tabla)
    this.empleadosQuery.selectHasCache().pipe(take(1)
    ).subscribe(hasCache => {
      if (!hasCache) {
        console.log('Horas Trabajadas List: Cargando Empleados para nombres actualizados');
        this.empleadosService.getEmpleados().subscribe();
      }
    });

    // Carga Proyectos si es necesario (para tener los nombres actualiSzados en la tabla)
    this.proyectosQuery.selectHasCache().pipe(take(1)
    ).subscribe(hasCache => {
      if (!hasCache) {
        console.log('Horas Trabajadas List: Cargando Proyectos para nombres actualizados');
        this.proyectosService.getProyectosPaginated().subscribe();
      }
    });


    // Suscribirse a los cambios del store para mantener sincronizado el paginador
    this.paginatorSubscription = this.horasTrabajadasQuery.selectPagination().subscribe(pagination => {
      if (pagination && this.paginator) {
        // Actualiza el estado visual del paginador
        this.paginator.length = pagination.totalElements;
        this.paginator.pageIndex = pagination.currentPage;
        this.paginator.pageSize = pagination.pageSize;
      }
    })

  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
    this.paginatorSubscription?.unsubscribe();
  }

  private setPermissions(): void {
    this.canCreate = this.authService.hasPermission('HORASTRABAJADAS_CREATE');
    this.canEdit = this.authService.hasPermission('HORASTRABAJADAS_UPDATE');
    this.canDelete = this.authService.hasPermission('HORASTRABAJADAS_DELETE');
    const userPlan = this.authService.currentUserPlan();
    const hasPremiumPlan = userPlan === 'NEGOCIOS' || userPlan === 'PROFESIONAL';
    this.canExportExcel = this.authService.hasPermission('EXPORT_EXCEL') && hasPremiumPlan;
    this.canExportPdf = this.authService.hasPermission('EXPORT_PDF') && hasPremiumPlan;
  }

  loadHorasTrabajadas(): void {
    const sortParam = `${this.currentSort},${this.sortDirection}`;
    this.horasTrabajadasService.getHorasTrabajadasPaginated(
      this.currentPageLocal, 
      this.pageSizeLocal, 
      this.filterValue, 
      sortParam)
      .subscribe();
  }

  applyFilter(filterValue: string): void {
    this.filterValue = filterValue.trim().toLowerCase();
    this.currentPageLocal = 0;
    this.paginator.pageIndex = 0;
    this.loadHorasTrabajadas();
  }

  applyFilterIfEmpty(filterValue: string): void {
    if (filterValue === '') {
      this.applyFilter('');
    }
  }

  openForm(horasTrabajadas?: HorasTrabajadas): void {
    const dialogRef = this.dialog.open(HorasTrabajadasFormComponent, {
      width: '500px',
      data: horasTrabajadas || null,
    });
    dialogRef.afterClosed().pipe(takeUntil(this.destroy$)).subscribe((result) => {
      if (result) this.loadHorasTrabajadas();
    });
  }

  deleteHorasTrabajadas(id: number): void {
    if (confirm(this.translate.instant('WORK_HOURS.CONFIRM_DELETE'))) {
      this.horasTrabajadasService.deleteHorasTrabajadas(id).subscribe({
        next: () => {
          this.snackBar.open(this.translate.instant('WORK_HOURS.SUCCESSFULLY_DELETED'), this.translate.instant('GLOBAL.CLOSE'), { duration: 3000 });
          this.notificationService.notifyDataChange();
          this.loadHorasTrabajadas();
        },
        error: (err: HttpErrorResponse) => {
          const errorKey = err.error?.message || 'error.unexpected';
          const translatedMessage = this.translate.instant(errorKey);
          this.snackBar.open(translatedMessage, this.translate.instant('GLOBAL.CLOSE'), { duration: 7000 });
        },
      });
    }
  }

  onPageChange(event: PageEvent): void {
    this.currentPageLocal = event.pageIndex;
    this.pageSizeLocal = event.pageSize;
    this.loadHorasTrabajadas();
  }
  
  onSortChange(sort: Sort) {
    this.currentSort = sort.direction ? sort.active : 'fecha';
    this.sortDirection = sort.direction || 'desc';
    this.loadHorasTrabajadas();
  }

  exportTo(format: 'excel' | 'pdf'): void {
    const sortParam = `${this.currentSort},${this.sortDirection}`;
    const apiUrl = `${this.horasTrabajadasService.getApiUrl()}/export/${format}`;
    const exportCall = format === 'excel'
      ? this.exportService.exportToExcel(apiUrl, this.filterValue, sortParam)
      : this.exportService.exportToPdf(apiUrl, this.filterValue, sortParam);

    const fileDetails = format === 'excel'
      ? { name: 'Reporte_Horas_Trabajadas.xlsx', type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' }
      : { name: 'Reporte_Horas_Trabajadas.pdf', type: 'application/pdf' };

    exportCall.subscribe({
      next: (response) => this.exportService.downloadFile(response, fileDetails.name, fileDetails.type),
      error: (error) => {
        console.error(`Error al exportar a ${format}:`, error);
        const errorKey = format === 'excel' ? 'WORK_HOURS.ERROR_EXPORT_EXCEL' : 'WORK_HOURS.ERROR_EXPORT_PDF';
        this.snackBar.open(this.translate.instant(errorKey), this.translate.instant('GLOBAL.CLOSE'), { duration: 5000 });
      }
    });
  }
}