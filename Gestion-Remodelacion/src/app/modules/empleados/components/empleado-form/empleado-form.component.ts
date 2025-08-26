// src/app/modules/empleados/components/empleado-form/empleado-form.component.ts
import { Component, Inject, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA, MatDialogActions, MatDialogModule } from '@angular/material/dialog';
import { EmpleadoService } from '../../services/empleado.service';
import { Empleado } from '../../models/empleado.model';
import { MatSnackBar } from '@angular/material/snack-bar';
import { CommonModule } from '@angular/common';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatButtonModule } from '@angular/material/button';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { provideNativeDateAdapter } from '@angular/material/core';

@Component({
  selector: 'app-empleado-form',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatCheckboxModule,
    MatButtonModule,
    MatDatepickerModule,
    MatDialogActions,
    MatDialogModule
],
  templateUrl: './empleado-form.component.html',
  styleUrls: ['./empleado-form.component.scss'],
  providers: [provideNativeDateAdapter()],
})
export class EmpleadoFormComponent implements OnInit {
  empleadoForm: FormGroup;
  isEditMode: boolean = false;

  constructor(
    private fb: FormBuilder,
    private empleadoService: EmpleadoService,
    public dialogRef: MatDialogRef<EmpleadoFormComponent>,
    @Inject(MAT_DIALOG_DATA) public data: Empleado,
    private snackBar: MatSnackBar
  ) {
    this.empleadoForm = this.fb.group({
      id: [null],
      nombreCompleto: ['', Validators.required],
      rolCargo: ['', Validators.required],
      telefonoContacto: [''],
      fechaContratacion: [null], // Maintain as null initially for datepicker
      costoPorHora: [0, [Validators.required, Validators.min(0.01)]],
      activo: [true],
      notas: [''],
    });
  }

  ngOnInit(): void {
    if (this.data) {
      this.isEditMode = true;
      const empleadoParaForm = { ...this.data };
      if (empleadoParaForm.fechaContratacion) {
        // --- START OF NEW CHANGE ---
        // Manually parse and create a Date object using local setters
        // This ensures the date is set without timezone shifting its day
        const dateString = empleadoParaForm.fechaContratacion as string; // e.g., "2025-05-10"
        const parts = dateString.split('-').map(Number); // [2025, 5, 10]

        const localDate = new Date();
        localDate.setFullYear(parts[0], parts[1] - 1, parts[2]); // Month is 0-indexed
        localDate.setHours(0, 0, 0, 0); // Set time to midnight local to avoid issues

        empleadoParaForm.fechaContratacion = localDate;
        // --- END OF NEW CHANGE ---
      }
      this.empleadoForm.patchValue(empleadoParaForm);
    }
  }

  onSubmit(): void {
    if (this.empleadoForm.valid) {
      const empleado: Empleado = { ...this.empleadoForm.value };

      if (empleado.fechaContratacion instanceof Date) {
        // This logic is already correct for converting the Date object
        // (which holds local time from the picker) back to 'YYYY-MM-DD' string
        // for the backend, which expects LocalDate format.
        const date = empleado.fechaContratacion;
        const year = date.getFullYear();
        const month = (date.getMonth() + 1).toString().padStart(2, '0');
        const day = date.getDate().toString().padStart(2, '0');
        empleado.fechaContratacion = `${year}-${month}-${day}`;
      }

      if (this.isEditMode) {
        this.empleadoService.updateEmpleado(this.data.id!, empleado).subscribe(
          () => {
            this.snackBar.open('Empleado actualizado', 'Cerrar', { duration: 3000 });
            this.dialogRef.close(true);
          },
          (error) => {
            console.error('Error al actualizar empleado:', error);
            this.snackBar.open('Error al actualizar empleado. Inténtalo de nuevo.', 'Cerrar', { duration: 5000 });
          }
        );
      } else {
        if (empleado.activo === undefined || empleado.activo === null) {
          empleado.activo = true;
        }
        this.empleadoService.createEmpleado(empleado).subscribe(
          () => {
            this.snackBar.open('Empleado creado', 'Cerrar', { duration: 3000 });
            this.dialogRef.close(true);
          },
          (error) => {
            console.error('Error al crear empleado:', error);
            this.snackBar.open('Error al crear empleado. Inténtalo de nuevo.', 'Cerrar', { duration: 5000 });
          }
        );
      }
    }
  }
}