// src/app/modules/dashboard/components/dashboard/dashboard.component.ts

import { Component, OnInit, AfterViewInit, ViewChild, OnDestroy, inject } from '@angular/core';
import { CommonModule, CurrencyPipe } from '@angular/common';
import { HttpClientModule } from '@angular/common/http';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { BreakpointObserver, Breakpoints } from '@angular/cdk/layout';
import { forkJoin, Subject } from 'rxjs';
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
import { DashboardService } from '../../services/dashboard.service';
import { DropdownItem } from '../../../../core/models/dropdown-item.model';
//Importa TranslateModule y TranslateService
import { TranslateModule, TranslateService } from '@ngx-translate/core';

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

  public proyectosData: any;
  public clientesData: any;
  filterValue: string = '';
  public isLoading = true;
  public displayedColumns: string[] = ['empleado', 'proyecto', 'horas', 'montoTotal'];
  public dataSource = new MatTableDataSource<any>([]);
  private charts: { [key: string]: Chart | null } = {};
  public cols = 2;
  private destroy$ = new Subject<void>();
  public availableProjects: DropdownItem[] = [];
  public proyectosSelectedId: number | null = null;
  public proyectosYears: number[] = [];
  public proyectosSelectedYear: number = new Date().getFullYear();
  public proyectosSelectedMonth: number | null = null;
  public clientesYears: number[] = [];
  public clientesSelectedYear: number = new Date().getFullYear();
  public clientesSelectedMonth: number | null = null;

  // 3. Declara 'availableMonths' sin inicializarlo
  public availableMonths: { value: number | null, viewValue: string }[] = [];

  // 4. Inyecta TranslateService
  private dashboardService = inject(DashboardService);
  private snackBar = inject(MatSnackBar);
  private breakpointObserver = inject(BreakpointObserver);
  private translate = inject(TranslateService);

  ngOnInit(): void {
    this.setupTranslations(); // Configura las traducciones primero
    this.setupResponsiveCols();
    this.loadInitialData();
  }

  // 5. Nuevo método para manejar traducciones y cambios de idioma
  private setupTranslations(): void {
    this.generateMonthList(); // Genera la lista de meses inicial

    // Suscríbete a los cambios de idioma para actualizar dinámicamente
    this.translate.onLangChange
      .pipe(takeUntil(this.destroy$))
      .subscribe(() => {
        this.generateMonthList(); // Regenera la lista de meses con el nuevo idioma
        this.setupVisuals(); // Vuelve a dibujar los gráficos con las nuevas traducciones
      });
  }

  // 6. Nuevo método para generar la lista de meses traducida
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
    this.breakpointObserver.observe([Breakpoints.XSmall, Breakpoints.Small, Breakpoints.Medium, Breakpoints.Large])
      .pipe(takeUntil(this.destroy$))
      .subscribe((result) => {
        this.cols = result.breakpoints[Breakpoints.XSmall] ? 1 : 2;
      });
  }

  loadInitialData(): void {
    this.isLoading = true;
    this.dashboardService.getAvailableYears().subscribe({
      next: (years) => {
        this.proyectosYears = years;
        this.clientesYears = years;
        if (!this.proyectosYears.includes(this.proyectosSelectedYear)) {
          this.proyectosSelectedYear = this.proyectosYears[0] || new Date().getFullYear();
        }
        if (!this.clientesYears.includes(this.clientesSelectedYear)) {
          this.clientesSelectedYear = this.clientesYears[0] || new Date().getFullYear();
        }

        this.loadProjectsForFilters();
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
          error: (err) => this.handleError(err, this.translate.instant('DASHBOARD.ERROR_LOADING_DATA'))
        });
      },
      error: (err) => this.handleError(err, this.translate.instant('DASHBOARD.ERROR_LOADING_YEARS'))
    });
  }

  loadProjectsForFilters(): void {
    this.dashboardService.getProjectsForFilter(this.proyectosSelectedYear, this.proyectosSelectedMonth).subscribe({
      next: (projects) => {
        this.availableProjects = projects;
        this.proyectosSelectedId = null;
      },
      error: (err) => this.handleError(err)
    });
  }

  loadProyectosData(): void {
    this.isLoading = true;
    this.dashboardService.getProyectosSummary(this.proyectosSelectedYear, this.proyectosSelectedMonth, this.proyectosSelectedId).subscribe({
      next: (data) => {
        this.proyectosData = data;
        this.setupVisuals();
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
        this.setupVisuals();
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
      nombreEmpleado: item[1], nombreProyecto: item[3], horas: item[4], montoTotal: item[5]
    }));
    this.dataSource.data = tableData;
    setTimeout(() => {
      this.dataSource.paginator = this.paginator;
      this.dataSource.sort = this.sort;
    });
  }

  public applyFilter(filterValue: String): void {
    this.filterValue = filterValue.trim().toLowerCase();
    this.dataSource.filter = this.filterValue;
    if (this.dataSource.paginator) {
      this.dataSource.paginator.firstPage();
    }
  }

  applyFilterIfEmpty(filterValue: string): void {
    if (filterValue === '') {
      this.applyFilter('');
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

  private handleError(error: any, customMessage?: string): void {
    console.error(error);
    this.isLoading = false;
    const message = customMessage || this.translate.instant('DASHBOARD.ERROR_LOADING_DATA');
    this.snackBar.open(message, 'Cerrar', { duration: 5000 });
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