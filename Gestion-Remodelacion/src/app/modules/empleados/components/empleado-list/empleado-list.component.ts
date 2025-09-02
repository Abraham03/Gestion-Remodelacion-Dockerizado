import { Component, ViewChild, AfterViewInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { CommonModule } from '@angular/common';
import { MatTableModule, MatTableDataSource } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';
import {
  MatPaginatorModule,
  MatPaginator,
} from '@angular/material/paginator';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';

import { EmpleadoService } from '../../services/empleado.service';
import { Empleado } from '../../models/empleado.model';
import { EmpleadoFormComponent } from '../empleado-form/empleado-form.component';
import { ExportService } from '../../../../core/services/export.service';
import { Sort } from '@angular/material/sort';
import { HttpErrorResponse } from '@angular/common/http';

@Component({
  selector: 'app-empleado-list',
  standalone: true,
  imports: [
    CommonModule,
    MatTableModule,
    MatButtonModule,
    MatIconModule,
    MatTooltipModule,
    MatPaginatorModule,
    MatFormFieldModule,
    MatInputModule,
  ],
  templateUrl: './empleado-list.component.html',
  styleUrls: ['./empleado-list.component.scss'],
})
export class EmpleadoListComponent implements AfterViewInit {
  empleados: Empleado[] = [];
  displayedColumns: string[] = [
    'nombreCompleto',
    'rolCargo',
    'telefonoContacto',
    'costoPorHora',
    'activo',
    'fechaContratacion',
    'notas',
    'acciones',
  ];
  dataSource = new MatTableDataSource<Empleado>([]);

  @ViewChild(MatPaginator) paginator!: MatPaginator;

  totalElements: number = 0;
  pageSize: number = 5;
  currentPage: number = 0;
  filterValue: string = '';
  currentSort: string = 'nombreCompleto';
  sortDirection: string = 'asc';
  constructor(
    private empleadoService: EmpleadoService,
    private dialog: MatDialog,
    private snackBar: MatSnackBar,
    private exportService: ExportService
  ) {}

  ngAfterViewInit(): void {
    // La suscripción se hace aquí para evitar el error 'Cannot read properties of undefined'
    this.paginator.page.subscribe(() => this.loadEmpleados());
    // Carga inicial de datos
    this.loadEmpleados();
  }

  loadEmpleados(): void {
    const sortParam = `${this.currentSort},${this.sortDirection}`;
    this.empleadoService
      .getEmpleados(
        this.paginator.pageIndex,
        this.paginator.pageSize,
        this.filterValue,
        sortParam
      )
      .subscribe({
        next: (response) => {
            this.dataSource.data = response.content;
            this.totalElements = response.totalElements;

            this.paginator.length = response.totalElements;
            this.paginator.pageIndex = response.number;
            this.paginator.pageSize = response.size;
          
        },
        error: (error) => {
          console.error('Error al cargar empleados:', error);
          this.snackBar.open(
            'Error al cargar los empleados. Inténtalo de nuevo más tarde.',
            'Cerrar',
            { duration: 5000 }
          );
        },
      });
  }

  applyFilter(filterValue: String): void {
    this.filterValue = filterValue
      .trim()
      .toLowerCase();
    this.paginator.pageIndex = 0; // Actualizar el índice del paginador
    this.loadEmpleados(); // Llamar al servicio para filtrar
  }

      applyFilterIfEmpty(filterValue: string): void {
    // Si el usuario ha borrado todo el texto del campo de búsqueda
    if (filterValue === '') {
      this.applyFilter(''); // Llama al filtro con un string vacío
    }
  }

  openForm(empleado?: Empleado): void {
    const dialogRef = this.dialog.open(EmpleadoFormComponent, {
      width: '500px',
      data: empleado || null,
    });

    dialogRef.afterClosed().subscribe((result) => {
      if (result) {
        this.loadEmpleados();
      }
    });
  }

  deleteEmpleado(id: number | undefined): void {
    if (!id) {
      this.snackBar.open('ID de empleado no válido para eliminar.', 'Cerrar', {
        duration: 3000,
      });
      return;
    }

    if (
      confirm(
        '¿Estás seguro de desactivar este empleado? (Esto cambiará su estado a inactivo)'
      )
    ) {
      // Clarify action
      this.empleadoService.deactivateEmpleado(id).subscribe(
        // Call deactivateEmpleado
        () => {
          this.snackBar.open('Empleado desactivado exitosamente', 'Cerrar', {
            duration: 3000,
          });
          this.loadEmpleados();
        },
        (error: HttpErrorResponse) => {
          console.error('Error al desactivar empleado:', error);
          if (error.status == 409) {
            this.snackBar.open('El empleado ya está desactivado', 'Cerrar', {
              duration: 3000,
            });
          }
        }
      );
    }
  }

  onSortChange(sort: Sort) {
    if (sort.direction) {
      this.currentSort = sort.active;
      this.sortDirection = sort.direction;
    } else {
      this.currentSort = 'nombreCompleto'; // Default sort
      this.sortDirection = 'asc';
    }
    this.loadEmpleados();
  }

  exportToExcel(): void {
    const sortParam = `${this.currentSort},${this.sortDirection}`;
    const apiUrl = this.empleadoService.getApiUrl() + '/export/excel'; // ⭐️ Corrección: Se usa un nuevo método en el servicio para obtener la URL
    this.exportService.exportToExcel(apiUrl, this.filterValue, sortParam)
      .subscribe({
        next: (response) => {
          this.exportService.downloadFile(response, 'Reporte_Empleados.xlsx', 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet');
        },
        error: (error) => {
          console.error('Error al exportar a Excel:', error);
          this.snackBar.open('Error al exportar a Excel.', 'Cerrar', { duration: 5000 });
        }
      });
  } 
  
    exportToPdf(): void {
    const sortParam = `${this.currentSort},${this.sortDirection}`;
    const apiUrl = this.empleadoService.getApiUrl() + '/export/pdf'; // ⭐️ Corrección: Se usa un nuevo método en el servicio para obtener la URL
    this.exportService.exportToPdf(apiUrl, this.filterValue, sortParam)
      .subscribe({
        next: (response) => {
          this.exportService.downloadFile(response, 'Reporte_Empleados.pdf', 'application/pdf');
        },
        error: (error) => {
          console.error('Error al exportar a PDF:', error);
          this.snackBar.open('Error al exportar a PDF.', 'Cerrar', { duration: 5000 });
        }
      });
  } 

}
