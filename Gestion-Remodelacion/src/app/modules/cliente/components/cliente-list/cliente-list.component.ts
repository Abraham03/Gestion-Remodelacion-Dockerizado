import { Component, ViewChild, AfterViewInit } from '@angular/core';
import { MatTableDataSource, MatTableModule } from '@angular/material/table';
import {
  MatPaginator,
  MatPaginatorModule,
  PageEvent,
} from '@angular/material/paginator';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatTooltipModule } from '@angular/material/tooltip';
import { CommonModule } from '@angular/common';
import { Cliente } from '../../models/cliente.model';
import { ClienteService } from '../../services/cliente.service';
import { ClienteFormComponent } from '../cliente-form/cliente-form.component';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ExportService } from '../../../../core/services/export.service';
import { MatSortModule, Sort } from '@angular/material/sort';
@Component({
  selector: 'app-cliente-list',
  standalone: true,
  imports: [
    CommonModule,
    MatTableModule,
    MatPaginatorModule,
    MatButtonModule,
    MatIconModule,
    MatFormFieldModule,
    MatInputModule,
    MatTooltipModule,
    MatDialogModule,
    MatSortModule,
  ],
  templateUrl: './cliente-list.component.html',
  styleUrls: ['./cliente-list.component.scss'],
})
export class ClienteListComponent implements AfterViewInit {
  dataSource = new MatTableDataSource<Cliente>([]);
  displayedColumns: string[] = [
    'nombreCliente',
    'telefonoContacto',
    'direccion',
    'fechaRegistro',
    'notas',
    'acciones',
  ]; // Ajusta según tu modelo Cliente
  cliente: Cliente[] = [];
  @ViewChild(MatPaginator) paginator!: MatPaginator;

  // Propiedades para la paginación y filtro del backend
  totalElements: number = 0;
  pageSize: number = 5;
  pageNumber: number = 0;
  filterValue: string = '';
  currentSort: string = 'nombreCliente';
  sortDirection: string = 'asc';
  constructor(
    private clienteService: ClienteService,
    private dialog: MatDialog,
    private snackBar: MatSnackBar,
    private exportService: ExportService
  ) {}

  ngAfterViewInit(): void {
    // La suscripción se hace aqui para evitar el error 'Cannot read properties of undefined'
    this.paginator.page.subscribe(() => this.loadClientes());
    this.loadClientes();
  }

  loadClientes(): void {
    const sortParam = `${this.currentSort},${this.sortDirection}`;
    this.clienteService
      .getClientes(
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
          this.pageSize = response.size;

        },
        error:(err) => {
          console.error('Error al cargar empleados:', err);
          this.snackBar.open(
            'Error al cargar los empleados. Inténtalo de nuevo más tarde.',
            'Cerrar',
            { duration: 5000 }
          );
        },
  });
  }

  applyFilter(event: Event): void {
    this.filterValue = (event.target as HTMLInputElement).value
      .trim()
      .toLowerCase();
    this.paginator.pageIndex = 0; // Resetear a la primera página al aplicar un nuevo filtro
    this.loadClientes();
  }
  
  openForm(cliente?: Cliente): void {
    const dialogRef = this.dialog.open(ClienteFormComponent, {
      width: '500px',
      data: cliente || null,
    });

    dialogRef.afterClosed().subscribe((result) => {
      if (result) {
        this.loadClientes(); // Recargar la lista después de crear/editar
      }
    });
  }

  deleteCliente(id: number): void {
    if (confirm('¿Estás seguro de eliminar este cliente?')) {
      this.clienteService.deleteCliente(id).subscribe({
        next: () => {
          this.loadClientes(); // Recargar la lista
          // TODO: Mostrar mensaje de éxito
        },
        error: (err) => {
          console.error('Error al eliminar cliente:', err);
          // TODO: Mostrar mensaje de error
        },
      });
    }
  }



  onPageChange(event: PageEvent): void {
    this.pageNumber = event.pageIndex;
    this.pageSize = event.pageSize;
    this.loadClientes();
  }

  // ⭐️ Nuevo método para capturar el cambio de orden
  onSortChange(sort: Sort): void {
    if (sort.direction) {
      this.currentSort = sort.active;
      this.sortDirection = sort.direction;
    } else {
      this.currentSort = 'nombreCliente'; // Default sort
      this.sortDirection = 'asc';
    }
    this.loadClientes();
  }

  // ⭐️ Métodos de exportación usando el servicio centralizado
  exportToExcel(): void {
    const sortParam = `${this.currentSort},${this.sortDirection}`;
    const apiUrl = this.clienteService.getApiUrl() + '/export/excel'; // ⭐️ Corrección: Se usa un nuevo método en el servicio para obtener la URL
    this.exportService.exportToExcel(apiUrl, this.filterValue, sortParam)
      .subscribe({
        next: (response) => {
          this.exportService.downloadFile(response, 'Reporte_Clientes.xlsx', 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet');
        },
        error: (error) => {
          console.error('Error al exportar a Excel:', error);
          this.snackBar.open('Error al exportar a Excel.', 'Cerrar', { duration: 5000 });
        }
      });
  }

  exportToPdf(): void {
    const sortParam = `${this.currentSort},${this.sortDirection}`;
    const apiUrl = this.clienteService.getApiUrl() + '/export/pdf'; // ⭐️ Corrección: Se usa un nuevo método en el servicio para obtener la URL
    this.exportService.exportToPdf(apiUrl, this.filterValue, sortParam)
      .subscribe({
        next: (response) => {
          this.exportService.downloadFile(response, 'Reporte_Clientes.pdf', 'application/pdf');
        },
        error: (error) => {
          console.error('Error al exportar a PDF:', error);
          this.snackBar.open('Error al exportar a PDF.', 'Cerrar', { duration: 5000 });
        }
      });
  }  

}
