import { CommonModule, DatePipe, DecimalPipe } from '@angular/common';
import { AfterViewInit, Component, inject, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatPaginator, MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatTableDataSource, MatTableModule } from '@angular/material/table';
import { MatTooltipModule } from '@angular/material/tooltip';
import { HorasTrabajadas } from '../../models/horas-trabajadas';
import { HorasTrabajadasService } from '../../services/horas-trabajadas.service';
import { HorasTrabajadasFormComponent } from '../horas-trabajadas-form/horas-trabajadas-form.component';
import { Subject, takeUntil } from 'rxjs';
import { AuthService } from '../../../../core/services/auth.service';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatSort, MatSortModule, Sort } from '@angular/material/sort';
import { ExportService } from '../../../../core/services/export.service';

@Component({
  selector: 'app-horas-trabajadas-list',
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
    MatDialogModule,
    DatePipe,
    DecimalPipe,
    MatSortModule 
  ],
  templateUrl: './horas-trabajadas-list.component.html',
  styleUrl: './horas-trabajadas-list.component.scss'
})
export class HorasTrabajadasListComponent implements AfterViewInit {
  displayedColumns: string[] = [
    'fecha',
    'nombreEmpleado',
    'nombreProyecto',
    'horas',
    'actividadRealizada',
    'acciones'
  ];
  dataSource = new MatTableDataSource<HorasTrabajadas>([]);
  totalElements: number = 0;
  pageSize: number = 5;
  currentPage: number = 0;
  filterValue: string  = '';
  currentSort = 'fecha'; // Columna por defecto para ordenar
  sortDirection = 'desc'; // Dirección por defecto

  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort; // Referencia al componente de ordenamiento

  private destroy$ = new Subject<void>();

  // Inyecta AuthService
  private authService = inject(AuthService);

  // Variables para controlar la visibilidad de los botones
  canCreate = false;
  canEdit = false;
  canDelete = false;
  constructor(
    private horasTrabajadasService: HorasTrabajadasService,
    private dialog: MatDialog,
    private snackBar: MatSnackBar,
    private exportService: ExportService
  ) {}

  ngAfterViewInit() {
    // Suscripción a los eventos de paginación
    this.paginator.page.pipe(takeUntil(this.destroy$)).subscribe((event: PageEvent) => {
      this.currentPage = event.pageIndex;
      this.pageSize = event.pageSize;
      this.loadHorasTrabajadas();
    });

    // Suscripción a los eventos de ordenamiento
    this.sort.sortChange.pipe(takeUntil(this.destroy$)).subscribe((sort: Sort) => {
        this.currentSort = sort.active;
        this.sortDirection = sort.direction;
        this.currentPage = 0; // Resetear a la primera página al cambiar el orden
        this.loadHorasTrabajadas();
    });

    this.loadHorasTrabajadas();

  }

    /**
   * Carga los registros de horas trabajadas aplicando paginación, ordenamiento y filtro.
   */
  loadHorasTrabajadas(): void {
  const sortParam = `${this.currentSort},${this.sortDirection}`;
    this.horasTrabajadasService
      .getHorasTrabajadasPaginated(
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
   * Aplica el filtro a la tabla de horas trabajadas.
   * @param event Evento del teclado para obtener el valor del filtro.
   */
  applyFilter(event: Event): void {
    const filterValue = (event.target as HTMLInputElement).value;
    this.filterValue = filterValue.trim().toLowerCase();
    this.currentPage = 0; // Resetear la página al aplicar un filtro
    this.loadHorasTrabajadas();
  }

  /**
   * Abre el formulario para registrar o editar horas trabajadas.
   * @param horasTrabajadas (Opcional) Objeto de horas trabajadas para editar.
   */
  openForm(horasTrabajadas?: HorasTrabajadas): void {
    const dialogRef = this.dialog.open(HorasTrabajadasFormComponent, {
      width: '500px',
      data: horasTrabajadas || null, // Pasa el objeto si es edición, o null si es nuevo registro
    });

    dialogRef.afterClosed().pipe(takeUntil(this.destroy$)).subscribe((result) => {
      if (result) {
        // Si el formulario devuelve un resultado (indicando éxito), recarga la lista
        this.loadHorasTrabajadas();
      }
    });
  }

  /**
   * Elimina un registro de horas trabajadas.
   * @param id ID del registro a eliminar.
   */
  deleteHorasTrabajadas(id: number): void {
    if (confirm('¿Estás seguro de que quieres eliminar este registro de horas trabajadas?')) {
      this.horasTrabajadasService
        .deleteHorasTrabajadas(id)
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: () => {
            console.log('Registro eliminado con éxito');
            this.loadHorasTrabajadas(); // Recargar la lista después de eliminar
          },
          error: (err) => {
            console.error('Error al eliminar el registro:', err);
            // Puedes mostrar un mensaje de error al usuario
          },
        });
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
    this.loadHorasTrabajadas();
  }

  exportToExcel(): void {
    const sortParam = `${this.currentSort},${this.sortDirection}`;
    const apiUrl = this.horasTrabajadasService.getApiUrl() + '/export/excel'; // ⭐️ Corrección: Se usa un nuevo método en el servicio para obtener la URL
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
    const apiUrl = this.horasTrabajadasService.getApiUrl() + '/export/pdf'; // ⭐️ Corrección: Se usa un nuevo método en el servicio para obtener la URL
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
