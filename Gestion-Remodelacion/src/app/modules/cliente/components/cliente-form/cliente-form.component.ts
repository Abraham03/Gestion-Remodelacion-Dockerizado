import { Component, OnInit, Inject } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA, MatDialogContent, MatDialogActions, MatDialogTitle } from '@angular/material/dialog';
import { CommonModule } from '@angular/common';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';

import { Cliente } from '../../models/cliente.model';
import { ClienteService } from '../../services/cliente.service';

@Component({
  selector: 'app-cliente-form',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatDialogContent,
    MatDialogActions,
    MatDialogTitle
  ],
  templateUrl: './cliente-form.component.html',
  styleUrls: ['./cliente-form.component.scss']
})
export class ClienteFormComponent implements OnInit {
  form: FormGroup;

  constructor(
    private fb: FormBuilder,
    private clienteService: ClienteService,
    public dialogRef: MatDialogRef<ClienteFormComponent>,
    @Inject(MAT_DIALOG_DATA) public data: Cliente | null
  ) {
    this.form = this.fb.group({
      id: [data?.id || null],
      nombreCliente: [data?.nombreCliente || '', Validators.required],
      telefonoContacto: [data?.telefonoContacto || '', [Validators.required, Validators.pattern(/^\d{10}$/)]],
      direccion: [data?.direccion || ''],
      notas: [data?.notas || ''], // Add notas to the form
      // fechaRegistro: [data?.fechaRegistro ? new Date(data.fechaRegistro) : null] // Consider how you handle this. Backend generates it.
    });
  }

  ngOnInit(): void {}

  onSubmit(): void {
    if (this.form.valid) {
      const clienteToSend: Cliente = this.form.value;

      if (clienteToSend.id) {
        // Actualizar cliente existente
        this.clienteService.updateCliente(clienteToSend.id, clienteToSend).subscribe({
          next: () => {
            this.dialogRef.close(true);
          },
          error: (err) => {
            console.error('Error al actualizar cliente:', err);
            // TODO: Mostrar un MatSnackBar con el error
          }
        });
      } else {
        // Añadir nuevo cliente
        this.clienteService.createCliente(clienteToSend).subscribe({
          next: () => {
            this.dialogRef.close(true);
          },
          error: (err) => {
            console.error('Error al crear cliente:', err);
            // TODO: Mostrar un MatSnackBar con el error
          }
        });
      }
    } else {
      this.form.markAllAsTouched();
      console.warn('Formulario inválido. Revise los campos.');
    }
  }

  onCancel(): void {
    this.dialogRef.close(false);
  }
}