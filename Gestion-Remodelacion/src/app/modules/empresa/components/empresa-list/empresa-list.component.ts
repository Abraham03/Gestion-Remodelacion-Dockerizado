// src/app/modules/empresas/components/empresa-list/empresa-list.component.ts

import { Component, ViewChild, AfterViewInit, ChangeDetectorRef, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatTableDataSource, MatTableModule } from '@angular/material/table';
import { MatPaginator, MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatSort, MatSortModule, Sort } from '@angular/material/sort';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatChipsModule } from '@angular/material/chips';
import { HttpErrorResponse } from '@angular/common/http';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { Empresa } from '../../model/Empresa';
import { EmpresaService } from '../../service/empresa.service';
import { AuthService } from '../../../../core/services/auth.service';
import { EmpresaFormComponent } from '../empresa-form/empresa-form.component';
import { PhonePipe } from '../../../../shared/pipes/phone.pipe';
import { EmpresaQuery } from '../../state/empresas.query';
import { AsyncPipe } from '@angular/common';
import { Observable } from 'rxjs';

@Component({
  selector: 'app-empresa-list',
  standalone: true,
  imports: [
    CommonModule, MatTableModule, MatPaginatorModule, MatSortModule, MatDialogModule,
    MatInputModule, MatFormFieldModule, MatIconModule, MatTooltipModule,
    MatButtonModule, MatSnackBarModule, TranslateModule, MatChipsModule, PhonePipe, 
    MatProgressBarModule,AsyncPipe 
  ],
  templateUrl: './empresa-list.component.html',
  styleUrls: ['./empresa-list.component.scss']
})
export class EmpresaListComponent implements OnInit, AfterViewInit {
  // Permisos (solo SUPER_USUARIO podrá ver esto)
  canCreate = false;
  canEdit = false;
  canChangeStatus = false;

  displayedColumns: string[] = ['nombreEmpresa', 'plan', 'estadoSuscripcion', 'activo', 'fechaFinSuscripcion','telefono', 'acciones'];

  // Se inyecta el Query de empresas
  private empresaQuery = inject(EmpresaQuery);

  // Se define Observable para los datos y el estado de carga
  empresas$: Observable<Empresa[]> = this.empresaQuery.selectAll();
  loading$: Observable<boolean> = this.empresaQuery.selectLoading();

  totalElements = 0;
  pageSize = 10;
  currentPage = 0;
  filterValue = '';
  currentSort = 'nombreEmpresa';
  sortDirection = 'asc';

  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;

  private empresaService = inject(EmpresaService);
  private dialog = inject(MatDialog);
  private snackBar = inject(MatSnackBar);
  private cdr = inject(ChangeDetectorRef);
  private authService = inject(AuthService);
  private translate = inject(TranslateService);

  ngOnInit(): void {
    this.setPermissions();
  }

  ngAfterViewInit(): void {
    this.loadEmpresas();
  }

  private setPermissions(): void {
    // El módulo solo será visible para el SUPER_USUARIO, por lo que los permisos internos son totales.
    this.canCreate = this.authService.hasPermission('EMPRESA_CREATE');
    this.canEdit = this.authService.hasPermission('EMPRESA_UPDATE');
    this.canChangeStatus = this.authService.hasPermission('EMPRESA_UPDATE'); // Reutilizamos el permiso
  }

  loadEmpresas(): void {
    const sortParam = `${this.currentSort},${this.sortDirection}`;
    this.empresaService.getEmpresas(this.currentPage, this.pageSize, this.filterValue, sortParam)
      .subscribe();
  }

  applyFilter(filterValue: string): void {
    this.filterValue = filterValue.trim().toLowerCase();
    this.paginator.pageIndex = 0;
    this.currentPage = 0;
    this.loadEmpresas();
  }

  openForm(empresa?: Empresa): void {
    const dialogRef = this.dialog.open(EmpresaFormComponent, {
      width: '600px',
      data: empresa || null,
    });
    dialogRef.afterClosed().subscribe((result) => {
      if (result) this.loadEmpresas();
    });
  }

  changeStatus(empresa: Empresa): void {
    const newStatus = !empresa.activo;
    const confirmMessage = newStatus 
      ? this.translate.instant('EMPRESAS.CONFIRM_ACTIVATE')
      : this.translate.instant('EMPRESAS.CONFIRM_DEACTIVATE');

    if (confirm(confirmMessage)) {
      this.empresaService.changeStatus(empresa.id!, newStatus).subscribe({
        next: () => {
          const successMessage = newStatus 
            ? this.translate.instant('EMPRESAS.SUCCESSFULLY_ACTIVATED')
            : this.translate.instant('EMPRESAS.SUCCESSFULLY_DEACTIVATED');
          this.snackBar.open(successMessage, this.translate.instant('GLOBAL.CLOSE'), { duration: 3000 });
          this.loadEmpresas();
        },
        error: (err) => this.handleApiError(err, 'error.unexpected'),
      });
    }
  }

  onPageChange(event: PageEvent): void {
    this.currentPage = event.pageIndex;
    this.pageSize = event.pageSize;
    this.loadEmpresas();
  }

  onSortChange(sort: Sort): void {
    this.currentSort = sort.active;
    this.sortDirection = sort.direction;
    this.loadEmpresas();
  }
  
  private handleApiError(err: HttpErrorResponse, defaultMessageKey: string): void {
    const errorKey = err.error?.message || defaultMessageKey;
    const translatedMessage = this.translate.instant(errorKey);
    this.snackBar.open(translatedMessage, this.translate.instant('GLOBAL.CLOSE'), { duration: 7000 });
  }

  // Métodos para estilos dinámicos de los chips
  getPlanChipColor(plan: string): string {
    switch (plan) {
      case 'PROFESIONAL': return 'primary';
      case 'NEGOCIOS': return 'accent';
      case 'BASICO': return 'warn';
      default: return '';
    }
  }

  getStatusChipColor(estado: string): string {
    switch (estado) {
      case 'ACTIVA': return 'accent';
      case 'CANCELADA': return 'warn';
      case 'VENCIDA': return 'warn';
      default: return '';
    }
  }
}