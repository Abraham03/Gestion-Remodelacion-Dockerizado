import { Component, OnInit, ViewChild, AfterViewInit } from '@angular/core';
import { MatTableDataSource } from '@angular/material/table';
import { MatPaginator, PageEvent } from '@angular/material/paginator';
import { MatDialog } from '@angular/material/dialog';
import { ProyectosService } from '../../services/proyecto.service';
import { Proyecto } from '../../models/proyecto.model';
import { CommonModule, DatePipe } from '@angular/common';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatPaginatorModule } from '@angular/material/paginator';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { ProyectosFormComponent } from '../proyecto-form/proyectos-form.component';
import { MatSortModule, Sort } from '@angular/material/sort'; // Importado MatSortModule
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatChipsModule } from '@angular/material/chips'; // <-- Importar MatChipsModule
import { ExportService } from '../../../../core/services/export.service';

@Component({
  selector: 'app-proyectos-list',
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
    MatSortModule,
    MatChipsModule,
    MatProgressBarModule,
  ],
  templateUrl: './proyecto-list.component.html',
  styleUrls: ['./proyecto-list.component.scss'],
  providers: [DatePipe],
})
export class ProyectosListComponent implements AfterViewInit {
  proyectos: Proyecto[] = [];
  dataSource = new MatTableDataSource<Proyecto>([]);
  displayedColumns: string[] = [
    'nombreProyecto',
    'nombreCliente',
    'nombreEmpleadoResponsable',
    'estado',
    'progresoPorcentaje',
    'fechaInicio',
    'fechaFinEstimada',
    'acciones',
  ];

  @ViewChild(MatPaginator) paginator!: MatPaginator;

  totalElements: number = 0; // Total de elementos en el backend
  pageSize: number = 5; // Tamaño de página por defecto
  currentPage: number = 0; // Página actual (basado en 0)
  currentSort = 'nombreProyecto'; // Columna por defecto para ordenar
  sortDirection = 'asc'; // Dirección de ordenamiento ('asc' o 'desc')
  filterValue = ''; // Valor actual del filtro

  constructor(
    private proyectosService: ProyectosService,
    private dialog: MatDialog,
    private snackBar: MatSnackBar,
    private exportService: ExportService
  ) {}

  ngAfterViewInit(): void {
    // Almacena la referencia a la suscripción
    this.paginator.page.subscribe((event: PageEvent) => {
      // 1. Actualiza las variables de estado con los valores del evento
      this.currentPage = event.pageIndex;
      this.pageSize = event.pageSize;
      // 2. Llama al método de carga de datos con los nuevos valores
      this.loadProyectos();
    });
    this.loadProyectos();
  }

  /**
   * Carga la lista de proyectos desde el servicio, aplicando paginación,
   * ordenamiento y filtro.
   */
  loadProyectos(): void {
    const sortParam = `${this.currentSort},${this.sortDirection}`;
    this.proyectosService
      .getProyectosPaginated(
        this.currentPage,
        this.pageSize,
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

  /**
   * Abre el formulario para añadir o editar un proyecto.
   * @param proyecto El proyecto a editar, o `undefined` si es un nuevo proyecto.
   */
  openForm(proyecto?: Proyecto): void {
    const dialogRef = this.dialog.open(ProyectosFormComponent, {
      width: '800px',
      data: proyecto || null,
    });

    dialogRef.afterClosed().subscribe((result) => {
      if (result) {
        // Si el formulario se cerró con éxito (se añadió/editó), recargar la lista
        this.loadProyectos();
      }
    });
  }

  /**
   * Elimina un proyecto de la base de datos.
   * @param id El ID del proyecto a eliminar.
   */
  deleteProyecto(id: number): void {
    if (confirm('¿Estás seguro de eliminar este proyecto?')) {
      this.proyectosService.deleteProyecto(id).subscribe({
        next: () => {
          this.loadProyectos(); // Recargar la lista después de la eliminación
          // Opcional: Mostrar un mensaje de éxito (ej. con MatSnackBar)
        },
        error: (err) => {
          console.error('Error al eliminar proyecto:', err);
          // Opcional: Mostrar un mensaje de error
        },
      });
    }
  }

  /**
   * Aplica un filtro a la lista de proyectos.
   * @param event El evento del campo de entrada.
   */
  applyFilter(filterValue: String): void {
    this.filterValue = filterValue
      .trim()
      .toLowerCase();
    this.paginator.pageIndex = 0; // Resetear a la primera página al aplicar un nuevo filtro
    this.loadProyectos();
  }

      applyFilterIfEmpty(filterValue: string): void {
    // Si el usuario ha borrado todo el texto del campo de búsqueda
    if (filterValue === '') {
      this.applyFilter(''); // Llama al filtro con un string vacío
    }
  }
  

  // Nuevo método para asignar colores a los chips de estado
  getEstadoColor(estado: string): 'primary' | 'accent' | 'warn' | 'basic' {
    switch (estado) {
      case 'PENDIENTE':
        return 'basic';
      case 'EN_PROGRESO':
        return 'primary';
      case 'FINALIZADO':
        return 'accent';
      case 'CANCELADO':
      case 'EN_PAUSA':
        return 'warn';
      default:
        return 'basic';
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
    this.loadProyectos();
  }

  exportToExcel(): void {
    const sortParam = `${this.currentSort},${this.sortDirection}`;
    const apiUrl = this.proyectosService.getApiUrl() + '/export/excel'; // ⭐️ Corrección: Se usa un nuevo método en el servicio para obtener la URL
    this.exportService
      .exportToExcel(apiUrl, this.filterValue, sortParam)
      .subscribe({
        next: (response) => {
          this.exportService.downloadFile(
            response,
            'Reporte_Proyectos.xlsx',
            'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
          );
        },
        error: (error) => {
          console.error('Error al exportar a Excel:', error);
          this.snackBar.open('Error al exportar a Excel.', 'Cerrar', {
            duration: 5000,
          });
        },
      });
  }

  exportToPdf(): void {
    const sortParam = `${this.currentSort},${this.sortDirection}`;
    const apiUrl = this.proyectosService.getApiUrl() + '/export/pdf'; // ⭐️ Corrección: Se usa un nuevo método en el servicio para obtener la URL
    this.exportService
      .exportToPdf(apiUrl, this.filterValue, sortParam)
      .subscribe({
        next: (response) => {
          this.exportService.downloadFile(
            response,
            'Reporte_Proyectos.pdf',
            'application/pdf'
          );
        },
        error: (error) => {
          console.error('Error al exportar a PDF:', error);
          this.snackBar.open('Error al exportar a PDF.', 'Cerrar', {
            duration: 5000,
          });
        },
      });
  }
}
