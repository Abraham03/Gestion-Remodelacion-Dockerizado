import { Component, DestroyRef, Inject, OnInit, inject } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef, MatDialogModule } from '@angular/material/dialog';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatButtonModule } from '@angular/material/button';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatSelectModule } from '@angular/material/select';
import { CommonModule } from '@angular/common';
import { MatSnackBar } from '@angular/material/snack-bar';
import { HttpErrorResponse } from '@angular/common/http';
import { TranslateModule, TranslateService } from '@ngx-translate/core';

import { Role, RoleDropdownResponse } from '../../../../../core/models/role.model';
import { User, UserRequest } from '../../../../../core/models/user.model';
import { RoleService } from '../../../../roles/services/role.service';
import { UserService } from '../../../services/user.service';
import { NotificationService } from '../../../../../core/services/notification.service';
import { EmpresaService } from '../../../../empresa/service/empresa.service';
import { EmpresaDropdown } from '../../../../empresa/model/Empresa';
import { AuthService } from '../../../../../core/services/auth.service';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { distinct, distinctUntilChanged, filter, tap } from 'rxjs';

@Component({
  selector: 'app-user-form',
  standalone: true,
  imports: [
    CommonModule, ReactiveFormsModule, MatDialogModule, MatInputModule, MatFormFieldModule,
    MatButtonModule, MatCheckboxModule, MatSelectModule, TranslateModule,
  ],
  templateUrl: './user-form.component.html',
  styleUrls: ['./user-form.component.scss'],
})
export class UserFormComponent implements OnInit {
  userForm: FormGroup;
  isEditMode: boolean;
  roles: RoleDropdownResponse[] = [];

  empresas: EmpresaDropdown[] = [];
  isSuperAdmin = false;
  private destroyRef = inject(DestroyRef); // Necesario para takeUntilDestroyed
  private translate = inject(TranslateService);

  constructor(
    private fb: FormBuilder,
    public dialogRef: MatDialogRef<UserFormComponent>,
    @Inject(MAT_DIALOG_DATA) public data: User | undefined,
    private userService: UserService,
    private roleService: RoleService,
    private snackBar: MatSnackBar,
    private notificationService: NotificationService,
    private authService: AuthService,
    private empresaService: EmpresaService
  ) {
    this.isEditMode = !!data;
    this.isSuperAdmin = this.authService.isSuperAdmin; 

    this.userForm = this.fb.group({
      id: [data?.id || null],
      username: [data?.username || '', Validators.required],
      password: ['', this.isEditMode ? [] : Validators.required],
      email: [data?.email || '', this.isEditMode ? [] : [Validators.required, Validators.email]],
      enabled: [data?.enabled ?? true],
      roles: [data?.roles.map(role => role.id) || [], Validators.required],
      empresaId: [data?.empresaId || null],
    });
  }

  ngOnInit(): void {
    if (this.isSuperAdmin){
      this.setupSuperAdminForm();
    }else{
      this.loadRolesForCurrentUser();
    }
  }

  private setupSuperAdminForm(): void {
    this.loadEmpresas();
    this.userForm.get('empresaId')?.setValidators(Validators.required);
    this.userForm.get('rolAAsignar')?.clearValidators(); // Roles no es requerido hasta seleccionar empresa
    this.userForm.get('rolAAsignar')?.updateValueAndValidity();

    // Escucha cambios en la seleccion de empresa
    this.userForm.get('empresaId')?.valueChanges.pipe(
      takeUntilDestroyed(this.destroyRef), // Usa el destroyRef inyectado
      distinctUntilChanged(),// Evita recargar si se selecciona la misma empresa
      tap(() => {// Limpia roles al cambiar empresa
        this.roles = [];
        this.userForm.get('rolAAsignar')?.reset();
        this.userForm.get('rolAAsignar')?.clearValidators();
        this.userForm.get('rolAAsignar')?.updateValueAndValidity();
      }),
      filter(empresaId => !!empresaId) // Solo carga si hay un ID de empresa seleccionado
    ).subscribe(empresaId => {
      this.loadRolesForSpecificEmpresa(empresaId);
    });

  }

   loadEmpresas(): void {
    this.empresaService.getAllEmpresasForForm().subscribe({
      next: (data) => {
        this.empresas = data;
        console.log(this.empresas);
      },
      error: (err) => {
        console.error('Error loading companies:', err);
        this.snackBar.open(this.translate.instant('USERS.ERROR_LOADING_COMPANIES'), this.translate.instant('GLOBAL.CLOSE'), { duration: 3000 });
      },
    });
  } 

  // Metodo para Admin normal (usa /all)
  loadRolesForCurrentUser(): void {
    this.roleService.getDropdownRoles().subscribe({
      next: (roles) => {
        this.roles = roles;
        console.log(this.roles);
      },
      error: (err) => {
        console.error('Error loading roles:', err);
        this.snackBar.open(this.translate.instant('USERS.ERROR_LOADING_ROLES'), this.translate.instant('GLOBAL.CLOSE'), { duration: 3000 });
      },
    });
  }

  // Metodo para Super Admin (usa /dropdown)
  loadRolesForSpecificEmpresa(empresaId: number): void {
    this.roleService.getDropdownRoles(empresaId).subscribe({
      next: (roles) => {
        this.roles = roles;
        // Ahora que hay roles, marca el control como requerido
        this.userForm.get('rolAAsignar')?.setValidators(Validators.required);
        this.userForm.get('rolAAsignar')?.updateValueAndValidity();
      },
      error: (err) => {
        console.error('Error loading roles:', err);
        this.snackBar.open(this.translate.instant('USERS.ERROR_LOADING_ROLES'), this.translate.instant('GLOBAL.CLOSE'), { duration: 3000 });
      },
    });
  }

  onSubmit(): void {
    if (this.userForm.invalid) {
      this.userForm.markAllAsTouched();
      this.snackBar.open(this.translate.instant('ROLES.VALIDATION_ERROR'), this.translate.instant('GLOBAL.CLOSE'), { duration: 3000 });
      return;
    }

    const userRequest: UserRequest = this.userForm.value;
    const serviceCall = this.isEditMode
      ? this.userService.updateUser(this.data!.id!, userRequest)
      : this.userService.createUser(userRequest);
    
    const successKey = this.isEditMode ? 'USERS.SUCCESSFULLY_UPDATED' : 'USERS.SUCCESSFULLY_CREATED';

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