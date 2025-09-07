import { CommonModule } from '@angular/common';
import { Component, Inject } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { provideNativeDateAdapter } from '@angular/material/core';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { Observable } from 'rxjs';
import { HorasTrabajadas } from '../../models/horas-trabajadas';
import { HorasTrabajadasService } from '../../services/horas-trabajadas.service';
import { EmpleadoService } from '../../../empleados/services/empleado.service';
import { ProyectosService } from '../../../proyectos/services/proyecto.service';
import { DropdownItem } from '../../../../core/models/dropdown-item.model';
import { NotificationService } from '../../../../core/services/notification.service';
import { MatSnackBar } from '@angular/material/snack-bar';
import { HttpErrorResponse } from '@angular/common/http';

@Component({
  selector: 'app-horas-trabajadas-form',
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
  templateUrl: './horas-trabajadas-form.component.html',
  styleUrl: './horas-trabajadas-form.component.scss',
  providers: [provideNativeDateAdapter()]

})
export class HorasTrabajadasFormComponent {
 form!: FormGroup;
  empleados$!: Observable<DropdownItem[]>;
  proyectos$!: Observable<DropdownItem[]>; // Asumo que necesitas una lista de proyectos para seleccionar

  constructor(
    private fb: FormBuilder,
    private horasTrabajadasService: HorasTrabajadasService,
    private empleadoService: EmpleadoService,
    private proyectosService: ProyectosService,   
    private snackBar: MatSnackBar,
    public dialogRef: MatDialogRef<HorasTrabajadasFormComponent>,
    private notificationService: NotificationService,
    @Inject(MAT_DIALOG_DATA) public data: HorasTrabajadas | null // 'data' contendrá el objeto a editar
  ) {}

  ngOnInit(): void {
    this.initForm();
    this.loadDropdownData(); // Cargar empleados y proyectos

    if (this.data) {
      const patchedData: any = { ...this.data };

      // Manejo de fechas para el Datepicker
      if (patchedData.fecha) {
        patchedData.fecha = this.createNormalizedLocalDate(patchedData.fecha);
      }
      if (patchedData.fechaRegistro) {
        // La fecha de registro no se edita, pero si la tuvieras que mostrar...
        // patchedData.fechaRegistro = this.createNormalizedLocalDate(patchedData.fechaRegistro);
      }

      this.form.patchValue(patchedData);
    }
  }

  // Métodos de manejo de fechas (copiados de tu `proyectos-form.component.ts`)
  private createNormalizedLocalDate(dateInput: string | Date): Date | null {
    if (!dateInput) return null;

    let dateObj: Date;
    if (typeof dateInput === 'string') {
      dateObj = new Date(dateInput);
    } else {
      dateObj = dateInput;
    }

    const year = dateObj.getUTCFullYear();
    const month = dateObj.getUTCMonth();
    const day = dateObj.getUTCDate();

    const localDate = new Date(year, month, day);
    localDate.setHours(0, 0, 0, 0);

    return localDate;
  }

  initForm(): void {
    this.form = this.fb.group({
      id: [this.data?.id || null],
      idEmpleado: [this.data?.idEmpleado || null, Validators.required],
      idProyecto: [this.data?.idProyecto || null, Validators.required],
      fecha: [null, Validators.required], // La fecha se inicializa como null y se parchea si hay data
      horas: [this.data?.horas || 0, [Validators.required, Validators.min(0.01), Validators.max(10000)]],
      actividadRealizada: [this.data?.actividadRealizada || '', Validators.maxLength(500)],
      // fechaRegistro no se incluye en el formulario ya que es gestionado por el backend
    });
  }

  loadDropdownData(): void {
    this.empleados$ = this.empleadoService.getEmpleadosForDropdown();
    this.proyectos$ = (this.proyectosService as any).getProyectosForDropdown(); // Se asume que ProyectosService tiene este método
  }

  // Método para formatear fechas para el backend (copiado de tu `proyectos-form.component.ts`)
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
      this.snackBar.open('Por favor, completa todos los campos.', 'Cerrar', { duration: 3000 });
      console.error('Formulario inválido:', this.form.errors);
      return;
    }

    const formValue = this.form.value;

    const horasTrabajadasData: HorasTrabajadas = {
      ...formValue,
      fecha: this.formatDateForBackend(formValue.fecha),
      // nombreEmpleado y nombreProyecto no se envían al backend, se gestionan por ID
      nombreEmpleado: null,
      nombreProyecto: null,
      fechaRegistro: null // Esto es gestionado por el backend
    };

    if (this.data) {
      // Actualizar
      this.horasTrabajadasService.updateHorasTrabajadas(horasTrabajadasData).subscribe({
        next: (response) => {
          this.snackBar.open('Horas trabajadas actualizadas correctamente.', 'Cerrar', { duration: 3000 });
          this.notificationService.notifyDataChange();
          this.dialogRef.close(true);
        },
        error: (error) => {
          if (error.status === 409) {
            this.snackBar.open(error.error?.message || 'Ocurrio un error inesperado.', 'Cerrar', { duration: 7000 });
          }else{
            this.snackBar.open('Ocurrio un error inesperado.', 'Cerrar', { duration: 7000 });
          }
        },
      });
    } else {
      // Crear
      this.horasTrabajadasService.addHorasTrabajadas(horasTrabajadasData).subscribe({
        next: (response) => {
          this.snackBar.open('Horas trabajadas creadas correctamente.', 'Cerrar', { duration: 3000 });
          this.notificationService.notifyDataChange();
          this.dialogRef.close(true);
        },
        error: (error: HttpErrorResponse) => {
          if (error.status === 409) {
            this.snackBar.open(error.error?.message || 'Ocurrio un error inesperado.', 'Cerrar', { duration: 7000 });
          }else{
            this.snackBar.open('Ocurrio un error inesperado.', 'Cerrar', { duration: 7000 });
          }
        },
      });
    }
  }

  onCancel(): void {
    this.dialogRef.close(false); // Cerrar sin éxito
  } 

}
