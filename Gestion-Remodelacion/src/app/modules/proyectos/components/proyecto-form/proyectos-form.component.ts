import { Component, OnInit, Inject, OnDestroy, inject } from '@angular/core';
import {
  FormBuilder,
  FormGroup,
  Validators,
  ReactiveFormsModule,
} from '@angular/forms';
import {
  MAT_DIALOG_DATA,
  MatDialogRef,
  MatDialogModule,
} from '@angular/material/dialog';
import { CommonModule } from '@angular/common';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { provideNativeDateAdapter } from '@angular/material/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { Observable, Subject, takeUntil } from 'rxjs';
import { HttpErrorResponse } from '@angular/common/http';
import { MatSnackBar } from '@angular/material/snack-bar';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { NotificationService } from '../../../../core/services/notification.service';
import { ProyectosService } from '../../services/proyecto.service';
import { ClienteService } from '../../../cliente/services/cliente.service';
import { EmpleadoService } from '../../../empleados/services/empleado.service';
import { Proyecto } from '../../models/proyecto.model';
import { DropdownItem } from '../../../../core/models/dropdown-item.model';
import { NumberFormatDirective } from '../../../../shared/directives/number-format.directive';

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
    TranslateModule,
    NumberFormatDirective,
  ],
  templateUrl: './proyectos-form.component.html',
  styleUrls: ['./proyectos-form.component.scss'],
  providers: [provideNativeDateAdapter()],
})
export class ProyectosFormComponent implements OnInit, OnDestroy {
  private destroy$ = new Subject<void>();
  form!: FormGroup;
  estadosProyecto: { backendValue: string; viewValue: string }[] = [];
  clientes$!: Observable<DropdownItem[]>;
  empleados$!: Observable<DropdownItem[]>;

  //    Inyección de dependencias
  private translate = inject(TranslateService);
  private notificationService = inject(NotificationService);
  private proyectosService = inject(ProyectosService);
  private clientesService = inject(ClienteService);
  private empleadoService = inject(EmpleadoService);
  private snackBar = inject(MatSnackBar);

  //    Se unifica toda la inyección de dependencias en el constructor
  //    para evitar conflictos y asegurar que 'data' siempre esté inicializado.
  constructor(
    private fb: FormBuilder,
    public dialogRef: MatDialogRef<ProyectosFormComponent>,
    @Inject(MAT_DIALOG_DATA) public data: Proyecto | null
  ) { }

ngOnInit(): void {
    this.initForm();
    this.setupDynamicTranslations();
    this.loadDropdownData();

    // ✅ CAMBIO IMPORTANTE: Cargar datos completos si es edición
    if (this.data && this.data.id) {
      // 1. Cargamos datos básicos inmediatamente para que no se vea vacío
      this.patchFormWithData(this.data);

      // 2. Pedimos al backend el objeto COMPLETO (que incluirá el equipoAsignado)
      this.proyectosService.getProyectoById(this.data.id)
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: (proyectoCompleto) => {
            // 3. Volvemos a parchear el formulario con la info fresca y el equipo
            this.patchFormWithData(proyectoCompleto);
          },
          error: () => this.snackBar.open('Error cargando detalles del equipo', 'Cerrar')
        });
    }
  }

  private patchFormWithData(data: any): void {
    const patchedData: any = { ...data };
    
    // Normalizar fechas
    if (patchedData.fechaInicio) patchedData.fechaInicio = this.createNormalizedLocalDate(patchedData.fechaInicio);
    if (patchedData.fechaFinEstimada) patchedData.fechaFinEstimada = this.createNormalizedLocalDate(patchedData.fechaFinEstimada);
    if (patchedData.fechaFinalizacionReal) patchedData.fechaFinalizacionReal = this.createNormalizedLocalDate(patchedData.fechaFinalizacionReal);
    if (patchedData.fechaUltimoPagoRecibido) patchedData.fechaUltimoPagoRecibido = this.createNormalizedLocalDate(patchedData.fechaUltimoPagoRecibido);
    
    // ✅ Mapear el equipo asignado a IDs
    if (data.equipoAsignado && Array.isArray(data.equipoAsignado)) {
      patchedData.idsEmpleadosAsignados = data.equipoAsignado.map((e: any) => e.id);
    } 
    // Nota: Si no hay equipo, no sobreescribimos con [] todavía para evitar borrar lo que el usuario esté tocando mientras carga

    this.form.patchValue(patchedData);
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private setupDynamicTranslations(): void {
    this.generateProjectStates();
    this.translate.onLangChange.pipe(takeUntil(this.destroy$)).subscribe(() => {
      this.generateProjectStates();
    });
  }

  private generateProjectStates(): void {
    // Los valores del backend DEBEN COINCIDIR con tu Enum de Java.
    const backendStates = [
      'PENDIENTE',
      'EN_PROGRESO',
      'EN_PAUSA',
      'FINALIZADO',
      'CANCELADO',
    ];
    this.estadosProyecto = backendStates.map((state) => ({
      backendValue: state,
      viewValue: this.translate.instant(`PROJECTS.STATE.${state}`),
    }));
  }

  private createNormalizedLocalDate(dateInput: string | Date): Date | null {
    if (!dateInput) return null;
    const dateStr =
      typeof dateInput === 'string'
        ? dateInput
        : dateInput.toISOString().split('T')[0];
    const [year, month, day] = dateStr.split('-').map(Number);
    return new Date(year, month - 1, day);
  }

  initForm(): void {
    this.form = this.fb.group({
      id: [null],
      nombreProyecto: ['', Validators.required],
      descripcion: [''],
      direccionPropiedad: ['', Validators.required],
      estado: ['PENDIENTE', Validators.required],
      fechaInicio: [null, Validators.required],
      fechaFinEstimada: [null, Validators.required],
      fechaFinalizacionReal: [null],
      fechaUltimoPagoRecibido: [null],
      montoContrato: [0, [Validators.required, Validators.min(0)]],
      montoRecibido: [0, Validators.min(0)],
      costoMaterialesConsolidado: [0, Validators.min(0)],
      otrosGastosDirectosConsolidado: [0, Validators.min(0)],
      costoManoDeObra: [{ value: 0, disabled: true }],
      progresoPorcentaje: [0, [Validators.min(0), Validators.max(100)]],
      notasProyecto: [''],
      idCliente: [null, Validators.required],
      idEmpleadoResponsable: [null],
      idsEmpleadosAsignados: [[]],
    });

    if (this.data) {
      this.form.patchValue(this.data);
    }
  }

  loadDropdownData(): void {
    this.clientes$ = this.clientesService.getClientesForDropdown();
    this.empleados$ = this.empleadoService.getEmpleadosForDropdown();
  }

  private formatDateForBackend(date: Date | null): string | null {
    if (!date) return null;
    const offset = date.getTimezoneOffset();
    const adjustedDate = new Date(date.getTime() - offset * 60 * 1000);
    return adjustedDate.toISOString().split('T')[0];
  }

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const formValue = this.form.getRawValue();
    const projectData: Proyecto = {
      ...formValue,
      fechaInicio: this.formatDateForBackend(formValue.fechaInicio),
      fechaFinEstimada: this.formatDateForBackend(formValue.fechaFinEstimada),
      fechaFinalizacionReal: this.formatDateForBackend(
        formValue.fechaFinalizacionReal
      ),
      fechaUltimoPagoRecibido: this.formatDateForBackend(
        formValue.fechaUltimoPagoRecibido
      ),
    };

    // El servicio de update espera 1 solo argumento (el objeto completo).
    const serviceCall = this.data
      ? this.proyectosService.updateProyecto(projectData)
      : this.proyectosService.addProyecto(projectData);

    const successKey = this.data
      ? 'PROJECTS.SUCCESSFULLY_UPDATED'
      : 'PROJECTS.SUCCESSFULLY_CREATED';

    serviceCall.subscribe({
      next: () => {
        this.snackBar.open(
          this.translate.instant(successKey),
          this.translate.instant('GLOBAL.CLOSE'),
          { duration: 3000 }
        );
        this.notificationService.notifyDataChange();
        this.dialogRef.close(true);
      },
      error: (err: HttpErrorResponse) => {
        const errorKey = err.error?.message || 'error.unexpected';
        const translatedMessage = this.translate.instant(errorKey);
        this.snackBar.open(
          translatedMessage,
          this.translate.instant('GLOBAL.CLOSE'),
          { duration: 7000 }
        );
      },
    });
  }

  onCancel(): void {
    this.dialogRef.close(false);
  }
}
