import { Component, Inject, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule, FormArray, FormControl } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef, MatDialogModule } from '@angular/material/dialog';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatButtonModule } from '@angular/material/button';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { CommonModule } from '@angular/common';
import { PermissionService } from '../../../../../core/services/permission.service'; // Importar PermissionService
import { Role, RoleRequest } from '../../../../../core/models/role.model';
import { Permission } from '../../../../../core/models/permission.model';
import { MatSnackBar } from '@angular/material/snack-bar';
import { RoleService } from '../../../services/role.service';
import { HttpErrorResponse } from '@angular/common/http';
import { NotificationService } from '../../../../../core/services/notification.service';
import { provideNativeDateAdapter } from '@angular/material/core';

@Component({
  selector: 'app-role-form',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatInputModule,
    MatFormFieldModule,
    MatButtonModule,
    MatCheckboxModule
  ],
  providers: [provideNativeDateAdapter()],
  templateUrl: './role-form.component.html',
  styleUrls: ['./role-form.component.scss']
})
export class RoleFormComponent implements OnInit {
  roleForm: FormGroup;
  isEditMode: boolean;
  allPermissions: Permission[] = []; // Para almacenar todos los permisos disponibles

  constructor(
    private fb: FormBuilder,
    public dialogRef: MatDialogRef<RoleFormComponent>,
    @Inject(MAT_DIALOG_DATA) public data: Role | undefined,
    private roleService: RoleService,
    private permissionService: PermissionService, // Inyectar PermissionService
    private snackBar: MatSnackBar,
    private notificationService: NotificationService
  ) {
    this.isEditMode = !!data;
    this.roleForm = this.fb.group({
      id: [null],
      name: ['', Validators.required],
      description: ['', Validators.required],
      // Aseguramos que permissionIds sea un FormArray vacío inicialmente
      permissions: this.fb.array([], Validators.required) 
    });

    if (this.isEditMode && this.data) {
      this.roleForm.patchValue({
        id: this.data.id,
        name: this.data.name,
        description: this.data.description
      });
      // Los permisos se cargarán y marcarán en loadAllPermissions
    }
  }

  ngOnInit(): void {
    this.loadAllPermissions();
  }

  loadAllPermissions(): void {
    this.permissionService.getAllPermissions().subscribe({
      next: (permissions) => {
        this.allPermissions = permissions;
        if (this.isEditMode && this.data) {
          // Si estamos editando, marcamos los checkboxes de los permisos que ya tiene el rol
          const permissionFormArray = this.roleForm.get('permissions') as FormArray;
          this.data.permissions.forEach(p => {
            // Solo añadir si el permiso existe en la lista de todos los permisos disponibles
            if (this.allPermissions.some(ap => ap.id === p.id)) {
              permissionFormArray.push(new FormControl(p.id));
            }
          });
          // Actualizar el valor del control para reflejar los checkboxes seleccionados
          permissionFormArray.updateValueAndValidity();
        }
      },
      error: (err) => {
        console.error('Error loading permissions:', err);
        this.snackBar.open('Error al cargar la lista de permisos.', 'Cerrar', { duration: 3000 });
      }
    });
  }

  onPermissionChange(event: any, permissionId: number): void {
    const permissionFormArray = this.roleForm.get('permissions') as FormArray;

    if (event.checked) {
      // Añadir el ID del permiso si está marcado
      if (!permissionFormArray.controls.some(control => control.value === permissionId)) {
        permissionFormArray.push(new FormControl(permissionId));
      }
    } else {
      // Remover el ID del permiso si está desmarcado
      const index = permissionFormArray.controls.findIndex(x => x.value === permissionId);
      if (index > -1) {
        permissionFormArray.removeAt(index);
      }
    }
    // Opcional: Para forzar la validación de inmediato
    permissionFormArray.markAsDirty();
    permissionFormArray.updateValueAndValidity();

  }

  // Helper para saber si un permiso ya está asignado al rol (útil para pre-marcar checkboxes)
  isPermissionSelected(permissionId: number): boolean {
    const permissionFormArray = this.roleForm.get('permissions') as FormArray;
    return permissionFormArray.controls.some(control => control.value === permissionId);
  }

  onSubmit(): void {
    if (this.roleForm.invalid) {
      this.roleForm.markAllAsTouched();
      this.snackBar.open('Por favor, complete todos los campos requeridos.', 'Cerrar', { duration: 3000 });
      return;
    }

    const roleRequest: RoleRequest = this.roleForm.value;

    if (this.isEditMode && roleRequest.id) {
      this.roleService.updateRole(roleRequest.id, roleRequest).subscribe({
        next: (res) => {
          this.snackBar.open('Rol actualizado correctamente.', 'Cerrar', { duration: 3000 });
          this.notificationService.notifyDataChange();
          this.dialogRef.close(true);
        },
        error: (err: HttpErrorResponse) => {
                if (err.status === 409) {
                    this.snackBar.open('Error al actualizar: El nombre del rol ya existe.', 'Cerrar', { duration: 3000 });
                    // Optionally, mark the role name field as invalid
                    this.roleForm.get('name')?.setErrors({ 'alreadyExists': true });
                } else {
                    this.snackBar.open('Error al crear el rol. Inténtalo de nuevo.', 'Cerrar', { duration: 3000 });
                }
        },
      });
    } else {
      this.roleService.createRole(roleRequest).subscribe({
        next: (res) => {
          this.snackBar.open('Rol creado correctamente.', 'Cerrar', { duration: 3000 });
          this.notificationService.notifyDataChange();
          this.dialogRef.close(true);
        },
        error: (err: HttpErrorResponse) => {
                if (err.status === 409) {
                    this.snackBar.open('Error: El nombre del rol ya existe.', 'Cerrar', { duration: 3000 });
                    // Optionally, mark the role name field as invalid
                    this.roleForm.get('name')?.setErrors({ 'alreadyExists': true });
                } else {
                    this.snackBar.open('Error al crear el rol. Inténtalo de nuevo.', 'Cerrar', { duration: 3000 });
                }
        },
      });
    }
  }

  onCancel(): void {
    this.dialogRef.close(false);
  }
}