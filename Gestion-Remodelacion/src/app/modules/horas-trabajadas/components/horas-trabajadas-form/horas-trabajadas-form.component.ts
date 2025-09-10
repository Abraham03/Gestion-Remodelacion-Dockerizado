// Importaciones necesarias de Angular y otros módulos.
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

// Importaciones de tus modelos y servicios personalizados.
import { HorasTrabajadas } from '../../models/horas-trabajadas';
import { HorasTrabajadasService } from '../../services/horas-trabajadas.service';
import { EmpleadoService } from '../../../empleados/services/empleado.service';
import { ProyectosService } from '../../../proyectos/services/proyecto.service';
import { DropdownItem } from '../../../../core/models/dropdown-item.model';
import { NotificationService } from '../../../../core/services/notification.service';
import { MatSnackBar } from '@angular/material/snack-bar';
import { HttpErrorResponse } from '@angular/common/http';
import { dropdownItemModeloHorastrabajadas } from '../../../../core/models/dropdown-item-modelo-horastrabajadas';

// Decorador del componente. Define metadatos como el selector, si es standalone, y qué módulos necesita.
@Component({
  selector: 'app-horas-trabajadas-form',
  standalone: true, // Indica que este componente gestiona sus propias dependencias.
  imports: [ // Lista de módulos y otros componentes necesarios para la plantilla HTML.
    CommonModule, // Para directivas como *ngFor y *ngIf.
    ReactiveFormsModule, // Esencial para trabajar con formularios reactivos (FormGroup).
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
  providers: [provideNativeDateAdapter()] // Proveedor para el datepicker de Angular Material.
})
export class HorasTrabajadasFormComponent {
  // --- PROPIEDADES DE LA CLASE ---

  // `form` contendrá la instancia de nuestro formulario reactivo.
  form!: FormGroup;
  
  // `empleados` es un arreglo simple para guardar los datos del dropdown.
  // Lo necesitamos como arreglo para poder usar `.find()` en la lógica de `ngOnInit`.
  empleados: dropdownItemModeloHorastrabajadas[] = [];
  
  // `proyectos$` es un Observable. La `$` al final es una convención para indicar que es un stream de datos.
  // Se usará con el pipe `async` en el HTML para la carga de datos.
  proyectos$!: Observable<DropdownItem[]>; 

  // `unidadDeEntrada` y `etiquetaDeEntrada` son propiedades para manejar el estado de la UI.
  // Cambiarán dinámicamente según el empleado seleccionado.
  unidadDeEntrada: 'horas' | 'dias' = 'horas';
  etiquetaDeEntrada: string = 'Horas';

  // El constructor es donde se realiza la Inyección de Dependencias.
  // Angular nos "inyecta" instancias de los servicios que necesitamos.
  constructor(
    private fb: FormBuilder, // Para construir el formulario reactivo.
    private horasTrabajadasService: HorasTrabajadasService, // Para crear/actualizar registros.
    private empleadoService: EmpleadoService, // Para obtener la lista de empleados.
    private proyectosService: ProyectosService, // Para obtener la lista de proyectos.
    private snackBar: MatSnackBar, // Para mostrar notificaciones.
    public dialogRef: MatDialogRef<HorasTrabajadasFormComponent>, // Para controlar el diálogo (modal).
    private notificationService: NotificationService, // Servicio personalizado para notificaciones entre componentes.
    @Inject(MAT_DIALOG_DATA) public data: HorasTrabajadas | null // Para recibir datos cuando el diálogo se abre en modo "editar".
  ) {}

  // `ngOnInit` es un "hook" del ciclo de vida de Angular. Se ejecuta una vez que el componente se ha inicializado.
  ngOnInit(): void {
    // 1. Definimos la estructura del formulario.
    this.initForm();
    // 2. Pedimos los datos para llenar los dropdowns.
    this.loadDropdownData();

    // 3. Escuchamos cualquier cambio en el valor del dropdown de empleados.
    this.form.get('idEmpleado')?.valueChanges.subscribe((empleadoId) => {
      // Si el valor es nulo (ej. al limpiar el formulario), no hacemos nada.
      if (!empleadoId) return;

      // Buscamos en nuestro arreglo local el objeto completo del empleado seleccionado.
      const empleadoSeleccionado = this.empleados.find(
        (empleado) => empleado.id === empleadoId
      );

      // Basado en el `modeloDePago` del empleado, actualizamos nuestras variables de estado.
      if (empleadoSeleccionado?.modeloDePago === 'POR_DIA') {
        this.unidadDeEntrada = 'dias';
        this.etiquetaDeEntrada = 'Días Trabajados';
        this.form.get('inputCantidad')?.setValue(1); // Ponemos 1 día como valor por defecto.
      } else {
        this.unidadDeEntrada = 'horas';
        this.etiquetaDeEntrada = 'Horas';
        this.form.get('inputCantidad')?.setValue(8); // Ponemos 8 horas como valor por defecto.
      }
    });

    // 4. Si el diálogo recibió datos (modo edición), llenamos el formulario con esos datos.
    if (this.data) {
      const patchedData: any = { ...this.data };

      // Normalizamos la fecha para evitar problemas de zona horaria con el datepicker.
      if (patchedData.fecha) {
        patchedData.fecha = this.createNormalizedLocalDate(patchedData.fecha);
      }
      
      // `patchValue` llena el formulario con los datos que coincidan por nombre.
      this.form.patchValue(patchedData);
    }
  }

  // Método privado para asegurar que la fecha se muestre correctamente en el datepicker sin importar la zona horaria.
  private createNormalizedLocalDate(dateInput: string | Date): Date | null {
    if (!dateInput) return null;

    let dateObj: Date = typeof dateInput === 'string' ? new Date(dateInput) : dateInput;

    const localDate = new Date(dateObj.getUTCFullYear(), dateObj.getUTCMonth(), dateObj.getUTCDate());
    localDate.setHours(0, 0, 0, 0);

    return localDate;
  }

  // Aquí se define la estructura del formulario, sus campos y sus validaciones.
  initForm(): void {
    this.form = this.fb.group({
      id: [this.data?.id || null],
      idEmpleado: [this.data?.idEmpleado || null, Validators.required],
      idProyecto: [this.data?.idProyecto || null, Validators.required],
      fecha: [null, Validators.required],
      // Este es el control para la entrada del usuario, sea en horas o días.
      inputCantidad: [this.data?.horas || 8, [Validators.required, Validators.min(0.01)]],
      actividadRealizada: [this.data?.actividadRealizada || '', Validators.maxLength(500)],
    });
  }

  // Método para obtener los datos de los servicios y llenar los dropdowns.
  loadDropdownData(): void {
    // Para empleados, nos suscribimos y guardamos el resultado en un arreglo simple.
    this.empleadoService.getEmpleadosForDropdown().subscribe(data => {
      this.empleados = data;
    });
    // Para proyectos, asignamos el Observable directamente para usarlo con el pipe `async` en el HTML.
    this.proyectos$ = (this.proyectosService as any).getProyectosForDropdown();
  }

  // Convierte el objeto `Date` del datepicker al formato de texto `YYYY-MM-DD` que espera el backend.
  private formatDateForBackend(date: Date | null): string | null {
    if (!date) return null;
    const year = date.getFullYear();
    const month = (date.getMonth() + 1).toString().padStart(2, '0');
    const day = date.getDate().toString().padStart(2, '0');
    return `${year}-${month}-${day}`;
  }

  // Se ejecuta cuando el usuario hace clic en el botón "Guardar".
  onSubmit(): void {
    // Primero, validamos que el formulario esté correcto.
    if (this.form.invalid) {
      this.form.markAllAsTouched(); // Marca todos los campos como "tocados" para mostrar los errores.
      this.snackBar.open('Por favor, completa todos los campos.', 'Cerrar', { duration: 3000 });
      return;
    }

    const formValue = this.form.value;
    let horasParaEnviar = formValue.inputCantidad;

    // Lógica de conversión: si el usuario ingresó días, los convertimos a horas.
    if (this.unidadDeEntrada === 'dias') {
      horasParaEnviar = formValue.inputCantidad * 8; // Asumimos una jornada de 8 horas.
    }

    // Construimos el objeto final que se enviará a la API.
    const horasTrabajadasData: HorasTrabajadas = {
      id: formValue.id,
      idEmpleado: formValue.idEmpleado,
      idProyecto: formValue.idProyecto,
      actividadRealizada: formValue.actividadRealizada,
      fecha: this.formatDateForBackend(formValue.fecha),
      horas: horasParaEnviar, // Siempre enviamos el valor en `horas`.
      nombreEmpleado: null,
      nombreProyecto: null,
      fechaRegistro: null,
    };

    // Determinamos si debemos llamar al servicio de actualizar o de crear.
    if (this.data) {
      // Modo Actualizar
      this.horasTrabajadasService.updateHorasTrabajadas(horasTrabajadasData).subscribe({
        next: () => {
          this.snackBar.open('Horas trabajadas actualizadas correctamente.', 'Cerrar', { duration: 3000 });
          this.notificationService.notifyDataChange(); // Notifica a otros componentes que los datos cambiaron.
          this.dialogRef.close(true); // Cierra el diálogo y devuelve `true` para indicar éxito.
        },
        error: (error) => {
          this.snackBar.open(error.error?.message || 'Ocurrió un error inesperado.', 'Cerrar', { duration: 7000 });
        },
      });
    } else {
      // Modo Crear
      this.horasTrabajadasService.addHorasTrabajadas(horasTrabajadasData).subscribe({
        next: () => {
          this.snackBar.open('Horas trabajadas creadas correctamente.', 'Cerrar', { duration: 3000 });
          this.notificationService.notifyDataChange();
          this.dialogRef.close(true);
        },
        error: (error: HttpErrorResponse) => {
          this.snackBar.open(error.error?.message || 'Ocurrió un error inesperado.', 'Cerrar', { duration: 7000 });
        },
      });
    }
  }

  // Se ejecuta cuando el usuario hace clic en el botón "Cancelar".
  onCancel(): void {
    this.dialogRef.close(false); // Cierra el diálogo y devuelve `false`.
  }
}