import { Injectable } from '@angular/core';
import { of } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class ReportesService {
  // Datos dummy para empleados
  private empleadosDummy = [
    { id: 1, nombre: 'Juan Pérez', puesto: 'Desarrollador', salario: 3000, fechaContratacion: '2023-01-15' },
    { id: 2, nombre: 'María López', puesto: 'Diseñadora', salario: 2500, fechaContratacion: '2023-02-10' },
    { id: 3, nombre: 'Carlos Gómez', puesto: 'Gerente', salario: 4000, fechaContratacion: '2023-03-05' },
  ];

  // Datos dummy para proyectos
  private proyectosDummy = [
    { id: 1, nombre: 'Proyecto A', estado: 'En progreso', fechaInicio: '2023-01-01', fechaFin: '2023-12-31', costo: 15000 },
    { id: 2, nombre: 'Proyecto B', estado: 'Completado', fechaInicio: '2023-02-01', fechaFin: '2023-11-30', costo: 10000 },
    { id: 3, nombre: 'Proyecto C', estado: 'Cancelado', fechaInicio: '2023-03-01', fechaFin: '2023-10-31', costo: 5000 },
  ];

  // Datos dummy para materiales
  private materialesDummy = [
    { id: 1, nombre: 'Cemento', cantidad: 100, unidadMedida: 'kg', fechaAdquisicion: '2023-01-01' },
    { id: 2, nombre: 'Pintura', cantidad: 50, unidadMedida: 'litros', fechaAdquisicion: '2023-02-01' },
    { id: 3, nombre: 'Madera', cantidad: 200, unidadMedida: 'unidades', fechaAdquisicion: '2023-03-01' },
  ];

  // Obtener reporte de empleados (dummy)
  getReporteEmpleados(filtros: any) {
    return of(this.empleadosDummy);
  }

  // Obtener reporte de proyectos (dummy)
  getReporteProyectos(filtros: any) {
    return of(this.proyectosDummy);
  }

  // Obtener reporte de materiales (dummy)
  getReporteMateriales(filtros: any) {
    return of(this.materialesDummy);
  }
}