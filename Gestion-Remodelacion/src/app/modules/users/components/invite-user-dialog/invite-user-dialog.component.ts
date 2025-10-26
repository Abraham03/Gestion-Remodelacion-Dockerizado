import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { Component, inject, OnInit, DestroyRef } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { MatDialogRef } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatDialogModule } from '@angular/material/dialog';
import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { MatSelectModule } from '@angular/material/select';
import { EmpresaDropdown } from '../../../empresa/model/Empresa';
import { AuthService } from '../../../../core/services/auth.service';
import { EmpresaService } from '../../../empresa/service/empresa.service';
import { Role, RoleDropdownResponse } from '../../../../core/models/role.model';
import { RoleService } from '../../../roles/services/role.service';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { filter } from 'rxjs';
import { InvitationService } from '../../../auth/services/invitation.service';

@Component({
  selector: 'app-invite-user-dialog',
  standalone: true,
  imports: [
    CommonModule, ReactiveFormsModule, MatFormFieldModule, MatInputModule,
    MatButtonModule, MatDialogModule, TranslateModule, MatSelectModule
  ],
  templateUrl: './invite-user-dialog.component.html',
  styleUrls: ['./invite-user-dialog.component.scss']
})
export class InviteUserDialogComponent implements OnInit {
  inviteForm: FormGroup;
  isLoading = false;
  isSuperAdmin = false;
  empresas: EmpresaDropdown[] = [];
  rolesDeEmpresa: RoleDropdownResponse[] = [];

  private fb = inject(FormBuilder);
  private dialogRef = inject(MatDialogRef<InviteUserDialogComponent>);
  private snackBar = inject(MatSnackBar);
  private translate = inject(TranslateService);
  private authService = inject(AuthService);
  private empresaService = inject(EmpresaService);
  private roleService = inject(RoleService);
  private invitationService = inject(InvitationService);
  private destroyRef = inject(DestroyRef); // Inyecta DestroyRef

  constructor() {
    this.inviteForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      empresaId: [null],
      rolAAsignar: [null]
    });
  }

  ngOnInit(): void {
    this.isSuperAdmin = this.authService.isSuperAdmin;
    if (this.isSuperAdmin) {
      this.setupSuperAdminForm();
    }
  }

  private setupSuperAdminForm(): void {
    this.inviteForm.get('empresaId')?.setValidators(Validators.required);
    this.inviteForm.get('rolAAsignar')?.setValidators(Validators.required);
    this.loadEmpresas();

    this.inviteForm.get('empresaId')?.valueChanges.pipe(
      takeUntilDestroyed(this.destroyRef), // Usa el destroyRef inyectado
      filter(empresaId => !!empresaId)
    ).subscribe(empresaId => {
      this.rolesDeEmpresa = [];
      this.inviteForm.get('rolAAsignar')?.reset();
      this.loadRolesForEmpresa(empresaId);
    });
  }

  loadEmpresas(): void {
    this.empresaService.getAllEmpresasForForm().subscribe({
      next: (data) => this.empresas = data,
      error: () => this.snackBar.open(this.translate.instant('USERS.ERROR_LOADING_COMPANIES'), this.translate.instant('GLOBAL.CLOSE'), { duration: 3000 })
    });
  }

  loadRolesForEmpresa(empresaId: number): void {
    this.roleService.getDropdownRoles(empresaId).subscribe({
      next: (roles) => this.rolesDeEmpresa = roles,
      error: () => this.snackBar.open(this.translate.instant('USERS.ERROR_LOADING_ROLES'), this.translate.instant('GLOBAL.CLOSE'), { duration: 3000 })
    });
  }

  onSubmit(): void {
    if (this.inviteForm.invalid || this.isLoading) {
      return;
    }

    this.isLoading = true;
    const { email, empresaId, rolAAsignar } = this.inviteForm.value;

    const inviteCall$ = this.isSuperAdmin
      ? this.invitationService.inviteUserBySuperAdmin(email, empresaId, rolAAsignar)
      : this.invitationService.inviteUser(email);

    inviteCall$.subscribe({
      next: () => {
        this.snackBar.open(this.translate.instant('USERS.INVITATION_SENT_SUCCESS'), this.translate.instant('GLOBAL.CLOSE'), { duration: 5000 });
        this.dialogRef.close(true);
      },
      error: (err: HttpErrorResponse) => {
        const errorMessage = err.error?.message || 'error.invitation.emailExists';
        this.snackBar.open(this.translate.instant(errorMessage), this.translate.instant('GLOBAL.CLOSE'), {
          duration: 7000,
          panelClass: ['error-snackbar']
        });
      },
    }).add(() => this.isLoading = false);
  }

  onCancel(): void {
    this.dialogRef.close();
  }
}