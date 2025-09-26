import { Component, OnInit, inject } from '@angular/core';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { MatSnackBar } from '@angular/material/snack-bar';
import { AuthApiService } from '../../services/auth-api.service';
import { TranslateModule, TranslateService } from '@ngx-translate/core';

// Importaciones de Angular Material y otros
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { UserRequest } from '../../../../core/models/user.model';
import { HttpErrorResponse } from '@angular/common/http';
import { InvitationService } from '../../services/invitation.service';

@Component({
  selector: 'app-register-invitation',
  standalone: true,
  imports: [
    CommonModule, ReactiveFormsModule, RouterModule,
    MatCardModule, MatFormFieldModule, MatInputModule,
    MatButtonModule, MatIconModule, MatProgressSpinnerModule, TranslateModule
  ],
  templateUrl: './register-invitation.component.html',
  styleUrls: ['./register-invitation.component.scss']
})
export class RegisterInvitationComponent implements OnInit {
  registerForm: FormGroup;
  isLoading = false;
  token: string | null = null;
  errorMessage: string | null = null;
  hidePassword = true;

  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private fb = inject(FormBuilder);
  private authApiService = inject(AuthApiService);
  private snackBar = inject(MatSnackBar);
  private translate = inject(TranslateService);
  private invitationService = inject(InvitationService);

  constructor() {
    this.registerForm = this.fb.group({
      username: ['', Validators.required],
      email: [{ value: '', disabled: true }, [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]]
    });
  }

  ngOnInit(): void {
    this.token = this.route.snapshot.queryParamMap.get('token');

    if (!this.token) {
      this.errorMessage = 'VALIDATION.TOKEN_MISSING';
      return;
    }

    this.isLoading = true; // Mostramos spinner mientras validamos
    
    // --- LÓGICA DE VALIDACIÓN ---
    this.invitationService.validateToken(this.token).subscribe({
      next: (details) => {
        console.log(details);
        // Si el token es válido, llenamos y bloqueamos el campo de email
        this.registerForm.patchValue({ email: details.email });
        this.registerForm.get('email')?.disable();
        this.isLoading = false;
      },
      error: (err: HttpErrorResponse) => {
        // Si el token no es válido, mostramos el error del backend
        this.errorMessage = err.error?.message || 'VALIDATION.TOKEN_INVALID';
        this.isLoading = false;
      }
    });
  }

  onSubmit(): void {
    if (this.registerForm.invalid || !this.token) {
      return;
    }
    this.isLoading = true;
    
    // Incluimos el email (deshabilitado en el form) en el objeto a enviar
    const formValue = this.registerForm.getRawValue();

    // Creamos el objeto UserRequest
    const signupData: UserRequest = {
      username: formValue.username,
      password: formValue.password,
      email: formValue.email,
      enabled: true,
      roleIds: [] // El backend asignará el rol por defecto
    };

    this.authApiService.registerByInvitation(signupData, this.token).subscribe({
      next: () => {
        this.snackBar.open(this.translate.instant('AUTH.REGISTER_SUCCESS'), this.translate.instant('GLOBAL.CLOSE'), { duration: 5000 });
        this.router.navigate(['/login']);
      },
      error: (err: HttpErrorResponse) => {
        this.isLoading = false;
        this.errorMessage = err.error?.message || 'AUTH.REGISTER_ERROR';
        
        if (this.errorMessage) {
          this.snackBar.open(this.translate.instant(this.errorMessage), this.translate.instant('GLOBAL.CLOSE'), {
            duration: 7000,
            panelClass: ['error-snackbar']
          });          
        }

      },
      complete: () => {
        this.isLoading = false;
      }
    });
  }
}