import { Component, Inject, OnInit, inject } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule, FormArray, FormControl } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef, MatDialogModule } from '@angular/material/dialog';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatButtonModule } from '@angular/material/button';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { CommonModule } from '@angular/common';
import { MatSnackBar } from '@angular/material/snack-bar';
import { HttpErrorResponse } from '@angular/common/http';
import { TranslateModule, TranslateService } from '@ngx-translate/core';

import { PermissionService } from '../../../../permissions/services/permission.service';
import { Role, RoleRequest } from '../../../../../core/models/role.model';
import { Permission, PermissionDropdownResponse } from '../../../../../core/models/permission.model';
import { RoleService } from '../../../services/role.service';
import { NotificationService } from '../../../../../core/services/notification.service';

@Component({
  selector: 'app-role-form',
  standalone: true,
  imports: [
    CommonModule, ReactiveFormsModule, MatDialogModule, MatInputModule,
    MatFormFieldModule, MatButtonModule, MatCheckboxModule, TranslateModule
  ],
  templateUrl: './role-form.component.html',
  styleUrls: ['./role-form.component.scss']
})
export class RoleFormComponent implements OnInit {
  roleForm: FormGroup;
  isEditMode: boolean;
  allPermissions: PermissionDropdownResponse[] = [];

  private translate = inject(TranslateService);

  constructor(
    private fb: FormBuilder,
    public dialogRef: MatDialogRef<RoleFormComponent>,
    @Inject(MAT_DIALOG_DATA) public data: Role | undefined,
    private roleService: RoleService,
    private permissionService: PermissionService,
    private snackBar: MatSnackBar,
    private notificationService: NotificationService
  ) {
    this.isEditMode = !!data;
    this.roleForm = this.fb.group({
      id: [data?.id || null],
      name: [data?.name || '', Validators.required],
      description: [data?.description || '', Validators.required],
      permissions: this.fb.array([], Validators.required) 
    });
  }

  ngOnInit(): void {
    this.loadAllPermissions();
  }

  loadAllPermissions(): void {
    this.permissionService.getPermissionsForDropdown().subscribe({
      next: (permissions) => {
        this.allPermissions = permissions;
        console.log(this.allPermissions);
        if (this.isEditMode && this.data?.permissions) {
          const permissionFormArray = this.roleForm.get('permissions') as FormArray;
          this.data.permissions.forEach(p => {
            if (this.allPermissions.some(ap => ap.id === p.id)) {
              permissionFormArray.push(new FormControl(p.id));
            }
          });
        }
      },
      error: () => {
        this.snackBar.open(this.translate.instant('ROLES.ERROR_LOADING_PERMISSIONS'), this.translate.instant('GLOBAL.CLOSE'), { duration: 3000 });
      }
    });
  }

  onPermissionChange(event: any, permissionId: number): void {
    const permissionFormArray = this.roleForm.get('permissions') as FormArray;
    if (event.checked) {
      if (!permissionFormArray.controls.some(control => control.value === permissionId)) {
        permissionFormArray.push(new FormControl(permissionId));
      }
    } else {
      const index = permissionFormArray.controls.findIndex(x => x.value === permissionId);
      if (index > -1) {
        permissionFormArray.removeAt(index);
      }
    }
  }

  isPermissionSelected(permissionId: number): boolean {
    const permissionFormArray = this.roleForm.get('permissions') as FormArray;
    return permissionFormArray.controls.some(control => control.value === permissionId);
  }

  onSubmit(): void {
    if (this.roleForm.invalid) {
      this.roleForm.markAllAsTouched();
      this.snackBar.open(this.translate.instant('ROLES.VALIDATION_ERROR'), this.translate.instant('GLOBAL.CLOSE'), { duration: 3000 });
      return;
    }

    const roleRequest: Role = this.roleForm.value;
    
    // La llamada a `updateRole` debe recibir el bjeto.
    const serviceCall = this.isEditMode && this.data
      ? this.roleService.updateRole(roleRequest)
      : this.roleService.createRole(roleRequest);

    const successKey = this.isEditMode ? 'ROLES.SUCCESSFULLY_UPDATED' : 'ROLES.SUCCESSFULLY_CREATED';

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