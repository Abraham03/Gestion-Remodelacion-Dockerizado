import { Component, Inject, OnInit } from '@angular/core';
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
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatButtonModule } from '@angular/material/button';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatSelectModule } from '@angular/material/select';
import { CommonModule } from '@angular/common';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Role } from '../../../../../core/models/role.model';
import { User, UserRequest } from '../../../../../core/models/user.model';
import { RoleService } from '../../../../roles/services/role.service';
import { UserService } from '../../../services/user.service';
import { HttpErrorResponse } from '@angular/common/http';

@Component({
  selector: 'app-user-form',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatInputModule,
    MatFormFieldModule,
    MatButtonModule,
    MatCheckboxModule,
    MatSelectModule,
  ],
  templateUrl: './user-form.component.html',
  styleUrls: ['./user-form.component.scss'],
})
export class UserFormComponent implements OnInit {
  userForm: FormGroup;
  isEditMode: boolean;
  roles: Role[] = []; // Para almacenar todos los roles disponibles

  constructor(
    private fb: FormBuilder,
    public dialogRef: MatDialogRef<UserFormComponent>,
    @Inject(MAT_DIALOG_DATA) public data: User | undefined, // 'data' puede ser un User o undefined
    private userService: UserService,
    private roleService: RoleService, // Inyectar RoleService
    private snackBar: MatSnackBar
  ) {
    this.isEditMode = !!data;
    this.userForm = this.fb.group({
      id: [null],
      username: ['', Validators.required],
      password: ['', this.isEditMode ? [] : Validators.required], // Password es requerido solo al crear
      enabled: [true],
      roles: [[], Validators.required], // Usamos roleIds para los IDs de los roles seleccionados
    });

    if (this.isEditMode && this.data) {
      this.userForm.patchValue({
        id: this.data.id,
        username: this.data.username,
        enabled: this.data.enabled,
        roles: this.data.roles.map((role) => role.id), // Mapea los roles del usuario a sus IDs
      });
    }
  }

  ngOnInit(): void {
    this.loadRoles(); // Carga todos los roles disponibles al inicializar el formulario
  }

  loadRoles(): void {
    // El servicio devuelve directamente un array de Role[], no necesitas `data.content` aquí.
    this.roleService.getAllRolesForForm().subscribe({
      next: (roles) => {
        this.roles = roles; // `roles` ya es un array
      },
      error: (err) => {
        console.error('Error loading roles:', err);
        this.snackBar.open('Error al cargar los roles disponibles.', 'Cerrar', {
          duration: 3000,
        });
      },
    });
  }

  onSubmit(): void {
    if (this.userForm.invalid) {
      this.userForm.markAllAsTouched(); // Marcar todos los campos como tocados para mostrar errores
      return;
    }

    const userRequest: UserRequest = this.userForm.value;

    if (this.isEditMode && userRequest.id) {
      this.userService.updateUser(userRequest.id, userRequest).subscribe({
        next: (res) => {
          this.snackBar.open('Usuario actualizado correctamente.', 'Cerrar', {duration: 3000,});
          this.dialogRef.close(true);
        },
        error: (err: HttpErrorResponse) => {
          // Specifically check for the 409 Conflict status
          if (err.status === 409) {
            this.snackBar.open('Error al actualizar: El nombre de usuario ya existe.', 'Cerrar', { duration: 4000 });
            // Optionally, mark the username field as invalid
            this.userForm.get('username')?.setErrors({ 'alreadyExists': true });
          } else {
            // Handle other potential errors
            this.snackBar.open('Error al actualizar el usuario. Inténtalo de nuevo.', 'Cerrar', { duration: 3000 });
          }

        },
      });
    } else {
      this.userService.createUser(userRequest).subscribe({
        next: (res) => {
          this.snackBar.open('Usuario creado correctamente.', 'Cerrar', {
            duration: 3000,
          });
          this.dialogRef.close(true);
        },
        error: (err: HttpErrorResponse) => {
          // Specifically check for the 409 Conflict status
          if (err.status === 409) {
            this.snackBar.open('Error: El nombre de usuario ya existe.', 'Cerrar', { duration: 4000 });
            // Optionally, mark the username field as invalid
            this.userForm.get('username')?.setErrors({ 'alreadyExists': true });
          } else {
            // Handle other potential errors
            this.snackBar.open('Error al crear el usuario. Inténtalo de nuevo.', 'Cerrar', { duration: 3000 });
          }
        },
      });
    }
  }

  onCancel(): void {
    this.dialogRef.close(false);
  }
}
