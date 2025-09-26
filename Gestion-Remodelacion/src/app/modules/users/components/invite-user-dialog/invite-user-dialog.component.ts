import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { Component, inject, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { MatDialogRef } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { UserService } from '../../services/user.service';
// Importaciones de Angular Material
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatDialogModule, MatDialogTitle, MatDialogContent, MatDialogActions } from '@angular/material/dialog';
import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { MatSelectModule } from '@angular/material/select';
import { EmpresaDropdown } from '../../../empresa/model/Empresa';
import { AuthService } from '../../../../core/services/auth.service';
import { EmpresaService } from '../../../empresa/service/empresa.service';

@Component({
  selector: 'app-invite-user-dialog',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatDialogModule,
    TranslateModule,
    MatSelectModule
  ],
  templateUrl: './invite-user-dialog.component.html',
  styleUrls: ['./invite-user-dialog.component.scss']
})
export class InviteUserDialogComponent implements OnInit {
  inviteForm: FormGroup;
  isLoading = false;
  isSuperAdmin = false;
  empresas: EmpresaDropdown[] = [];


  private fb = inject(FormBuilder);
  private userService = inject(UserService);
  private dialogRef = inject(MatDialogRef<InviteUserDialogComponent>);
  private snackBar = inject(MatSnackBar);
  private translate = inject(TranslateService);
  private authService = inject(AuthService);
  private empresaService = inject(EmpresaService);


  constructor() {
    this.inviteForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      empresaId: [null]
    });
  }

  ngOnInit(): void {
    this.isSuperAdmin = this.authService.isSuperAdmin;
    if (this.isSuperAdmin) {
      this.inviteForm.get('empresaId')?.setValidators(Validators.required);
      this.loadEmpresas();
    }
  }

   loadEmpresas(): void {
    this.empresaService.getAllEmpresasForForm().subscribe({
      next: (data) => this.empresas = data,
      error: () => this.snackBar.open(this.translate.instant('USERS.ERROR_LOADING_COMPANIES'), this.translate.instant('GLOBAL.CLOSE'), { duration: 3000 })
    });
  } 

  onSubmit(): void {
    if (this.inviteForm.invalid || this.isLoading) {
      return;
    }

    this.isLoading = true;
    const formValue = this.inviteForm.value;

    this.userService.inviteUser(formValue.email, formValue.empresaId).subscribe({
      next: () => {
        this.snackBar.open(this.translate.instant('USERS.INVITATION_SENT_SUCCESS'), this.translate.instant('GLOBAL.CLOSE'), { duration: 5000 });
        this.dialogRef.close(true);
      },
      error: (err: HttpErrorResponse) => {
        this.isLoading = false;
        const errorMessage = err.error?.message || 'USERS.INVITATION_SENT_ERROR';
        this.snackBar.open(this.translate.instant(errorMessage), this.translate.instant('GLOBAL.CLOSE'), {
          duration: 7000,
          panelClass: ['error-snackbar']
        });
      },
      complete: () => {
        this.isLoading = false;
      }
    });
  }

  onCancel(): void {
    this.dialogRef.close();
  }
}