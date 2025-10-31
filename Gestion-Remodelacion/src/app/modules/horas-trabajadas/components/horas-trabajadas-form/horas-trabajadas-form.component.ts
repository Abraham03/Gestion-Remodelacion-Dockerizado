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
import { HorasTrabajadas } from '../../models/horas-trabajadas';
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
    @Inject(MAT_DIALOG_DATA) public data: HorasTrabajadas
  ) {
    // 1. Definición del formulario: Se mantiene igual, es correcto.
    this.form = this.fb.group({
        id: [null],
        idEmpleado: [null, Validators.required],
        idProyecto: [null, Validators.required],
        fecha: [null, Validators.required],
        inputCantidad: [8, [Validators.required, Validators.min(0.01)]],
        actividadRealizada: ['', Validators.maxLength(500)],
    });
  }

  ngOnInit(): void {
    this.loadDropdownData();
    this.setupDynamicTranslations();

    // 2. Lógica para escuchar cambios en el empleado seleccionado.
    //    Esto es clave para cambiar la etiqueta de "Horas" a "Días" dinámicamente.
    this.form.get('idEmpleado')?.valueChanges.subscribe((empleadoId) => {
      if (!empleadoId) return;
      const empleadoSeleccionado = this.empleados.find(e => e.id === empleadoId);
      this.updateInputLabel(empleadoSeleccionado?.modeloDePago);
    });

    // 3. Lógica para rellenar el formulario en modo edición.
    if (this.data) {
        const patchedData = { 
          ...this.data, 
          // Mapeamos el valor 'horas' de los datos al control 'inputCantidad' del formulario.
          inputCantidad: this.data.horas, 
          idEmpleado: this.data.idEmpleado,
          idProyecto: this.data.idProyecto
        };
        if (patchedData.fecha) {
            patchedData.fecha = this.createNormalizedLocalDate(patchedData.fecha);
        }
        this.form.patchValue(patchedData);
    }
  }

  // 4. Se agregó un método para normalizar fechas y evitar problemas de zona horaria.
  private createNormalizedLocalDate(dateInput: string | Date): Date | null {
    if (!dateInput) return null;
    const dateStr = typeof dateInput === 'string' ? dateInput : dateInput.toISOString().split('T')[0];
    const [year, month, day] = dateStr.split('-').map(Number);
    return new Date(year, month - 1, day);
  }
  
  // 5. Se centralizó la lógica de traducción para las etiquetas dinámicas.
  //    Esto asegura que la etiqueta cambie de idioma si el usuario cambia el idioma
  //    mientras el diálogo está abierto.
  private setupDynamicTranslations(): void {
    this.updateInputLabel(); // Establece la etiqueta inicial al cargar.
    this.translate.onLangChange.subscribe(() => {
        const empleadoSeleccionado = this.empleados.find(e => e.id === this.form.get('idEmpleado')?.value);
        this.updateInputLabel(empleadoSeleccionado?.modeloDePago);
    });
  }
  
  // 6. Este método ahora usa 'translate.instant' para obtener la traducción correcta
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
        // Esto ahora sí funcionará
        this.empleados = data;
        
        // Si estás en modo edición, necesitas re-validar el label
        // después de que los empleados hayan cargado.
        if (this.data) {
          const empleadoSeleccionado = this.empleados.find(e => e.id === this.data.idEmpleado);
          this.updateInputLabel(empleadoSeleccionado?.modeloDePago);
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
    let horasParaEnviar = formValue.inputCantidad;

    if (this.unidadDeEntrada === 'dias') {
      horasParaEnviar = formValue.inputCantidad * 8; // Convertimos días a horas.
    }

    const horasTrabajadasData: any = {
      ...formValue,
      fecha: this.formatDateForBackend(formValue.fecha),
      horas: horasParaEnviar,
    };


    // 7. La llamada al servicio se unifica. Ahora, para actualizar, pasamos
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