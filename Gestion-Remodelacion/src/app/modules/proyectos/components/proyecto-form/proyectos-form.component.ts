// src/app/modules/proyectos/components/proyectos-form/proyectos-form.component.ts

import { Component, OnInit, Inject } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef, MatDialogModule } from '@angular/material/dialog';
import { CommonModule } from '@angular/common';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { provideNativeDateAdapter } from '@angular/material/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { ProyectosService } from '../../services/proyecto.service';
import { ClienteService } from '../../../cliente/services/cliente.service';
import { EmpleadoService } from '../../../empleados/services/empleado.service';
import { Proyecto } from '../../models/proyecto.model';
import { Observable } from 'rxjs';
import { HttpErrorResponse } from '@angular/common/http';
import { MatSnackBar } from '@angular/material/snack-bar';

// Define the DropdownItem interface here or in a common types file
interface DropdownItem {
  id: number;
  nombre: string;
}

@Component({
  selector: 'app-proyectos-form',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatDatepickerModule,
    MatButtonModule,
    MatIconModule,
    MatDialogModule,
  ],
  templateUrl: './proyectos-form.component.html',
  styleUrls: ['./proyectos-form.component.scss'],
  providers: [provideNativeDateAdapter()],
})
export class ProyectosFormComponent implements OnInit {
  form!: FormGroup;
  estadosProyecto: string[] = ['Pendiente', 'En Progreso', 'En Pausa', 'Finalizado', 'Cancelado'];
  clientes$!: Observable<DropdownItem[]>;
  empleados$!: Observable<DropdownItem[]>;

  constructor(
    private fb: FormBuilder,
    private proyectosService: ProyectosService,
    private clientesService: ClienteService,
    private empleadoService: EmpleadoService,
    public dialogRef: MatDialogRef<ProyectosFormComponent>,
    private snackBar: MatSnackBar,
    @Inject(MAT_DIALOG_DATA) public data: Proyecto | null
  ) {}

  ngOnInit(): void {
    this.initForm();
    this.loadDropdownData();
    if (this.data) {
      const patchedData: any = { ...this.data };

      // === MANEJO DE FECHAS ===
      // Las fechas vienen como strings ISO 8601 del backend (ej. "2025-04-30T00:00:00.000+00:00")
      // Necesitamos parsearlas y normalizarlas para el datepicker.
      if (patchedData.fechaInicio) {
        patchedData.fechaInicio = this.createNormalizedLocalDate(patchedData.fechaInicio);
      }
      if (patchedData.fechaFinEstimada) {
        patchedData.fechaFinEstimada = this.createNormalizedLocalDate(patchedData.fechaFinEstimada);
      }
      if (patchedData.fechaFinalizacionReal) {
        patchedData.fechaFinalizacionReal = this.createNormalizedLocalDate(patchedData.fechaFinalizacionReal);
      }
      if (patchedData.fechaUltimoPagoRecibido) {
        patchedData.fechaUltimoPagoRecibido = this.createNormalizedLocalDate(patchedData.fechaUltimoPagoRecibido);
      }
      // =======================

      // Formateamos el estado
      if (patchedData.estado) {
        patchedData.estado = this.formatEstadoForFrontend(patchedData.estado);
      }

      this.form.patchValue(patchedData);
    }
  }

  /**
   * Normaliza una cadena de fecha ISO 8601 (del backend, con zona horaria)
   * a un objeto Date que represente la misma fecha calendario a medianoche local.
   * Esto es CRUCIAL para que el MatDatepicker muestre el día correcto.
   * @param dateInput La cadena de fecha en formato ISO 8601 (ej. '2025-04-30T00:00:00.000Z') o un objeto Date.
   * @returns Un objeto Date normalizado para el MatDatepicker o null si la entrada es nula.
   */
  private createNormalizedLocalDate(dateInput: string | Date): Date | null {
    if (!dateInput) return null;

    let dateObj: Date;
    if (typeof dateInput === 'string') {
      // Si es un string ISO 8601, JavaScript lo parseará.
      // Ej: new Date("2025-04-30T00:00:00.000Z") en GMT-5 resultará en un Date para "Apr 29 2025 19:00:00 GMT-5".
      dateObj = new Date(dateInput);
    } else {
      // Si ya es un objeto Date (por ejemplo, si el parser JSON de Angular ya lo hizo)
      dateObj = dateInput;
    }


    // IMPORTANT: Usar los métodos getUTC* para extraer los componentes de la fecha
    // del objeto Date que se creó a partir del string UTC.
    // Esto asegura que tomamos el día '30' de abril, y no el '29'.
    const year = dateObj.getUTCFullYear();
    const month = dateObj.getUTCMonth(); // getUTCMonth() ya es 0-indexado
    const day = dateObj.getUTCDate();

    // Crear una nueva fecha en la zona horaria local usando los componentes UTC extraídos.
    // Al no especificar la hora, se inicializa a medianoche local (00:00:00).
    const localDate = new Date(year, month, day);

    // Asegurarse de que no haya ninguna hora que pueda afectar la visualización del datepicker.
    localDate.setHours(0, 0, 0, 0);

    return localDate;
  }

  /**
   * Convierte el estado de backend a un formato legible en el frontend.
   */
  private formatEstadoForFrontend(backendEstado: string): string {
    switch (backendEstado) {
      case 'PENDIENTE':
        return 'Pendiente';
      case 'EN_PROGRESO':
        return 'En Progreso';
      case 'EN_PAUSA':
        return 'En Pausa';
      case 'FINALIZADO':
        return 'Finalizado';
      case 'CANCELADO':
        return 'Cancelado';
      default:
        return backendEstado;
    }
  }

  /**
   * Convierte el estado de frontend a un formato compatible con el backend.
   */
  private formatEstadoForBackend(frontendEstado: string): string {
    switch (frontendEstado) {
      case 'Pendiente':
        return 'PENDIENTE';
      case 'En Progreso':
        return 'EN_PROGRESO';
      case 'En Pausa':
        return 'EN_PAUSA';
      case 'Finalizado':
        return 'FINALIZADO';
      case 'Cancelado':
        return 'CANCELADO';
      default:
        return frontendEstado;
    }
  }

  initForm(): void {
    this.form = this.fb.group({
      id: [this.data?.id || null],
      nombreProyecto: [this.data?.nombreProyecto || '', Validators.required],
      descripcion: [this.data?.descripcion || ''],
      direccionPropiedad: [this.data?.direccionPropiedad || '', Validators.required],
      estado: [this.data?.estado ? this.formatEstadoForFrontend(this.data.estado) : 'Pendiente', Validators.required],
      fechaInicio: [null, Validators.required],
      fechaFinEstimada: [null, Validators.required],
      fechaFinalizacionReal: [null],
      fechaUltimoPagoRecibido: [null],
      montoContrato: [this.data?.montoContrato || 0, [Validators.required, Validators.min(0)]],
      montoRecibido: [this.data?.montoRecibido || 0, Validators.min(0)],
      costoMaterialesConsolidado: [this.data?.costoMaterialesConsolidado || 0, Validators.min(0)],
      otrosGastosDirectosConsolidado: [this.data?.otrosGastosDirectosConsolidado || 0, Validators.min(0)],
      costoManoDeObra: [this.data?.costoManoDeObra || 0, Validators.min(0)],
      progresoPorcentaje: [this.data?.progresoPorcentaje || 0, [Validators.min(0), Validators.max(100)]],
      notasProyecto: [this.data?.notasProyecto || ''],
      idCliente: [this.data?.idCliente || null, Validators.required],
      idEmpleadoResponsable: [this.data?.idEmpleadoResponsable || null],
    });
  }

  loadDropdownData(): void {
    this.clientes$ = this.clientesService.getClientesForDropdown();
    this.empleados$ = this.empleadoService.getEmpleadosForDropdown();
  }

  /**
   * Formatea un objeto Date (que ya está a medianoche local) a una cadena YYYY-MM-DD
   * para enviar al backend.
   * @param date El objeto Date a formatear.
   * @returns La fecha como cadena YYYY-MM-DD o null.
   */
  private formatDateForBackend(date: Date | null): string | null {
    if (!date) return null;
    const year = date.getFullYear();
    const month = (date.getMonth() + 1).toString().padStart(2, '0');
    const day = date.getDate().toString().padStart(2, '0');
    return `${year}-${month}-${day}`;
  }

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const formValue = this.form.value;

    const projectData: Proyecto = {
      ...formValue,
      estado: this.formatEstadoForBackend(formValue.estado) as any,
      fechaInicio: this.formatDateForBackend(formValue.fechaInicio),
      fechaFinEstimada: this.formatDateForBackend(formValue.fechaFinEstimada),
      fechaFinalizacionReal: this.formatDateForBackend(formValue.fechaFinalizacionReal),
      fechaUltimoPagoRecibido: this.formatDateForBackend(formValue.fechaUltimoPagoRecibido),
    };

    // Ajuste para cliente y empleado: Si el formValue tiene idCliente/idEmpleadoResponsable,
    // asegúrate de que el objeto Proyecto tenga los objetos Cliente/Empleado anidados
    // o que el servicio de Angular solo espere los IDs.
    // En tu proyecto.model.ts, tienes idCliente y idEmpleadoResponsable como numbers.
    // Esto es correcto para enviar. El backend se encargará de buscar el objeto completo.
    // No necesitas crear objetos Cliente o Empleado aquí en el frontend.

    if (this.data) {
      this.proyectosService.updateProyecto(projectData).subscribe({
        next: (response) => {
          this.dialogRef.close(true);
        },
        error: (error: HttpErrorResponse) => {
          if (error.status === 404) {
            this.snackBar.open('Error al actualizar: El proyecto no existe.', 'Cerrar', { duration: 4000 });
          }
        },
      });
    } else {
      this.proyectosService.addProyecto(projectData).subscribe({
        next: (response) => {
          this.dialogRef.close(true);
        },
        error: (error: HttpErrorResponse) => {
          this.snackBar.open('Error al crear el proyecto. Inténtalo de nuevo.', 'Cerrar', { duration: 3000 });
        },
      });
    }
  }

  onCancel(): void {
    this.dialogRef.close(false);
  }
}