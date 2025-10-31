import { Component, OnInit, ViewChild, AfterViewInit, ChangeDetectorRef, OnDestroy, inject } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { MatTableDataSource, MatTableModule } from '@angular/material/table';
import { MatPaginator, MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSortModule, Sort } from '@angular/material/sort';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatChipsModule } from '@angular/material/chips';
import { HttpErrorResponse } from '@angular/common/http';
import { combineLatest, map, Subject, take, takeUntil } from 'rxjs';
import { TranslateModule, TranslateService } from '@ngx-translate/core';

import { ProyectosService } from '../../services/proyecto.service';
import { Proyecto } from '../../models/proyecto.model';
import { ProyectosFormComponent } from '../proyecto-form/proyectos-form.component';
import { ExportService } from '../../../../core/services/export.service';
import { AuthService } from '../../../../core/services/auth.service';
import { Observable, Subscription } from 'rxjs';
import { ProyectosQuery } from '../../state/proyecto.query';
import { AsyncPipe } from '@angular/common';

import { EmpleadosQuery } from '../../../empleados/state/empleados.query';
import { EmpleadoService } from '../../../empleados/services/empleado.service';
import { Empleado } from '../../../empleados/models/empleado.model';
import { ClientesQuery } from '../../../cliente/state/cliente.query';
import { ClienteService } from '../../../cliente/services/cliente.service';
import { Cliente } from '../../../cliente/models/cliente.model';
import { NotificationService } from '../../../../core/services/notification.service';

// Interfaz para el viewModel combinando que usara la tabla
interface ProyectosViewModel {
  nombreClienteActualizado: string;
  nombreEmpleadoResponsableActualizado: string;
}

@Component({
  selector: 'app-proyectos-list',
  standalone: true,
  imports: [
    CommonModule, MatTableModule, MatButtonModule, MatIconModule, MatTooltipModule,
    MatPaginatorModule, MatFormFieldModule, MatInputModule, MatDialogModule,
    MatSortModule, MatChipsModule, MatProgressBarModule, TranslateModule, AsyncPipe
  ],
  templateUrl: './proyecto-list.component.html',
  styleUrls: ['./proyecto-list.component.scss'],
  providers: [DatePipe],
})
export class ProyectosListComponent implements OnInit, AfterViewInit, OnDestroy { 
  canExportExcel = false;
  canExportPdf = false;
  canCreate = false;
  canEdit = false;
  canDelete = false;

  //dataSource = new MatTableDataSource<Proyecto>([]);
  displayedColumns: string[] = ['nombreProyecto', 'nombreCliente', 'nombreEmpleadoResponsable', 'estado', 'progresoPorcentaje', 'fechaInicio', 'fechaFinEstimada', 'acciones'];

  @ViewChild(MatPaginator) paginator!: MatPaginator;

  private proyectosService = inject(ProyectosService);
  private dialog = inject(MatDialog);
  private snackBar = inject(MatSnackBar);
  private exportService = inject(ExportService);
  private authService = inject(AuthService);
  private translate = inject(TranslateService);

  private notificationService = inject(NotificationService);
  private empleadosQuery = inject(EmpleadosQuery);
  private empleadosService = inject(EmpleadoService);
  private clientesQuery = inject(ClientesQuery);
  private clientesService = inject(ClienteService);

  // Se inyecta el Query de Proyectos
  private proyectosQuery = inject(ProyectosQuery);
  
  // Se define Observables para los datos y el estado de carga
  proyectos$: Observable<Proyecto[]> = this.proyectosQuery.selectAll();
  loading$: Observable<boolean> = this.proyectosQuery.selectLoading();
  // Se define Observables para los datos y el estado de carga
  totalElements$: Observable<number> = this.proyectosQuery.selectTotalElements();
  currentPage$: Observable<number> = this.proyectosQuery.selectCurrentPage();
  pageSize$: Observable<number> = this.proyectosQuery.selectPageSize();

  // Observable que combina Proyectos con Empleados y clientes
  proyectosViewModel$: Observable<Proyecto[]> = combineLatest([
    this.proyectosQuery.selectAll(), // Stream 1: Proyectos (contiene ids de empleados y clientes)
    this.empleadosQuery.selectAll({ asObject: true }), // Stream 2: Objeto de empleados
    this.clientesQuery.selectAll({ asObject: true }) // Stream 3: Objeto de clientes      
  ]).pipe(
    map(([proyectosArray, empleadosMap, clientesMap]) => {
        // Si alguno de los mapas esta vacio (aun no cargado), se retorna un array vacio
        // para evitar errores y mostrar "No hay registros" temporalmente
        if (Object.keys(empleadosMap).length === 0 || Object.keys(clientesMap).length === 0) {
          // Podria devolver ProyectoArray aqui si prefieres mostrar los nombres viejos temporalmente
          // return proyectosArray; // Opcional: Mostrar datos viejos mientras carga
          return [];
    }
    // Por cada proyecto, busca el empleado y cliente mas recientes en sus respectivos mapas
    return proyectosArray.map(pct => {
      const empleadoActual = empleadosMap[pct.idEmpleadoResponsable];
      const clienteActual = clientesMap[pct.idCliente];

      return {
        ...pct, // copia todas las propiedades originales de Proyecto
        // Añade / Sobreescribe los nombres actualizados de los stores correspondientes
        nombreEmpleadoResponsableActualizado: empleadoActual?.nombreCompleto ?? pct.nombreEmpleadoResponsable ?? 'Empleado Desconocido',
        nombreClienteActualizado: clienteActual?.nombreCliente ?? pct.nombreCliente ?? 'Cliente Desconocido'
      }
    });
    })
  );
  private destroy$ = new Subject<void>();
  
  pageSizeLocal = 5;
  currentPageLocal = 0;
  currentSort = 'fechaInicio';
  sortDirection = 'asc';
  filterValue = '';

  // Suscripcion para el observable
  private paginatorSubscription: Subscription | null = null;


  ngOnInit(): void {
    this.setPermissions();

    this.notificationService.dataChanges$.pipe(
      takeUntil(this.destroy$) // Desuscribirse automáticoamente
    ).subscribe(() => {
      console.log('ProyectosList: Received data change notification, reloading...');
      // Llama a tu método de carga para refrescar la lista desde la API
      this.loadProyectos();
    })

  }

  ngAfterViewInit(): void {
    this.proyectosQuery.selectHasCache().pipe(take(1) // Solo se necesita verificar una vez al cargar
    ).subscribe(
      hasCache => {
        if (!hasCache) {
          this.loadProyectos();
        }
      }
    );

    // carga empleados si es necesario (para tener los nombres actualizados en la tabla)
    this.empleadosQuery.selectHasCache().pipe(take(1)
    ).subscribe(hasCache => {
      if (!hasCache) {
        console.log('Proyectos List: Cargando Empleados para nombres actualizados');
        this.empleadosService.getEmpleados().subscribe();
      }
    });

    // carga clientes si es necesario (para tener los nombres actualizados en la tabla)
    this.clientesQuery.selectHasCache().pipe(take(1)
    ).subscribe(hasCache => {
      if (!hasCache) {
        console.log('Proyectos List: Cargando Clientes para nombres actualizados');
        this.clientesService.getClientes().subscribe();
      }
    });

    // Suscribirse a los cambios del store para mantener sincronizado el paginador
    this.paginatorSubscription = this.proyectosQuery.selectPagination().subscribe(pagination => {
      if (pagination && this.paginator) {
        // Actualiza el estado visual del paginador
        this.paginator.length = pagination.totalElements;
        this.paginator.pageIndex = pagination.currentPage;
        this.paginator.pageSize = pagination.pageSize;
      }
    });

  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
    this.paginatorSubscription?.unsubscribe();
  }

  private setPermissions(): void {
    this.canCreate = this.authService.hasPermission('PROYECTO_CREATE');
    this.canEdit = this.authService.hasPermission('PROYECTO_UPDATE');
    this.canDelete = this.authService.hasPermission('PROYECTO_DELETE');
    const userPlan = this.authService.currentUserPlan();
    const hasPremiumPlan = userPlan === 'NEGOCIOS' || userPlan === 'PROFESIONAL';
    this.canExportExcel = hasPremiumPlan;
    this.canExportPdf = hasPremiumPlan;
    //this.canExportExcel = this.authService.hasPermission('EXPORT_EXCEL') && hasPremiumPlan;
    //this.canExportPdf = this.authService.hasPermission('EXPORT_PDF') && hasPremiumPlan;
  }

  loadProyectos(): void {
    const sortParam = `${this.currentSort},${this.sortDirection}`;
    this.proyectosService.getProyectosPaginated(
      this.currentPageLocal, 
      this.pageSizeLocal, 
      this.filterValue, 
      sortParam)
      .subscribe();
  }

  applyFilter(filterValue: string): void {
    this.filterValue = filterValue.trim().toLowerCase();
    this.paginator.pageIndex = 0;
    this.currentPageLocal = 0;
    this.loadProyectos();
  }

  applyFilterIfEmpty(filterValue: string): void {
    if (filterValue === '') {
      this.applyFilter('');
    }
  }  

  openForm(proyecto?: Proyecto): void {
    const dialogRef = this.dialog.open(ProyectosFormComponent, {
      width: '800px',
      data: proyecto || null,
    });
    dialogRef.afterClosed().subscribe((result) => {
      if (result) this.loadProyectos();
    });
  }

  deleteProyecto(id: number): void {
    if (confirm(this.translate.instant('PROJECTS.CONFIRM_DELETE'))) {
      this.proyectosService.deleteProyecto(id).subscribe({
        next: () => {
          this.snackBar.open(this.translate.instant('PROJECTS.SUCCESSFULLY_DELETED'), this.translate.instant('GLOBAL.CLOSE'), { duration: 3000 });
          this.notificationService.notifyDataChange();
          this.loadProyectos();
        },
        error: (err: HttpErrorResponse) => {
          const errorKey = err.error?.message || 'error.unexpected';
          const translatedMessage = this.translate.instant(errorKey);
          this.snackBar.open(translatedMessage, this.translate.instant('GLOBAL.CLOSE'), {
            duration: 7000,
            panelClass: ['error-snackbar']
          });        
        },
      });
    }
  }

  getEstadoColor(estado: string): 'primary' | 'accent' | 'warn' | 'basic' {
    switch (estado) {
      case 'PENDING': return 'basic';
      case 'IN_PROGRESS': return 'primary';
      case 'COMPLETED': return 'accent';
      case 'CANCELLED': case 'PAUSED': return 'warn';
      default: return 'basic';
    }
  }

  onPageChange(event: PageEvent): void {
    this.currentPageLocal = event.pageIndex;
    this.pageSizeLocal = event.pageSize;
    this.loadProyectos();
  }

  onSortChange(sort: Sort) {
    this.currentSort = sort.direction ? sort.active : 'nombreProyecto';
    this.sortDirection = sort.direction || 'asc';
    this.loadProyectos();
  }

  exportTo(format: 'excel' | 'pdf'): void {
    const sortParam = `${this.currentSort},${this.sortDirection}`;
    const apiUrl = `${this.proyectosService.getApiUrl()}/export/${format}`;
    const exportCall = format === 'excel'
        ? this.exportService.exportToExcel(apiUrl, this.filterValue, sortParam)
        : this.exportService.exportToPdf(apiUrl, this.filterValue, sortParam);

    const fileDetails = format === 'excel'
        ? { name: 'Reporte_Proyectos.xlsx', type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' }
        : { name: 'Reporte_Proyectos.pdf', type: 'application/pdf' };

    exportCall.subscribe({
        next: (response) => {
            this.exportService.downloadFile(response, fileDetails.name, fileDetails.type);
        },
        error: (error) => {
            console.error(`Error al exportar a ${format}:`, error);
            const errorKey = format === 'excel' ? 'PROJECTS.ERROR_EXPORT_EXCEL' : 'PROJECTS.ERROR_EXPORT_PDF';
            this.snackBar.open(this.translate.instant(errorKey), this.translate.instant('GLOBAL.CLOSE'), { duration: 5000 });
        }
    });
  }
}