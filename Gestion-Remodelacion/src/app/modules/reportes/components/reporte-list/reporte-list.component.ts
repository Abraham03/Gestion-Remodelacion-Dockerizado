import { Component, OnInit } from '@angular/core';
import { ReportesService } from '../../services/reporte.service';
import { Chart, ChartModule } from 'angular-highcharts';
import { CommonModule } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatSelectModule } from '@angular/material/select';
import { MatTableModule } from '@angular/material/table';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-reportes-list',
  standalone: true,
  imports: [
    CommonModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatSelectModule,
    MatTableModule,
    ChartModule,
    FormsModule,
  ],
  templateUrl: './reporte-list.component.html',
  styleUrls: ['./reporte-list.component.scss'],
})
export class ReportesListComponent implements OnInit {
  filtros = {
    modulo: 'empleados', // Por defecto, se selecciona el módulo de empleados
    fechaInicio: new Date(),
    fechaFin: new Date(),
  };

  datosReporte: any[] = [];
  displayedColumns: string[] = [];
  chart: Chart | undefined;

  constructor(private reportesService: ReportesService) {}

  ngOnInit(): void {

  }

}