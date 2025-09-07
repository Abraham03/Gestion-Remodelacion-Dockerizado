// src/app/modules/dashboard/components/dashboard/dashboard.component.ts

import {
  Component,
  OnInit,
  AfterViewInit,
  ViewChild,
  OnDestroy,
} from '@angular/core';
import { CommonModule, CurrencyPipe } from '@angular/common';
import { HttpClientModule } from '@angular/common/http';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { BreakpointObserver, Breakpoints } from '@angular/cdk/layout';
import { forkJoin, Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { FormsModule } from '@angular/forms';

// Módulos de Angular Material
import { MatCardModule } from '@angular/material/card';
import { MatGridListModule } from '@angular/material/grid-list';
import { MatIconModule } from '@angular/material/icon';
import { MatTableModule, MatTableDataSource } from '@angular/material/table';
import { MatPaginator, MatPaginatorModule } from '@angular/material/paginator';
import { MatSort, MatSortModule } from '@angular/material/sort';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSelectModule } from '@angular/material/select';

// Chart.js y el Servicio
import { Chart, registerables } from 'chart.js';
import { DashboardService } from '../../services/dashboard.service';
import { DashboardProyecto } from '../../models/dashboard-proyecto.model';
import { DropdownItem } from '../../../../core/models/dropdown-item.model';
import { NotificationService } from '../../../../core/services/notification.service';

Chart.register(...registerables);

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    HttpClientModule,
    MatSnackBarModule,
    FormsModule,
    MatCardModule,
    MatGridListModule,
    MatIconModule,
    MatTableModule,
    MatPaginatorModule,
    MatSortModule,
    MatFormFieldModule,
    MatInputModule,
    MatProgressSpinnerModule,
    MatSelectModule,
  ],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss'],
})
export class DashboardComponent implements OnInit, AfterViewInit, OnDestroy {
  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;

  public proyectosData: any;
  public clientesData: any;
  filterValue: string = '';
  public isLoading = true;
  public displayedColumns: string[] = ['empleado', 'proyecto', 'horas', 'montoTotal'];
  public dataSource = new MatTableDataSource<any>([]);
  private charts: { [key: string]: Chart | null } = {};  public cols = 2;
  private destroy$ = new Subject<void>();

  // PROPIEDADES para el filtro de proyectos
  public availableProjects: DropdownItem[] = [];
  public proyectosSelectedId: number | null = null;  

  public proyectosYears: number[] = [];
  public proyectosSelectedYear: number = new Date().getFullYear();
  public proyectosSelectedMonth: number | null = null;

  public clientesYears: number[] = [];
  public clientesSelectedYear: number = new Date().getFullYear();
  public clientesSelectedMonth: number | null = null;

  public availableMonths = [
    { value: null, viewValue: 'Año Completo' },
    { value: 1, viewValue: 'Enero' },
    { value: 2, viewValue: 'Febrero' },
    { value: 3, viewValue: 'Marzo' },
    { value: 4, viewValue: 'Abril' },
    { value: 5, viewValue: 'Mayo' },
    { value: 6, viewValue: 'Junio' },
    { value: 7, viewValue: 'Julio' },
    { value: 8, viewValue: 'Agosto' },
    { value: 9, viewValue: 'Septiembre' },
    { value: 10, viewValue: 'Octubre' },
    { value: 11, viewValue: 'Noviembre' },
    { value: 12, viewValue: 'Diciembre' },
  ];
  constructor(
    private dashboardService: DashboardService,
    private snackBar: MatSnackBar,
    private breakpointObserver: BreakpointObserver,
    private notificationService: NotificationService
  ) 
  {
        console.log(`DashboardComponent está usando NotificationService con ID: ${(this.notificationService as any).instanceId}`);

  }

  ngOnInit(): void {
    this.setupResponsiveCols();
    this.loadInitialData();

    this.notificationService.dataChanges$
    .pipe(takeUntil(this.destroy$))
    .subscribe(() =>{
        console.log('DashboardComponent recibió una notificación. Recargando datos...');
       this.loadInitialData()
  });
  }

  ngAfterViewInit(): void {
    this.dataSource.paginator = this.paginator;
    this.dataSource.sort = this.sort;
  }

  ngOnDestroy(): void {
    Object.values(this.charts).forEach(chart => chart?.destroy());
    this.destroy$.next();
    this.destroy$.complete();
  }

  private setupResponsiveCols(): void {
    this.breakpointObserver
      .observe([
        Breakpoints.XSmall,

        Breakpoints.Small,

        Breakpoints.Medium,

        Breakpoints.Large,
      ])
      .pipe(takeUntil(this.destroy$))
      .subscribe((result) => {
        if (result.breakpoints[Breakpoints.XSmall]) {
          this.cols = 1;
        } else {
          this.cols = 2;
        }
      });
  }

  /**
   * Carga la lista de años UNA SOLA VEZ para poblar el dropdown.
   */
  loadInitialData(): void {
    this.isLoading = true;
    this.dashboardService.getAvailableYears().subscribe({
      next: (years) => {
        this.proyectosYears = years;
        this.clientesYears = years; // Asumimos que usan la misma lista de años

        // Seteamos los años seleccionados por defecto
        if (!this.proyectosYears.includes(this.proyectosSelectedYear)) {
          this.proyectosSelectedYear = this.proyectosYears[0] || new Date().getFullYear();
        }
        if (!this.clientesYears.includes(this.clientesSelectedYear)) {
          this.clientesSelectedYear = this.clientesYears[0] || new Date().getFullYear();
        }

        this.loadProjectsForFilters();
        // Usamos forkJoin para hacer ambas llamadas a la vez y esperar a que terminen
        forkJoin({
          proyectos: this.dashboardService.getProyectosSummary(this.proyectosSelectedYear, this.proyectosSelectedMonth),
          clientes: this.dashboardService.getClientesSummary(this.clientesSelectedYear, this.clientesSelectedMonth)
        }).subscribe({
          next: (responses) => {
            this.proyectosData = responses.proyectos;
            this.clientesData = responses.clientes;
            this.setupVisuals();
            this.isLoading = false;
          },
          error: (err) => this.handleError(err)
        });
      },
      error: (err) => this.handleError(err, 'No se pudo obtener la lista de años.')
    });
  }

  // Método para cargar los proyectos basados en el filtro de año/mes
  loadProjectsForFilters(): void {
    this.dashboardService.getProjectsForFilter(this.proyectosSelectedYear, this.proyectosSelectedMonth).subscribe({
      next: (projects) => {
        this.availableProjects = projects;
        this.proyectosSelectedId = null; // Reiniciar el proyecto seleccionado al cambiar el filtro
      },
      error: (err) => this.handleError(err, 'No se pudo obtener la lista de proyectos.')
    });
  }  
  /**
   * Carga los datos del dashboard para el AÑO SELECCIONADO.
   */
  loadProyectosData(): void {
    this.isLoading = true;
    this.dashboardService.getProyectosSummary(this.proyectosSelectedYear, this.proyectosSelectedMonth, this.proyectosSelectedId).subscribe({
      next: (data) => {
        this.proyectosData = data;
        this.setupVisuals(); // Actualiza los visuales
        this.isLoading = false;
      },
      error: (err) => this.handleError(err)
    });
  }


   loadClientesData(): void {
    this.isLoading = true;
    this.dashboardService.getClientesSummary(this.clientesSelectedYear, this.clientesSelectedMonth).subscribe({
      next: (data) => {
        this.clientesData = data;
        this.setupVisuals(); // Actualiza los visuales
        this.isLoading = false;
      },
      error: (err) => this.handleError(err)
    });
  }

    onProyectosYearChange(): void {
    this.proyectosSelectedMonth = null;
    this.proyectosSelectedId = null;
    this.loadProjectsForFilters();
    this.loadProyectosData();
  }
  onProyectosMonthChange(): void {
    this.proyectosSelectedId = null;
    this.loadProjectsForFilters();
    this.loadProyectosData();
  }

  onClientesYearChange(): void {
    this.clientesSelectedMonth = null;
    this.loadClientesData();
  }
  onClientesMonthChange(): void {
    this.loadClientesData();
  }  

  onProyectosChange(): void {
    this.loadProyectosData();
  }  

  private setupVisuals(): void {
    this.setupTable();
    setTimeout(() => this.setupCharts(), 0);
  }

  private setupTable(): void {
    if (!this.proyectosData?.horasPorEmpleadoProyecto) {
      this.dataSource.data = [];
      return;
    }
    const tableData = this.proyectosData.horasPorEmpleadoProyecto.map((item: any[]) => ({
      nombreEmpleado: item[1], 
      nombreProyecto: item[3],
      horas: item[4],
      montoTotal: item[5]
    }));
    
    this.dataSource.data = tableData;
    
    // Re-vinculamos el paginador al dataSource DESPUÉS de actualizar los datos.
    // Esto notifica al paginador sobre el nuevo conjunto de datos y lo hace funcionar.
    // Usamos un pequeño retraso (setTimeout) para asegurar que Angular ha procesado los cambios.
    setTimeout(() => {
      this.dataSource.paginator = this.paginator;
      this.dataSource.sort = this.sort;
    });
  }

    public applyFilter(filterValue: String): void {
    this.filterValue = filterValue
    .trim()
    .toLowerCase();
    this.dataSource.filter = filterValue.trim().toLowerCase();
    if (this.dataSource.paginator) {
      this.dataSource.paginator.firstPage();
    }
  }

    applyFilterIfEmpty(filterValue: string): void {
    // Si el usuario ha borrado todo el texto del campo de búsqueda
    if (filterValue === '') {
      this.applyFilter(''); // Llama al filtro con un string vacío
    }
  }  

 private setupCharts(): void {
    Object.values(this.charts).forEach(chart => chart?.destroy());
    
    if (this.proyectosData) {
      this.charts['proyectosEstado'] = this.createPieChart('estadoProyectosChart', this.proyectosData.proyectosPorEstado);
      this.charts['empleadosRol'] = this.createDoughnutChart('empleadosPorRolChart', this.proyectosData.empleadosPorRol);
      this.charts['horasProyecto'] = this.createBarChart('horasPorProyectoChart', this.proyectosData.horasPorProyecto);
    }
    if (this.clientesData) {
      this.charts['clientesMes'] = this.createLineChart('clientesPorMesChart', this.clientesData.clientesPorMes);
    }
  }

    private handleError(error: any, message: string = 'No se pudieron cargar los datos.'): void {
      console.error(message, error);
      this.isLoading = false;
      this.snackBar.open(message, 'Cerrar', { duration: 5000 });
  }

  // --- MÉTODOS DE CREACIÓN DE GRÁFICOS (CON LÓGICA COMPLETA Y VALIDACIONES) ---

  private createPieChart(elementId: string, data: any[][]): Chart | null {
    const ctx = document.getElementById(elementId) as HTMLCanvasElement;
    if (!ctx || !data || data.length === 0) return null;

    const labels = data.map((item) => item[0].replace('_', ' '));
    const values = data.map((item) => item[1]);

    return new Chart(ctx, {
      type: 'pie',
      data: {
        labels: labels,
        datasets: [
          {
            label: 'Proyectos',
            data: values,
            backgroundColor: [
              '#42A5F5',
              '#66BB6A',
              '#FFCA28',
              '#9E9E9E',
              '#EF5350',
            ],
            hoverOffset: 4,
          },
        ],
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: { legend: { position: 'top' } },
      },
    });
  }

  private createDoughnutChart(elementId: string, data: any[][]): Chart | null {
    const ctx = document.getElementById(elementId) as HTMLCanvasElement;
    if (!ctx || !data || data.length === 0) return null;

    const labels = data.map((item) => item[0]);
    const values = data.map((item) => item[1]);

    return new Chart(ctx, {
      type: 'doughnut',
      data: {
        labels: labels,
        datasets: [
          {
            label: 'Nº de Empleados',
            data: values,
            backgroundColor: [
              '#3F51B5',
              '#FF4081',
              '#4CAF50',
              '#00BCD4',
              '#FF9800',
              '#795548',
              '#607D8B',
            ],
            hoverOffset: 4,
          },
        ],
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: { legend: { position: 'top' } },
      },
    });
  }

  private createLineChart(
    elementId: string,
    data: [number, number, number][]
  ): Chart | null {
    const ctx = document.getElementById(elementId) as HTMLCanvasElement;
    if (!ctx || !data || data.length === 0) return null;

    const monthlyData = Array(12).fill(0);
    data.forEach((item) => {
      const monthIndex = item[1] - 1;
      if (monthIndex >= 0 && monthIndex < 12) {
        monthlyData[monthIndex] = item[2];
      }
    });

    return new Chart(ctx, {
      type: 'line',
      data: {
        labels: [
          'Ene',
          'Feb',
          'Mar',
          'Abr',
          'May',
          'Jun',
          'Jul',
          'Ago',
          'Sep',
          'Oct',
          'Nov',
          'Dic',
        ],
        datasets: [
          {
            label: 'Nuevos Clientes',
            data: monthlyData,
            borderColor: 'rgba(63, 81, 181, 1)',
            backgroundColor: 'rgba(63, 81, 181, 0.2)',
            fill: true,
            tension: 0.4,
          },
        ],
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        scales: { y: { beginAtZero: true } },
      },
    });
  }

  private createBarChart(
    elementId: string,
    data: [string, number][]
  ): Chart | null {
    const ctx = document.getElementById(elementId) as HTMLCanvasElement;
    if (!ctx || !data || data.length === 0) return null;

    // Ordenamos por el valor numérico (item[1]) y tomamos el top 5
    const sortedData = [...data].sort((a, b) => b[1] - a[1]).slice(0, 5);

    // La etiqueta (nombre) está en el índice 0
    const labels = sortedData.map((item) => item[0]);
    // El valor (horas) está en el índice 1
    const values = sortedData.map((item) => item[1]);

    return new Chart(ctx, {
      type: 'bar',
      data: {
        labels: labels,
        datasets: [
          {
            label: 'Horas Registradas',
            data: values,
            backgroundColor: '#42A5F5',
            borderRadius: 4,
          },
        ],
      },
      options: {
        indexAxis: 'y',
        responsive: true,
        maintainAspectRatio: false,
        plugins: { legend: { display: false } },
      },
    });
  }
}
