import { CommonModule } from '@angular/common';
import { Component, Inject, inject, OnInit } from '@angular/core';
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
import { MatSnackBar } from '@angular/material/snack-bar';
import { HttpErrorResponse } from '@angular/common/http';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { NotificationService } from '../../../../core/services/notification.service';

import { HorasTrabajadas, HorasTrabajadasRequest } from '../../models/horas-trabajadas';
import { HorasTrabajadasService } from '../../services/horas-trabajadas.service';
import { EmpleadoService } from '../../../empleados/services/empleado.service';
import { ProyectosService } from '../../../proyectos/services/proyecto.service';
import { dropdownItemModeloHorastrabajadas } from '../../../../core/models/dropdown-item-modelo-horastrabajadas';
import { ProyectoDropdown } from '../../../proyectos/models/proyecto.model';

@Component({
  selector: 'app-horas-trabajadas-form',
  standalone: true,
  imports: [
    CommonModule, ReactiveFormsModule, MatFormFieldModule, MatInputModule,
    MatSelectModule, MatDatepickerModule, MatButtonModule, MatIconModule,
    MatDialogModule, TranslateModule,
  ],
  templateUrl: './horas-trabajadas-form.component.html',
  styleUrl: './horas-trabajadas-form.component.scss',
  providers: [provideNativeDateAdapter()]
})
export class HorasTrabajadasFormComponent implements OnInit {
  form: FormGroup;
  empleados: dropdownItemModeloHorastrabajadas[] = [];
  proyectos$!: Observable<ProyectoDropdown[]>;
  unidadDeEntrada: 'horas' | 'dias' = 'horas';
  etiquetaDeEntrada: string = '';

  private translate = inject(TranslateService);
  private notificationService = inject(NotificationService);
  private horasTrabajadasService = inject(HorasTrabajadasService);
  private proyectosService = inject(ProyectosService);
  private empleadoService = inject(EmpleadoService);

  constructor(
    private fb: FormBuilder,
    private snackBar: MatSnackBar,
    public dialogRef: MatDialogRef<HorasTrabajadasFormComponent>,
    @Inject(MAT_DIALOG_DATA) public data: HorasTrabajadas // Recibe el modelo de horas trabajadas en modo edición
  ) {
    // 1. Definición del formulario: Se mantiene igual, es correcto.
    this.form = this.fb.group({
        id: [null],
        idEmpleado: [null, Validators.required],
        idProyecto: [null, Validators.required],
        fecha: [null, Validators.required],
        inputCantidad: [null, [Validators.required, Validators.min(0.01)]],
        actividadRealizada: ['', Validators.maxLength(500)],
    });
  }

  ngOnInit(): void {
    this.loadDropdownData();
    this.setupDynamicTranslations();

    //    Lógica para escuchar cambios en el empleado seleccionado.
    //    Esto es clave para cambiar la etiqueta de "Horas" a "Días" dinámicamente.
    this.form.get('idEmpleado')?.valueChanges.subscribe((empleadoId) => {
      // No actualiza el valor si ya estamos en modo edición
      if (!empleadoId) return;

      const empleadoSeleccionado = this.empleados.find(e => e.id === empleadoId);
      this.updateInputLabel(empleadoSeleccionado?.modeloDePago);

    });

    // Lógica para rellenar el formulario en modo edición.
    if (this.data) {
        const patchedData = { 
          ...this.data, 
          // Mapeamos el valor 'horas' de los datos al control 'inputCantidad' del formulario.
          inputCantidad: this.data.cantidad, 
          idEmpleado: this.data.idEmpleado,
          idProyecto: this.data.idProyecto
        };
        if (patchedData.fecha) {
            patchedData.fecha = this.createNormalizedLocalDate(patchedData.fecha);
        }
        this.form.patchValue(patchedData);
        // El valor de 'inputCantidad' se establecera en el loadDropdownData()
    } else {
        // Si no es un Nuevo registro, establece el valor por defecto
        this.updateInputLabel('POR_HORA'); // Por defecto es 'POR_HORA'
    }
  }

  // Se agregó un método para normalizar fechas y evitar problemas de zona horaria.
  private createNormalizedLocalDate(dateInput: string | Date): Date | null {
    if (!dateInput) return null;
    const dateStr = typeof dateInput === 'string' ? dateInput : dateInput.toISOString().split('T')[0];
    const [year, month, day] = dateStr.split('-').map(Number);
    return new Date(year, month - 1, day);
  }
  
  //    Se centralizó la lógica de traducción para las etiquetas dinámicas.
  //    Esto asegura que la etiqueta cambie de idioma si el usuario cambia el idioma
  //    mientras el diálogo está abierto.
  private setupDynamicTranslations(): void {
    // Si estamos editando, usa la 'unidad' del snapshot para establecer la etiqueta inicial
    if (this.data) {
      const modelo = this.data.unidad.toLowerCase().includes('dia') ? 'POR_DIA' : 'POR_HORA';
      this.updateInputLabel(modelo);
    } else{
      this.updateInputLabel('POR_HORA'); // Por defecto es 'POR_HORA'
    }

    this.translate.onLangChange.subscribe(() => {
        const empleadoSeleccionado = this.empleados.find(e => e.id === this.form.get('idEmpleado')?.value);
        const modelo = this.data ? (this.data.unidad.toLowerCase().includes('dia') ? 'POR_DIA' : 'POR_HORA') : empleadoSeleccionado?.modeloDePago;
        this.updateInputLabel(modelo);
    });
  }
  
  //    Este método ahora usa 'translate.instant' para obtener la traducción correcta
  //    de 'Días Trabajados' u 'Horas Trabajadas'.
  private updateInputLabel(modeloDePago?: string): void {
      if (modeloDePago === 'POR_DIA') {
          this.unidadDeEntrada = 'dias';
          this.etiquetaDeEntrada = this.translate.instant('WORK_HOURS.WORKED_DAYS');
          if (!this.data) this.form.get('inputCantidad')?.setValue(1); // Valor por defecto para días
      } else {
          this.unidadDeEntrada = 'horas';
          this.etiquetaDeEntrada = this.translate.instant('WORK_HOURS.WORKED_HOURS');
          if (!this.data) this.form.get('inputCantidad')?.setValue(8); // Valor por defecto para horas
      }
  }

loadDropdownData(): void {
    this.empleadoService.getEmpleadosForDropdown().subscribe({
      next: data => {
        this.empleados = data;
        
        // Se asegura si los empleados cargan después, la etiqueta se re-evalúe.
        if (this.data) {
          const empleadoSeleccionado = this.empleados.find(e => e.id === this.data.idEmpleado);
          // Usa el 'modeloDePago' actual del empleado para la ETIQUETA
          const modeloDePago = empleadoSeleccionado?.modeloDePago;
          // El valor 'inputCantidad' ya se estableció en ngOnInit
        }
      },
      error: err => {
        // Si hubieras tenido esto, habrías visto el error en la consola.
        console.error('Error al cargar empleados dropdown:', err);
        this.snackBar.open('Error al cargar empleados', 'Cerrar', { duration: 3000 });
      }
    });

    this.proyectos$ = (this.proyectosService as any).getProyectosForDropdown();
  }

  private formatDateForBackend(date: Date | null): string | null {
    if (!date) return null;
    // Lógica mejorada para manejar correctamente la zona horaria del cliente.
    const offset = date.getTimezoneOffset();
    const adjustedDate = new Date(date.getTime() - (offset * 60 * 1000));
    return adjustedDate.toISOString().split('T')[0];
  }

  onSubmit(): void {
    // Validamos el formulario antes de enviarlo 
    if (this.form.invalid) {
      this.snackBar.open(this.translate.instant('WORK_HOURS.VALIDATION_ERROR'), this.translate.instant('GLOBAL.CLOSE'), { duration: 3000 });
      return;
    }

    const formValue = this.form.value;

    const horasTrabajadasData: HorasTrabajadasRequest = {
      id: this.data ? this.data.id : undefined, // Añade ID si estamos editando
      idEmpleado: formValue.idEmpleado,
      idProyecto: formValue.idProyecto,
      fecha: this.formatDateForBackend(formValue.fecha),
      actividadRealizada: formValue.actividadRealizada,
      cantidad: formValue.inputCantidad,
      unidad: this.unidadDeEntrada // 'dias' o 'horas'
    };


    //    La llamada al servicio se unifica. Ahora, para actualizar, pasamos
    //    el objeto completo `horasTrabajadasData`, que ya contiene el ID,
    //    cumpliendo con lo que espera el método de tu servicio (1 solo argumento).
    const serviceCall = this.data
      ? this.horasTrabajadasService.updateHorasTrabajadas(horasTrabajadasData)
      : this.horasTrabajadasService.addHorasTrabajadas(horasTrabajadasData);
    
    const successKey = this.data ? 'WORK_HOURS.SUCCESSFULLY_UPDATED' : 'WORK_HOURS.SUCCESSFULLY_CREATED';

    serviceCall.subscribe({
      next: () => {
        this.snackBar.open(this.translate.instant(successKey), this.translate.instant('GLOBAL.CLOSE'), { duration: 3000 });
        this.notificationService.notifyDataChange();
        this.dialogRef.close(true);
      },
      error: (err: HttpErrorResponse) => {
        const errorKey = err.error?.message || 'error.unexpected';
        const translatedMessage = this.translate.instant(errorKey);
        this.snackBar.open(translatedMessage, this.translate.instant('GLOBAL.CLOSE'), { duration: 7000 });
      },
    });
  }

  onCancel(): void {
    this.dialogRef.close(false);
  }
}