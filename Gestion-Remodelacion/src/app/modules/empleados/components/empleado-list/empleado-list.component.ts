import { Component, ViewChild, AfterViewInit, ChangeDetectorRef, OnInit, inject } from '@angular/core';
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

import { EmpleadoService } from '../../services/empleado.service';
import { Empleado } from '../../models/empleado.model';
import { EmpleadoFormComponent } from '../empleado-form/empleado-form.component';
import { ExportService } from '../../../../core/services/export.service';
import { AuthService } from '../../../../core/services/auth.service';
import { PhonePipe } from '../../../../shared/pipes/phone.pipe';

@Component({
  selector: 'app-empleado-list',
  standalone: true,
  imports: [
    CommonModule, MatTableModule, MatButtonModule, MatIconModule, MatTooltipModule,
    MatPaginatorModule, MatFormFieldModule, MatInputModule, PhonePipe,
    MatSortModule, TranslateModule,
  ],
  providers: [DatePipe],
  templateUrl: './empleado-list.component.html',
  styleUrls: ['./empleado-list.component.scss'],
})
export class EmpleadoListComponent implements OnInit, AfterViewInit {
  // Propiedades
  canExportExcel = false;
  canExportPdf = false;
  canCreate = false;
  canEdit = false;
  canDelete = false;
  displayedColumns: string[] = ['nombreCompleto', 'rolCargo', 'telefonoContacto', 'modeloDePago', 'costoPorHora', 'activo', 'fechaContratacion', 'notas', 'acciones'];
  dataSource = new MatTableDataSource<Empleado>([]);
  @ViewChild(MatPaginator) paginator!: MatPaginator;
  totalElements: number = 0;
  pageSize: number = 5;
  currentPage: number = 0;
  filterValue: string = '';
  currentSort: string = 'nombreCompleto';
  sortDirection: string = 'asc';

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
  }

  private setPermissions(): void {
    this.canCreate = this.authService.hasPermission('EMPLEADO_CREATE');
    this.canEdit = this.authService.hasPermission('EMPLEADO_UPDATE');
    this.canDelete = this.authService.hasPermission('EMPLEADO_DELETE');
    const userPlan = this.authService.currentUserPlan();
    const hasPremiumPlan = userPlan === 'NEGOCIOS' || userPlan === 'PROFESIONAL';
    this.canExportExcel = this.authService.hasPermission('EXPORT_EXCEL') && hasPremiumPlan;
    this.canExportPdf = this.authService.hasPermission('EXPORT_PDF') && hasPremiumPlan;
  }

  loadEmpleados(): void {
    const sortParam = `${this.currentSort},${this.sortDirection}`;
    this.empleadoService.getEmpleados(this.currentPage, this.pageSize, this.filterValue, sortParam)
      .subscribe({
        next: (response) => {
          this.dataSource.data = response.content;
          this.totalElements = response.totalElements;
          this.cdr.detectChanges();
        },
        error: (error) => {
          console.error('Error al cargar empleados:', error);
          this.snackBar.open(
            this.translate.instant('EMPLOYEES.ERROR_LOADING'),
            this.translate.instant('GLOBAL.CLOSE'),
            { duration: 5000 }
          );
        },
      });
  }

  applyFilter(filterValue: string): void {
    this.filterValue = filterValue.trim().toLowerCase();
    this.paginator.pageIndex = 0;
    this.currentPage = 0;
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

  deleteEmpleado(id: number | undefined): void {
    const closeAction = this.translate.instant('GLOBAL.CLOSE');
    if (!id) {
      this.snackBar.open(this.translate.instant('EMPLOYEES.ERROR_INVALID_ID_FOR_DELETE'), closeAction, { duration: 3000 });
      return;
    }

    if (confirm(this.translate.instant('GLOBAL.CONFIRM_DEACTIVATE'))) {
      this.empleadoService.deactivateEmpleado(id).subscribe({
        next: () => {
          this.snackBar.open(this.translate.instant('EMPLOYEES.SUCCESSFULLY_DEACTIVATED'), closeAction, { duration: 3000 });
          this.loadEmpleados();
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
    this.currentPage = event.pageIndex;
    this.pageSize = event.pageSize;
    this.loadEmpleados();
  }

  onSortChange(sort: Sort) {
    this.currentSort = sort.direction ? sort.active : 'nombreCompleto';
    this.sortDirection = sort.direction || 'asc';
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