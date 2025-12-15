export interface DashboardSummary {
  totalProyectos: number;
  empleadosActivos: number;
  balanceFinanciero: number;
  montoRecibido: number;
  costoMateriales: number;
  otrosGastos: number;
  costoManoDeObra: number;
  // Arrays para gráficos
  empleadosPorRol: [string, number][];      // [Rol, Cantidad]
  horasPorProyecto: [string, number][];     // [Proyecto, Horas]
  proyectosPorEstado: [string, number][];   // [Estado, Cantidad]
  horasPorEmpleadoProyecto: any[];          // Datos de la tabla
}

export interface DashboardClientes {
  clientesPorMes: [number, number, number][]; // [Año, Mes, Cantidad]
}

// Estado de Akita
export interface DashboardState {
  summary: DashboardSummary | null;
  clientesSummary: DashboardClientes | null;
  availableYears: number[];
  isLoading: boolean;
}