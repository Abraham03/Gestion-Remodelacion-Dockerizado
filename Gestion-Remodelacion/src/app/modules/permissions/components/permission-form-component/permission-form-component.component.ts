import { Component, Inject, OnInit, inject } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef, MatDialogModule } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';

import { Permission, PermissionRequest } from '../../../../core/models/permission.model';
import { PermissionService } from '../../services/permission.service';
import { NotificationService } from '../../../../core/services/notification.service'; 

// Angular Material Imports
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatSelectModule } from '@angular/material/select';

@Component({
  selector: 'app-permission-form-component',
  standalone: true,
  imports: [
    CommonModule, ReactiveFormsModule, MatDialogModule, MatInputModule,
    MatFormFieldModule, MatButtonModule, MatSelectModule, TranslateModule
  ],
  templateUrl: './permission-form-component.component.html',
  styleUrl: './permission-form-component.component.scss'
})
export class PermissionFormComponent implements OnInit {
  permissionForm: FormGroup;
  isEditMode: boolean;
  scopes = ['PLATFORM', 'TENANT'];

  private fb = inject(FormBuilder);
  private dialogRef = inject(MatDialogRef<PermissionFormComponent>);
  private permissionService = inject(PermissionService);
  private snackBar = inject(MatSnackBar);
  private translate = inject(TranslateService);
  
  constructor(
    @Inject(MAT_DIALOG_DATA) public data: Permission | undefined,
    private notificationService: NotificationService
  ) {
    this.isEditMode = !!data;
    this.permissionForm = this.fb.group({
      id: [data?.id || null],
      name: [data?.name || '', Validators.required],
      description: [data?.description || '', Validators.required],
      scope: [data?.scope || 'TENANT', Validators.required]
    });
  }

  ngOnInit(): void {
    // Si estamos editando, no se puede cambiar el nombre ni el scope por seguridad
    if (this.isEditMode) {
      this.permissionForm.get('name')?.disable();
    }
  }

  onSubmit(): void {
    if (this.permissionForm.invalid) {
      return;
    }

    const permissionRequest: Permission = this.permissionForm.getRawValue();

    const serviceCall = this.isEditMode
      ? this.permissionService.updatePermission(permissionRequest)
      : this.permissionService.createPermission(permissionRequest);
    
    const successKey = this.isEditMode ? 'PERMISSIONS.SUCCESSFULLY_UPDATED' : 'PERMISSIONS.SUCCESSFULLY_CREATED';

    serviceCall.subscribe({
      next: () => {
        this.snackBar.open(this.translate.instant(successKey), this.translate.instant('GLOBAL.CLOSE'), { duration: 3000 });
        this.notificationService.notifyDataChange();
        this.dialogRef.close(true);
      },
      error: (err: HttpErrorResponse) => {
        const errorKey = err.error?.message || 'error.unexpected';
        this.snackBar.open(this.translate.instant(errorKey), this.translate.instant('GLOBAL.CLOSE'), { duration: 7000 });
      },
    });
  }

  onCancel(): void {
    this.dialogRef.close(false);
  }
}
