// src/app/modules/clientes/components/cliente-list/cliente-list.component.ts

import { Component, ViewChild, AfterViewInit, ChangeDetectorRef, OnInit, inject } from '@angular/core';
import { MatTableDataSource, MatTableModule } from '@angular/material/table';
import { MatPaginator, MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatTooltipModule } from '@angular/material/tooltip';
import { CommonModule, DatePipe } from '@angular/common';
import { Cliente } from '../../models/cliente.model';
import { ClienteService } from '../../services/cliente.service';
import { ClienteFormComponent } from '../cliente-form/cliente-form.component';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ExportService } from '../../../../core/services/export.service';
import { MatSortModule, Sort } from '@angular/material/sort';
import { HttpErrorResponse } from '@angular/common/http';
import { AuthService } from '../../../../core/services/auth.service';
import { PhonePipe } from '../../../../shared/pipes/phone.pipe';
// 👉 1. Importa TranslateModule y TranslateService
import { TranslateModule, TranslateService } from '@ngx-translate/core';

@Component({
  selector: 'app-cliente-list',
  standalone: true,
  imports: [
    CommonModule, MatTableModule, MatPaginatorModule, MatButtonModule, MatIconModule,
    MatFormFieldModule, MatInputModule, MatTooltipModule, MatDialogModule,
    MatSortModule, PhonePipe, TranslateModule, // 👈 2. Agrega TranslateModule
  ],
  providers: [DatePipe],
  templateUrl: './cliente-list.component.html',
  styleUrls: ['./cliente-list.component.scss'],
})
export class ClienteListComponent implements OnInit, AfterViewInit {
  // ... (propiedades existentes sin cambios)
  dataSource = new MatTableDataSource<Cliente>([]);
  displayedColumns: string[] = ['nombreCliente', 'telefonoContacto', 'direccion', 'fechaRegistro', 'notas', 'acciones'];
  @ViewChild(MatPaginator) paginator!: MatPaginator;
  totalElements = 0;
  pageSize = 5;
  pageNumber = 0;
  filterValue = '';
  currentSort = 'nombreCliente';
  sortDirection = 'asc';
  canExportExcel = false;
  canExportPdf = false;
  canCreate = false;
  canEdit = false;
  canDelete = false;

  // 👉 3. Inyecta TranslateService
  private clienteService = inject(ClienteService);
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
    this.loadClientes();
  }

  private setPermissions(): void {
    this.canCreate = this.authService.hasPermission('CLIENTE_CREATE');
    this.canEdit = this.authService.hasPermission('CLIENTE_UPDATE');
    this.canDelete = this.authService.hasPermission('CLIENTE_DELETE');
    const userPlan = this.authService.currentUserPlan();
    const hasPremiumPlan = userPlan === 'NEGOCIOS' || userPlan === 'PROFESIONAL';
    // Solo puedes exportar si tienes el plan PREMIUM, ya no es necesario el permiso de EXPORT  
    this.canExportExcel = hasPremiumPlan;
    this.canExportPdf = hasPremiumPlan;
    //this.canExportExcel = this.authService.hasPermission('EXPORT_EXCEL') && hasPremiumPlan;
    //this.canExportPdf = this.authService.hasPermission('EXPORT_PDF') && hasPremiumPlan;
  }

  loadClientes(): void {
    const sortParam = `${this.currentSort},${this.sortDirection}`;
    this.clienteService.getClientes(this.pageNumber, this.pageSize, this.filterValue, sortParam)
      .subscribe({
        next: (response) => {
          this.dataSource.data = response.content;
          this.totalElements = response.totalElements;
          this.pageNumber = response.number;
          this.cdr.detectChanges();
        },
        error: (err) => {
          console.error('Error al cargar clientes:', err);
          // 👉 4. Usa traducciones en los mensajes de error
          this.snackBar.open(
            this.translate.instant('CLIENTS.ERROR_LOADING'),
            this.translate.instant('GLOBAL.CLOSE'),
            { duration: 5000 }
          );
        },
      });
  }

  applyFilter(filterValue: string): void {
    this.filterValue = filterValue.trim().toLowerCase();
    this.paginator.pageIndex = 0;
    this.pageNumber = 0;
    this.loadClientes();
  }

  applyFilterIfEmpty(filterValue: string): void {
    if (filterValue === '') {
      this.applyFilter('');
    }
  }

  openForm(cliente?: Cliente): void {
    const dialogRef = this.dialog.open(ClienteFormComponent, {
      width: '500px',
      data: cliente || null,
    });
    dialogRef.afterClosed().subscribe((result) => {
      if (result) {
        this.loadClientes();
      }
    });
  }

  deleteCliente(id: number): void {
    // 👉 5. Traduce el mensaje de confirmación
    const confirmMessage = this.translate.instant('GLOBAL.CONFIRM_DELETE');
    if (confirm(confirmMessage)) {
      this.clienteService.deleteCliente(id).subscribe({
        next: () => {
          this.loadClientes();
          // 👉 6. Traduce el mensaje de éxito
          this.snackBar.open(
            this.translate.instant('GLOBAL.SUCCESSFULLY_DELETED'),
            this.translate.instant('GLOBAL.CLOSE'),
            { duration: 3000 }
          );
        },
        error: (err: HttpErrorResponse) => {
          // Lógica corregida: la respuesta del backend siempre es una clave de error.
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

  onPageChange(event: PageEvent): void {
    this.pageNumber = event.pageIndex;
    this.pageSize = event.pageSize;
    this.loadClientes();
  }

  onSortChange(sort: Sort): void {
    this.currentSort = sort.direction ? sort.active : 'nombreCliente';
    this.sortDirection = sort.direction || 'asc';
    this.loadClientes();
  }

  exportToExcel(): void {
    const sortParam = `${this.currentSort},${this.sortDirection}`;
    const apiUrl = this.clienteService.getApiUrl() + '/export/excel';
    this.exportService.exportToExcel(apiUrl, this.filterValue, sortParam)
      .subscribe({
        next: (response) => {
          this.exportService.downloadFile(response, 'Reporte_Clientes.xlsx', 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet');
        },
        error: (error) => {
          console.error('Error al exportar a Excel:', error);
          // 👉 7. Traduce los mensajes de error de exportación
          this.snackBar.open(
            this.translate.instant('CLIENTS.ERROR_EXPORT_EXCEL'),
            this.translate.instant('GLOBAL.CLOSE'),
            { duration: 5000 }
          );
        }
      });
  }

  exportToPdf(): void {
    const sortParam = `${this.currentSort},${this.sortDirection}`;
    const apiUrl = this.clienteService.getApiUrl() + '/export/pdf';
    this.exportService.exportToPdf(apiUrl, this.filterValue, sortParam)
      .subscribe({
        next: (response) => {
          this.exportService.downloadFile(response, 'Reporte_Clientes.pdf', 'application/pdf');
        },
        error: (error) => {
          console.error('Error al exportar a PDF:', error);
          this.snackBar.open(
            this.translate.instant('CLIENTS.ERROR_EXPORT_PDF'),
            this.translate.instant('GLOBAL.CLOSE'),
            { duration: 5000 }
          );
        }
      });
  }
}