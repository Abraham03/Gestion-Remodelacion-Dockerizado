import { Component, Inject, OnInit, inject } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA, MatDialogActions, MatDialogModule } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { CommonModule } from '@angular/common';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatButtonModule } from '@angular/material/button';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { provideNativeDateAdapter } from '@angular/material/core';
import { MatSelectModule } from '@angular/material/select';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { HttpErrorResponse } from '@angular/common/http';

import { EmpleadoService } from '../../services/empleado.service';
import { Empleado } from '../../models/empleado.model';
import { NumberFormatDirective } from '../../../../shared/directives/number-format.directive';

@Component({
  selector: 'app-empleado-form',
  standalone: true,
  imports: [
    CommonModule, ReactiveFormsModule, MatFormFieldModule, MatInputModule,
    MatCheckboxModule, MatButtonModule, MatDatepickerModule, MatDialogActions,
    MatDialogModule, MatSelectModule, NumberFormatDirective,
    TranslateModule,
  ],
  templateUrl: './empleado-form.component.html',
  styleUrls: ['./empleado-form.component.scss'],
  providers: [provideNativeDateAdapter()],
})
export class EmpleadoFormComponent implements OnInit {
  empleadoForm: FormGroup;
  isEditMode: boolean = false;
  modelosDePago: { value: string, viewValue: string }[] = [];
  
  private translate = inject(TranslateService);

  constructor(
    private fb: FormBuilder,
    private empleadoService: EmpleadoService,
    public dialogRef: MatDialogRef<EmpleadoFormComponent>,
    @Inject(MAT_DIALOG_DATA) public data: Empleado,
    private snackBar: MatSnackBar,
  ) {
    this.empleadoForm = this.fb.group({
      id: [null],
      nombreCompleto: ['', Validators.required],
      rolCargo: ['', Validators.required],
      telefonoContacto: [''],
      fechaContratacion: [null],
      costoPorHora: [0, [Validators.required, Validators.min(0.01)]],
      modeloDePago: ['POR_HORA', Validators.required],
      activo: [true],
      notas: [''],
    });
  }

  ngOnInit(): void {
    this.setupDynamicTranslations();

    if (this.data) {
      this.isEditMode = true;
      const empleadoParaForm = { ...this.data };
      let montoParaForm = empleadoParaForm.modeloDePago === 'POR_DIA'
        ? empleadoParaForm.costoPorHora * 8
        : empleadoParaForm.costoPorHora;
      
      const patchData = { ...empleadoParaForm, costoPorHora: montoParaForm };
      
      if (empleadoParaForm.fechaContratacion) {
        const dateString = empleadoParaForm.fechaContratacion as string;
        const parts = dateString.split('-').map(Number);
        const localDate = new Date(parts[0], parts[1] - 1, parts[2]);
        patchData.fechaContratacion = localDate;
      }
      this.empleadoForm.patchValue(patchData);
    }
  }

  private setupDynamicTranslations(): void {
    this.generatePaymentModels();
    this.translate.onLangChange.subscribe(() => {
      this.generatePaymentModels();
    });
  }

  private generatePaymentModels(): void {
    this.modelosDePago = [
      { value: 'POR_HORA', viewValue: this.translate.instant('EMPLOYEES.PAY_PER_HOUR') },
      { value: 'POR_DIA', viewValue: this.translate.instant('EMPLOYEES.PAY_PER_DAY') }
    ];
  }

  get montoLabel(): string {
    const modelo = this.empleadoForm.get('modeloDePago')?.value;
    const key = modelo === 'POR_DIA' ? 'EMPLOYEES.AMOUNT_PER_DAY' : 'EMPLOYEES.AMOUNT_PER_HOUR';
    return this.translate.instant(key);
  }

  onSubmit(): void {
    if (this.empleadoForm.valid) {
      const empleado: Empleado = { ...this.empleadoForm.value };

      if (empleado.fechaContratacion instanceof Date) {
        const date = empleado.fechaContratacion;
        empleado.fechaContratacion = `${date.getFullYear()}-${(date.getMonth() + 1).toString().padStart(2, '0')}-${date.getDate().toString().padStart(2, '0')}`;
      }

      const serviceCall = this.isEditMode
        ? this.empleadoService.updateEmpleado(empleado)
        : this.empleadoService.createEmpleado(empleado);
      
      const successKey = this.isEditMode ? 'EMPLOYEES.SUCCESSFULLY_UPDATED' : 'EMPLOYEES.SUCCESSFULLY_CREATED';

      serviceCall.subscribe({
        next: () => {
          this.snackBar.open(this.translate.instant(successKey), this.translate.instant('GLOBAL.CLOSE'), { duration: 3000 });
          this.dialogRef.close(true);
        },
        error: (err: HttpErrorResponse) => {
          console.error(`Error al ${this.isEditMode ? 'actualizar' : 'crear'} empleado:`, err);
          // 1. Obtenemos la clave de error del backend.
          const errorKey = err.error?.message || 'error.unexpected';
          // 2. Traducimos la clave para obtener el mensaje.
          const translatedMessage = this.translate.instant(errorKey);
          // 3. Mostramos el mensaje traducido.
          this.snackBar.open(translatedMessage, this.translate.instant('GLOBAL.CLOSE'), { duration: 5000 });
        }
      });
    }
  }
}