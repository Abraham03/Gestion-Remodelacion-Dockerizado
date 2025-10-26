import { Component, ViewChild, AfterViewInit, ChangeDetectorRef, OnInit, inject, OnDestroy } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { CommonModule, DatePipe } from '@angular/common';
import { MatTableModule, MatTableDataSource } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatPaginatorModule, MatPaginator, PageEvent } from '@angular/material/paginator';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { Sort, MatSortModule } from '@angular/material/sort';
import { HttpErrorResponse } from '@angular/common/http';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { EmpleadoService } from '../../services/empleado.service';
import { Empleado } from '../../models/empleado.model';
import { EmpleadoFormComponent } from '../empleado-form/empleado-form.component';
import { ExportService } from '../../../../core/services/export.service';
import { AuthService } from '../../../../core/services/auth.service';
import { PhonePipe } from '../../../../shared/pipes/phone.pipe';
import { EmpleadosQuery } from '../../state/empleados.query';
import { AsyncPipe } from '@angular/common';
import { Observable, Subscription } from 'rxjs';

@Component({
  selector: 'app-empleado-list',
  standalone: true,
  imports: [
    CommonModule, MatTableModule, MatButtonModule, MatIconModule, MatTooltipModule,
    MatPaginatorModule, MatFormFieldModule, MatInputModule, PhonePipe,
    MatSortModule, TranslateModule, AsyncPipe, MatProgressBarModule
  ],
  providers: [DatePipe],
  templateUrl: './empleado-list.component.html',
  styleUrls: ['./empleado-list.component.scss'],
})
export class EmpleadoListComponent implements OnInit, AfterViewInit, OnDestroy {
  // Propiedades
  canExportExcel = false;
  canExportPdf = false;
  canCreate = false;
  canEdit = false;
  canDelete = false;
 
  displayedColumns: string[] = ['nombreCompleto', 'rolCargo', 'telefonoContacto', 'modeloDePago', 'costoPorHora', 'activo', 'fechaContratacion', 'notas', 'acciones'];

  @ViewChild(MatPaginator) paginator!: MatPaginator;

  // Se inyecta el Query de Empleados
  private empleadosQuery = inject(EmpleadosQuery);

  // Se define Observables para los datos y el estado de carga
  empleados$: Observable<Empleado[]> = this.empleadosQuery.selectAll();
  loading$: Observable<boolean> = this.empleadosQuery.selectLoading();

  // Observables para la paginación (directos al HTML con async pipe)
  totalElements$: Observable<number> = this.empleadosQuery.selectTotalElements();
  pageSize$: Observable<number> = this.empleadosQuery.selectPageSize();
  currentPage$: Observable<number> = this.empleadosQuery.selectCurrentPage();

  currentPageLocal: number = 0;
  pageSizeLocal: number = 5;
  filterValue: string = '';
  currentSort: string = 'nombreCompleto';
  sortDirection: string = 'asc';

  // Suscripcion para actualizar paginator
  private paginatorSubscription: Subscription | null = null;

  // Inyección de servicios
  private empleadoService = inject(EmpleadoService);
  private dialog = inject(MatDialog);
  private snackBar = inject(MatSnackBar);
  private exportService = inject(ExportService);
  private cdr = inject(ChangeDetectorRef);
  private authService = inject(AuthService);
  private translate = inject(TranslateService);

  ngOnInit(): void {
    this.setPermissions();
  }

  ngAfterViewInit(): void {
    this.loadEmpleados();
    
  // Suscribirse a los cambios del store para mantener sincronizado el paginador
    this.paginatorSubscription = this.empleadosQuery.selectPagination().subscribe(pagination => {
        if (pagination && this.paginator) {
            // Actualiza el estado visual del paginador
            this.paginator.length = pagination.totalElements;
            this.paginator.pageIndex = pagination.currentPage;
            this.paginator.pageSize = pagination.pageSize;
        }
    });

  }

  ngOnDestroy(): void {
      this.paginatorSubscription?.unsubscribe();
  }

  private setPermissions(): void {
    this.canCreate = this.authService.hasPermission('EMPLEADO_CREATE');
    this.canEdit = this.authService.hasPermission('EMPLEADO_UPDATE');
    this.canDelete = this.authService.hasPermission('EMPLEADO_DELETE');
    const userPlan = this.authService.currentUserPlan();
    const hasPremiumPlan = userPlan === 'NEGOCIOS' || userPlan === 'PROFESIONAL';
    this.canExportExcel = hasPremiumPlan;
    this.canExportPdf = hasPremiumPlan;
  }

  loadEmpleados(): void {
    const sortParam = `${this.currentSort},${this.sortDirection}`;
    this.empleadoService.getEmpleados(this.currentPageLocal, this.pageSizeLocal, this.filterValue, sortParam)
      .subscribe();
  }

  applyFilter(filterValue: string): void {
    this.filterValue = filterValue.trim().toLowerCase();
    this.paginator.pageIndex = 0;
    this.currentPageLocal = 0;
    this.loadEmpleados();
  }

  applyFilterIfEmpty(filterValue: string): void {
    if (filterValue === '') {
      this.applyFilter('');
    }
  }

  openForm(empleado?: Empleado): void {
    const dialogRef = this.dialog.open(EmpleadoFormComponent, {
      width: '500px',
      data: empleado || null,
    });
    dialogRef.afterClosed().subscribe((result) => {
      if (result) this.loadEmpleados();
    });
  }

  deleteEmpleado(id: number): void {
    const closeAction = this.translate.instant('GLOBAL.CLOSE');
    if (!id) {
      this.snackBar.open(this.translate.instant('EMPLOYEES.ERROR_INVALID_ID_FOR_DELETE'), closeAction, { duration: 3000 });
      return;
    }

    if (confirm(this.translate.instant('GLOBAL.CONFIRM_DEACTIVATE'))) {
      this.empleadoService.deactivateEmpleado(id, false).subscribe({
        next: () => {
          this.snackBar.open(this.translate.instant('EMPLOYEES.SUCCESSFULLY_DEACTIVATED'), closeAction, { duration: 3000 });
        },
        error: (err: HttpErrorResponse) => {
          console.error('Error al desactivar empleado:', err);
          const errorKey = err.error?.message || 'error.unexpected';
          const translatedMessage = this.translate.instant(errorKey);
          
          this.snackBar.open(translatedMessage, closeAction, { duration: 5000 });
        }
      });
    }
  }

  onPageChange(event: PageEvent): void {
    this.currentPageLocal = event.pageIndex;
    this.pageSizeLocal = event.pageSize;
    this.loadEmpleados();
  }

  onSortChange(sort: Sort) {
    this.currentSort = sort.direction ? sort.active : 'nombreCompleto';
    this.sortDirection = sort.direction || 'asc';
    this.currentPageLocal = 0;
    this.loadEmpleados();
  }

  exportTo(format: 'excel' | 'pdf'): void {
    const sortParam = `${this.currentSort},${this.sortDirection}`;
    const apiUrl = `${this.empleadoService.getApiUrl()}/export/${format}`;
    const exportCall = format === 'excel'
      ? this.exportService.exportToExcel(apiUrl, this.filterValue, sortParam)
      : this.exportService.exportToPdf(apiUrl, this.filterValue, sortParam);

    const fileDetails = format === 'excel'
      ? { name: 'Reporte_Empleados.xlsx', type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' }
      : { name: 'Reporte_Empleados.pdf', type: 'application/pdf' };

    exportCall.subscribe({
      next: (response) => this.exportService.downloadFile(response, fileDetails.name, fileDetails.type),
      error: (error) => {
        console.error(`Error al exportar a ${format}:`, error);
        const errorKey = format === 'excel' ? 'EMPLOYEES.ERROR_EXPORT_EXCEL' : 'EMPLOYEES.ERROR_EXPORT_PDF';
        this.snackBar.open(this.translate.instant(errorKey), this.translate.instant('GLOBAL.CLOSE'), { duration: 5000 });
      }
    });
  }
}