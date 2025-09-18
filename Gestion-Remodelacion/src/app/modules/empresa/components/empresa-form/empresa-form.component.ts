import { Component, Inject, OnInit, inject } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { CommonModule } from '@angular/common';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatButtonModule } from '@angular/material/button';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { provideNativeDateAdapter } from '@angular/material/core';
import { MatSelectModule } from '@angular/material/select';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { HttpErrorResponse } from '@angular/common/http';
import { of } from 'rxjs';
import { switchMap } from 'rxjs/operators';

import { Empresa } from '../../model/Empresa';
import { EmpresaService } from '../../service/empresa.service';

@Component({
  selector: 'app-empresa-form',
  standalone: true,
  imports: [
    CommonModule, ReactiveFormsModule, MatFormFieldModule, MatInputModule, MatDialogModule,
    MatCheckboxModule, MatButtonModule, MatDatepickerModule, MatSelectModule, MatSnackBarModule,
    TranslateModule, MatIconModule, MatTooltipModule
  ],
  providers: [provideNativeDateAdapter()],
  templateUrl: './empresa-form.component.html',
  styleUrls: ['./empresa-form.component.scss']
})
export class EmpresaFormComponent implements OnInit {
  empresaForm: FormGroup;
  isEditMode: boolean;
  planes = ['BASICO', 'NEGOCIOS', 'PROFESIONAL'];
  estadosSuscripcion = ['ACTIVA', 'CANCELADA', 'VENCIDA'];
  selectedFile: File | null = null;

  private translate = inject(TranslateService);

  constructor(
    private fb: FormBuilder,
    private empresaService: EmpresaService,
    public dialogRef: MatDialogRef<EmpresaFormComponent>,
    @Inject(MAT_DIALOG_DATA) public data: Empresa | null,
    private snackBar: MatSnackBar
  ) {
    this.isEditMode = !!this.data;
    this.empresaForm = this.fb.group({
      id: [this.data?.id || null],
      nombreEmpresa: [this.data?.nombreEmpresa || '', Validators.required],
      activo: [this.data?.activo ?? true, Validators.required],
      plan: [this.data?.plan || 'BASICO', Validators.required],
      estadoSuscripcion: [this.data?.estadoSuscripcion || 'ACTIVA', Validators.required],
      fechaInicioSuscripcion: [this.data?.fechaInicioSuscripcion || null],
      fechaFinSuscripcion: [this.data?.fechaFinSuscripcion || null],
      logoUrl: [this.data?.logoUrl || ''],
      telefono: [this.data?.telefono || '']
    });
  }

  ngOnInit(): void {
    // Si el usuario escribe en el campo URL, se borra el archivo seleccionado.
    this.empresaForm.get('logoUrl')?.valueChanges.subscribe(value => {
      if (value && this.selectedFile) {
        this.selectedFile = null;
      }
    });
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files[0]) {
      this.selectedFile = input.files[0];
      this.empresaForm.patchValue({ logoUrl: '' }); // Limpia el campo de URL.
    }
  }

  clearSelectedFile(): void {
    this.selectedFile = null;
    const fileInput = document.querySelector('input[type=file]') as HTMLInputElement;
    if (fileInput) fileInput.value = '';
  }

  onSubmit(): void {
    if (this.empresaForm.invalid) {
      this.empresaForm.markAllAsTouched();
      return;
    }

    const empresaData: Empresa = this.empresaForm.value;

    const saveOrUpdate$ = this.isEditMode
      ? this.empresaService.updateEmpresa(this.data!.id!, empresaData)
      : this.empresaService.createEmpresa(empresaData);

    saveOrUpdate$.pipe(
      switchMap((empresaGuardada: Empresa) => {
        // Si hay un archivo seleccionado, se sube después de guardar los datos del formulario.
        if (this.selectedFile) {
          return this.empresaService.uploadLogo(empresaGuardada.id!, this.selectedFile);
        }
        // Si no hay archivo, se continúa sin hacer nada más.
        return of(null);
      })
    ).subscribe({
      next: () => {
        const successKey = this.isEditMode ? 'EMPRESAS.SUCCESSFULLY_UPDATED' : 'EMPRESAS.SUCCESSFULLY_CREATED';
        this.snackBar.open(this.translate.instant(successKey), this.translate.instant('GLOBAL.CLOSE'), { duration: 3000 });
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