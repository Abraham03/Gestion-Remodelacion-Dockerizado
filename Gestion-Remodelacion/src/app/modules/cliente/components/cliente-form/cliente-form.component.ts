import { Component, OnInit, Inject, inject } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA, MatDialogContent, MatDialogActions, MatDialogTitle } from '@angular/material/dialog';
import { CommonModule } from '@angular/common';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { HttpErrorResponse } from '@angular/common/http';
import { MatSnackBar } from '@angular/material/snack-bar';
import { provideNativeDateAdapter } from '@angular/material/core';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { NotificationService } from '../../../../core/services/notification.service';
import { Cliente } from '../../models/cliente.model';
import { ClienteService } from '../../services/cliente.service';

@Component({
  selector: 'app-cliente-form',
  standalone: true,
  imports: [
    CommonModule, ReactiveFormsModule, MatFormFieldModule, MatInputModule, MatButtonModule,
    MatDialogContent, MatDialogActions, MatDialogTitle, TranslateModule,
  ],
  providers: [provideNativeDateAdapter()],
  templateUrl: './cliente-form.component.html',
  styleUrls: ['./cliente-form.component.scss']
})
export class ClienteFormComponent implements OnInit {
  form: FormGroup;
  private translate = inject(TranslateService);
  private notificationService = inject(NotificationService);
  private snackBar = inject(MatSnackBar);
  private clienteService = inject(ClienteService);


  constructor(
    private fb: FormBuilder,
    public dialogRef: MatDialogRef<ClienteFormComponent>,
    @Inject(MAT_DIALOG_DATA) public data: Cliente | null,
  ) {
    this.form = this.fb.group({
      id: [data?.id || null],
      nombreCliente: [data?.nombreCliente || '', Validators.required],
      telefonoContacto: [data?.telefonoContacto || '', [Validators.required, Validators.pattern(/^\d{10}$/)]],
      direccion: [data?.direccion || ''],
      notas: [data?.notas || ''],
    });
  }

  ngOnInit(): void {}

  onSubmit(): void {
    if (this.form.valid) {
      const clienteToSend: Cliente = this.form.value;
      const successMessageKey = clienteToSend.id ? 'CLIENTS.SUCCESSFULLY_UPDATED' : 'CLIENTS.SUCCESSFULLY_CREATED';
      const closeAction = this.translate.instant('GLOBAL.CLOSE');

      const serviceCall = clienteToSend.id
        ? this.clienteService.updateCliente(clienteToSend.id, clienteToSend)
        : this.clienteService.createCliente(clienteToSend);

      serviceCall.subscribe({
        next: () => {
          this.snackBar.open(this.translate.instant(successMessageKey), closeAction, { duration: 3000 });
          this.notificationService.notifyDataChange();
          this.dialogRef.close(true);
        },

        error: (err: HttpErrorResponse) => {
          // 1. Obtenemos la clave de error del backend. Si no existe, usamos una por defecto.
          const errorKey = err.error?.message || 'error.unexpected';
          
          // 2. Traducimos la clave para obtener el mensaje en el idioma actual.
          const translatedMessage = this.translate.instant(errorKey);

          // 3. Mostramos el mensaje ya traducido al usuario.
          this.snackBar.open(translatedMessage, closeAction, { duration: 7000 });
        }
      });
    } else {
      this.form.markAllAsTouched();
    }
  }

  onCancel(): void {
    this.dialogRef.close(false);
  }
}