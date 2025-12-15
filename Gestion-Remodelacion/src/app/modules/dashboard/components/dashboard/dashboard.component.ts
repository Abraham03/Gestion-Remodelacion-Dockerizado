// src/app/modules/dashboard/components/dashboard/dashboard.component.ts

import { Component, OnInit, AfterViewInit, ViewChild, OnDestroy, inject } from '@angular/core';
import { CommonModule, CurrencyPipe } from '@angular/common';
import { HttpClientModule } from '@angular/common/http';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { BreakpointObserver, Breakpoints } from '@angular/cdk/layout';
import { combineLatest, forkJoin, Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { FormsModule } from '@angular/forms';
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
import { Chart, registerables } from 'chart.js';
import { DropdownItem } from '../../../../core/models/dropdown-item.model';
//Importa TranslateModule y TranslateService
import { DashboardService } from '../../services/dashboard.service';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { DashboardQuery } from '../../state/dashboard.query';
import { DashboardSummary, DashboardClientes } from '../../models/dashboard.model';

Chart.register(...registerables);

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [
    CommonModule, HttpClientModule, MatSnackBarModule, FormsModule, MatCardModule,
    MatGridListModule, MatIconModule, MatTableModule, MatPaginatorModule,
    MatSortModule, MatFormFieldModule, MatInputModule, MatProgressSpinnerModule,
    MatSelectModule, TranslateModule, 
  ],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss'],
})
export class DashboardComponent implements OnInit, AfterViewInit, OnDestroy {
  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;

// Inyecciones
  private dashboardService = inject(DashboardService);
  public dashboardQuery = inject(DashboardQuery); // Inyectamos el Query
  private snackBar = inject(MatSnackBar);
  private breakpointObserver = inject(BreakpointObserver);
  private translate = inject(TranslateService);

// Variables Observables del Store (Reactividad)
  public isLoading$ = this.dashboardQuery.isLoading$;
  public years$ = this.dashboardQuery.years$;

  // VARIABLES UNIFICADAS DE FILTRO (El cambio clave)
  public selectedYear: number = new Date().getFullYear();
  public selectedMonth: number | null = null;
  public selectedProjectId: number | null = null;

  public availableMonths: { value: number | null, viewValue: string }[] = [];
  public availableProjects: DropdownItem[] = [];

  public displayedColumns: string[] = ['empleado', 'proyecto', 'horas', 'montoTotal'];
  public dataSource = new MatTableDataSource<any>([]);
  public cols = 2;
  private charts: { [key: string]: Chart | null } = {};
  private destroy$ = new Subject<void>();


  ngOnInit(): void {
    this.setupTranslations();
    this.setupResponsiveCols();
    
    // Inicialización
    this.initializeDashboard();
    
    // Suscripción a cambios del Store (Reactividad pura)
    this.subscribeToStoreChanges();
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



private initializeDashboard(): void {
    // 1. Cargar años. Cuando lleguen, si el año actual no está, seleccionar el primero.
    this.dashboardService.loadAvailableYears().subscribe({
      next: (years) => {
        console.log(years);
        if (years.length > 0 && !years.includes(this.selectedYear)) {
          this.selectedYear = years[0];
        }
        // 2. Una vez tenemos el año seguro, cargamos TODO.
        this.refreshAllData();
      },
      error: () => this.showError('DASHBOARD.ERROR_LOADING_YEARS')
    });
  }


  private refreshAllData(): void {
    // 1. Cargar Dropdown de proyectos (depende de año/mes)
    this.loadProjectsDropdown();

    // 2. Cargar KPI y Gráficos de Proyectos
    this.dashboardService.loadProyectosSummary(this.selectedYear, this.selectedMonth, this.selectedProjectId)
      .subscribe({ error: () => this.showError() });

    // 3. Cargar Gráfico de Clientes (AHORA USA EL MISMO AÑO 'selectedYear')
    this.dashboardService.loadClientesSummary(this.selectedYear, this.selectedMonth)
      .subscribe({ error: () => this.showError() });
  }

  private loadProjectsDropdown(): void {
    this.dashboardService.getProjectsForFilter(this.selectedYear, this.selectedMonth)
      .subscribe(projects => this.availableProjects = projects);
  }


  private subscribeToStoreChanges(): void {
    // Combinamos las fuentes de datos. Cuando el Store se actualice (por el servicio),
    // este código se ejecutará automáticamente y redibujará la vista.
    combineLatest([
      this.dashboardQuery.summary$,
      this.dashboardQuery.clientesSummary$
    ]).pipe(takeUntil(this.destroy$))
    .subscribe(([summary, clientes]) => {
      
      if (summary) {
        this.updateTable(summary);
        // Pequeño timeout para asegurar que el HTML del canvas esté listo
        setTimeout(() => this.updateProjectCharts(summary), 0);
      }
      
      if (clientes) {
        setTimeout(() => this.updateClientCharts(clientes), 0);
      }
    });
  }


  onYearChange(): void {
    // Al cambiar el año global: reseteamos mes y proyecto, recargamos TODO.
    this.selectedMonth = null;
    this.selectedProjectId = null;
    this.refreshAllData(); // Esto actualiza proyectos Y clientes
  }

  onMonthChange(): void {
    // Al cambiar mes: reseteamos proyecto, recargamos todo.
    this.selectedProjectId = null;
    this.refreshAllData();
  }

  onProjectChange(): void {
    // Al cambiar proyecto: Solo necesitamos recargar el Summary de proyectos.
    // (Clientes usualmente es por año/mes general, no por proyecto específico)
    this.dashboardService.loadProyectosSummary(this.selectedYear, this.selectedMonth, this.selectedProjectId)
      .subscribe({ error: () => this.showError() });
  }

 
private updateTable(data: DashboardSummary): void {
    if (!data.horasPorEmpleadoProyecto) {
      this.dataSource.data = [];
      return;
    }
    // Mapeo de datos (Array -> Objeto para la tabla)
    const tableData = data.horasPorEmpleadoProyecto.map((item: any[]) => ({
      nombreEmpleado: item[1], 
      nombreProyecto: item[3], 
      horas: item[4], 
      montoTotal: item[5]
    }));
    this.dataSource.data = tableData;
  }

private updateProjectCharts(data: DashboardSummary): void {
    if (this.charts['proyectosEstado']) this.charts['proyectosEstado']?.destroy();
    if (this.charts['empleadosRol']) this.charts['empleadosRol']?.destroy();
    if (this.charts['horasProyecto']) this.charts['horasProyecto']?.destroy();

    this.charts['proyectosEstado'] = this.createPieChart('estadoProyectosChart', data.proyectosPorEstado);
    this.charts['empleadosRol'] = this.createDoughnutChart('empleadosPorRolChart', data.empleadosPorRol);
    this.charts['horasProyecto'] = this.createBarChart('horasPorProyectoChart', data.horasPorProyecto);
  }

  private updateClientCharts(data: DashboardClientes): void {
    if (this.charts['clientesMes']) this.charts['clientesMes']?.destroy();
    this.charts['clientesMes'] = this.createLineChart('clientesPorMesChart', data.clientesPorMes);
  }

public applyFilter(filterValue: string): void {
    this.dataSource.filter = filterValue.trim().toLowerCase();
    if (this.dataSource.paginator) {
      this.dataSource.paginator.firstPage();
    }
  }

  private showError(key: string = 'DASHBOARD.ERROR_LOADING_DATA'): void {
    this.snackBar.open(this.translate.instant(key), this.translate.instant('GLOBAL.CLOSE'), { duration: 5000 });
  }

private setupResponsiveCols(): void {
     this.breakpointObserver.observe([Breakpoints.XSmall, Breakpoints.Small, Breakpoints.Medium, Breakpoints.Large])
      .pipe(takeUntil(this.destroy$))
      .subscribe((result) => {
        this.cols = result.breakpoints[Breakpoints.XSmall] ? 1 : 2;
      });
  }

  private setupTranslations(): void {
    this.generateMonthList(); 
    this.translate.onLangChange
      .pipe(takeUntil(this.destroy$))
      .subscribe(() => {
        this.generateMonthList(); 
        // Forzamos repintado de gráficos con nuevas etiquetas obteniendo datos actuales del Store
        const state = this.dashboardQuery.getValue();
        if(state.summary) this.updateProjectCharts(state.summary);
        if(state.clientesSummary) this.updateClientCharts(state.clientesSummary);
      });
  }


  private generateMonthList(): void {
    const monthKeys = ['JANUARY', 'FEBRUARY', 'MARCH', 'APRIL', 'MAY', 'JUNE', 'JULY', 'AUGUST', 'SEPTEMBER', 'OCTOBER', 'NOVEMBER', 'DECEMBER'];
    this.availableMonths = [
      { value: null, viewValue: this.translate.instant('MONTHS.FULL_YEAR') }
    ];
    monthKeys.forEach((key, index) => {
      this.availableMonths.push({
        value: index + 1,
        viewValue: this.translate.instant(`MONTHS.${key}`)
      });
    });
  }


  // --- MÉTODOS DE GRÁFICOS ACTUALIZADOS CON TRADUCCIONES ---

  private createPieChart(elementId: string, data: any[][]): Chart | null {
    const ctx = document.getElementById(elementId) as HTMLCanvasElement;
    if (!ctx || !data || data.length === 0) return null;
    
    // 👉 7. Traduce las etiquetas de los estados del proyecto
    const labels = data.map((item) => this.translate.instant(`DASHBOARD.STATE.${item[0]}`));
    const values = data.map((item) => item[1]);

    return new Chart(ctx, {
      type: 'pie',
      data: {
        labels: labels,
        datasets: [{
          // 👉 8. Traduce la etiqueta del dataset
          label: this.translate.instant('DASHBOARD.CHART_PROJECTS_LABEL'),
          data: values,
          backgroundColor: ['#42A5F5', '#66BB6A', '#FFCA28', '#9E9E9E', '#EF5350'],
          hoverOffset: 4,
        }],
      },
      options: { responsive: true, maintainAspectRatio: false, plugins: { legend: { position: 'top' } } },
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
        datasets: [{
          label: this.translate.instant('DASHBOARD.CHART_EMPLOYEES_LABEL'),
          data: values,
          backgroundColor: ['#3F51B5', '#FF4081', '#4CAF50', '#00BCD4', '#FF9800', '#795548'],
          hoverOffset: 4,
        }],
      },
      options: { responsive: true, maintainAspectRatio: false, plugins: { legend: { position: 'top' } } },
    });
  }


    private createLineChart(elementId: string, data: [number, number, number][]): Chart | null {
    const ctx = document.getElementById(elementId) as HTMLCanvasElement;
    if (!ctx || !data || data.length === 0) return null;

    const monthlyData = Array(12).fill(0);
    data.forEach((item) => {
      const monthIndex = item[1] - 1;
      if (monthIndex >= 0 && monthIndex < 12) {
        monthlyData[monthIndex] = item[2];
      }
    });

    // 👉 9. Traduce las etiquetas de los meses
    const monthShortKeys = ['JAN_SHORT', 'FEB_SHORT', 'MAR_SHORT', 'APR_SHORT', 'MAY_SHORT', 'JUN_SHORT', 'JUL_SHORT', 'AUG_SHORT', 'SEP_SHORT', 'OCT_SHORT', 'NOV_SHORT', 'DEC_SHORT'];
    const translatedLabels = monthShortKeys.map(key => this.translate.instant(`MONTHS.${key}`));

    return new Chart(ctx, {
      type: 'line',
      data: {
        labels: translatedLabels,
        datasets: [{
          label: this.translate.instant('DASHBOARD.CHART_NEW_CLIENTS_LABEL'),
          data: monthlyData,
          borderColor: 'rgba(63, 81, 181, 1)',
          backgroundColor: 'rgba(63, 81, 181, 0.2)',
          fill: true, tension: 0.4,
        }],
      },
      options: { responsive: true, maintainAspectRatio: false, scales: { y: { beginAtZero: true } } },
    });
  }

  private createBarChart(elementId: string, data: [string, number][]): Chart | null {
    const ctx = document.getElementById(elementId) as HTMLCanvasElement;
    if (!ctx || !data || data.length === 0) return null;
    const sortedData = [...data].sort((a, b) => b[1] - a[1]).slice(0, 5);
    const labels = sortedData.map((item) => item[0]);
    const values = sortedData.map((item) => item[1]);

    return new Chart(ctx, {
      type: 'bar',
      data: {
        labels: labels,
        datasets: [{
          label: this.translate.instant('DASHBOARD.CHART_HOURS_REGISTERED_LABEL'),
          data: values,
          backgroundColor: '#42A5F5', borderRadius: 4,
        }],
      },
      options: { indexAxis: 'y', responsive: true, maintainAspectRatio: false, plugins: { legend: { display: false } } },
    });
  }
}