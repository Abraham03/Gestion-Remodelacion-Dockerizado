import { CommonModule, DatePipe, DecimalPipe } from '@angular/common';
import { AfterViewInit, ChangeDetectorRef, Component, inject, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatPaginator, MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatTableDataSource, MatTableModule } from '@angular/material/table';
import { MatTooltipModule } from '@angular/material/tooltip';
import { Subject, takeUntil } from 'rxjs';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatSort, MatSortModule, Sort } from '@angular/material/sort';
import { HttpErrorResponse } from '@angular/common/http';
import { TranslateModule, TranslateService } from '@ngx-translate/core';

import { HorasTrabajadas } from '../../models/horas-trabajadas';
import { HorasTrabajadasService } from '../../services/horas-trabajadas.service';
import { HorasTrabajadasFormComponent } from '../horas-trabajadas-form/horas-trabajadas-form.component';
import { AuthService } from '../../../../core/services/auth.service';
import { ExportService } from '../../../../core/services/export.service';

@Component({
  selector: 'app-horas-trabajadas-list',
  standalone: true,
  imports: [
    CommonModule, MatTableModule, MatButtonModule, MatIconModule, MatTooltipModule,
    MatPaginatorModule, MatFormFieldModule, MatInputModule, MatDialogModule,
    DatePipe, DecimalPipe, MatSortModule, TranslateModule
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
  dataSource = new MatTableDataSource<HorasTrabajadas>([]);
  totalElements = 0;
  pageSize = 5;
  currentPage = 0;
  filterValue = '';
  currentSort = 'fecha';
  sortDirection = 'desc';

  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;

  private destroy$ = new Subject<void>();

  // Inyección de servicios
  private horasTrabajadasService = inject(HorasTrabajadasService);
  private dialog = inject(MatDialog);
  private snackBar = inject(MatSnackBar);
  private exportService = inject(ExportService);
  private cdr = inject(ChangeDetectorRef);
  private authService = inject(AuthService);
  private translate = inject(TranslateService);

  ngOnInit(): void {
    this.setPermissions();
  }

  ngAfterViewInit() {
    this.dataSource.sort = this.sort;
    this.paginator.page.pipe(takeUntil(this.destroy$)).subscribe((event: PageEvent) => {
      this.currentPage = event.pageIndex;
      this.pageSize = event.pageSize;
      this.loadHorasTrabajadas();
    });
    this.loadHorasTrabajadas();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private setPermissions(): void {
    this.canCreate = this.authService.hasPermission('HORASTRABAJADAS_CREATE');
    this.canEdit = this.authService.hasPermission('HORASTRABAJADAS_UPDATE');
    this.canDelete = this.authService.hasPermission('HORASTRABAJADAS_DELETE');
    const userPlan = this.authService.currentUserPlan();
    const hasPremiumPlan = userPlan === 'NEGOCIOS' || userPlan === 'PROFESIONAL';
    // Podra exportar solo si tiene un plan premium
    this.canExportExcel = hasPremiumPlan;
    this.canExportPdf = hasPremiumPlan;
    //this.canExportExcel = this.authService.hasPermission('EXPORT_EXCEL') && hasPremiumPlan;
    //this.canExportPdf = this.authService.hasPermission('EXPORT_PDF') && hasPremiumPlan;
  }

  loadHorasTrabajadas(): void {
    const sortParam = `${this.currentSort},${this.sortDirection}`;
    this.horasTrabajadasService.getHorasTrabajadasPaginated(this.currentPage, this.pageSize, this.filterValue, sortParam)
      .subscribe({
        next: (response) => {
          this.dataSource.data = response.content;
          this.totalElements = response.totalElements;
          this.cdr.detectChanges();
        },
        error: (error) => {
          console.error('Error al cargar horas trabajadas:', error);
          this.snackBar.open(this.translate.instant('WORK_HOURS.ERROR_LOADING'), this.translate.instant('GLOBAL.CLOSE'), { duration: 5000 });
        },
      });
  }

  applyFilter(filterValue: string): void {
    this.filterValue = filterValue.trim().toLowerCase();
    this.currentPage = 0;
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
    this.currentPage = event.pageIndex;
    this.pageSize = event.pageSize;
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