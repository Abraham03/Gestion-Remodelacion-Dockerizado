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
import { HorasTrabajadas } from '../../models/horas-trabajadas';
import { HorasTrabajadasService } from '../../services/horas-trabajadas.service';
import { HorasTrabajadasFormComponent } from '../horas-trabajadas-form/horas-trabajadas-form.component';
import { Subject, takeUntil } from 'rxjs';
import { AuthService } from '../../../../core/services/auth.service';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatSort, MatSortModule, Sort } from '@angular/material/sort';
import { ExportService } from '../../../../core/services/export.service';
import { HttpErrorResponse } from '@angular/common/http';

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
  providers: [DatePipe],
  templateUrl: './horas-trabajadas-list.component.html',
  styleUrl: './horas-trabajadas-list.component.scss'
})
export class HorasTrabajadasListComponent implements OnInit, AfterViewInit {
   // Propiedades de permisos basados en el plan
  canExportExcel = false;
  canExportPdf = false;
  canCreate = false;
  canEdit = false;
  canDelete = false; 
  displayedColumns: string[] = [
    'fecha',
    'nombreEmpleado',
    'nombreProyecto',
    'horas',
    'montoTotal',
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

  constructor(
    private horasTrabajadasService: HorasTrabajadasService,
    private dialog: MatDialog,
    private snackBar: MatSnackBar,
    private exportService: ExportService,
    private cdr: ChangeDetectorRef,
    private authService: AuthService
  ) {}

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

    // Permisos basados en el plan del usuario
    const userPlan = this.authService.currentUserPlan(); // Obtiene el valor de la señal

    // Lógica: Solo los planes NEGOCIOS y PROFESIONAL pueden exportar.
    const hasPremiumPlan = userPlan === 'NEGOCIOS' || userPlan === 'PROFESIONAL';

    this.canExportExcel = this.authService.hasPermission('EXPORT_EXCEL') && hasPremiumPlan;
    this.canExportPdf = this.authService.hasPermission('EXPORT_PDF') && hasPremiumPlan;
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

            if(this.paginator){
            this.paginator.length = response.totalElements;
            this.paginator.pageIndex = response.number;
            this.paginator.pageSize = response.size;              
            }
            
            this.cdr.detectChanges();
          
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
  applyFilter(filterValue: String): void {
    this.filterValue = filterValue.trim().toLowerCase();
    this.currentPage = 0; // Resetear la página al aplicar un filtro
    this.loadHorasTrabajadas();
  }

  applyFilterIfEmpty(filterValue: string): void {
    // Si el usuario ha borrado todo el texto del campo de búsqueda
    if (filterValue === '') {
      this.applyFilter(''); // Llama al filtro con un string vacío
    }
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
            this.snackBar.open('Registro eliminado correctamente', 'Cerrar', { duration: 5000 });
            this.loadHorasTrabajadas(); // Recargar la lista después de eliminar
          },
          error: (err: HttpErrorResponse) => {
            console.error('Error al eliminar el registro:', err);
            if (err.status === 404) {
              this.snackBar.open(err.error.message, 'Cerrar', { duration: 5000 });
            }
            
          },
        });
    }
  }

  onPageChange(event: PageEvent): void {
    this.currentPage = event.pageIndex;
    this.pageSize = event.pageSize;
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
