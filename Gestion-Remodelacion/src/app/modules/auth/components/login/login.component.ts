import { Component, inject } from '@angular/core';
import { FormBuilder, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router, ActivatedRoute, RouterLink } from '@angular/router'; // Importar RouterLink
import { AuthService } from '../../../../core/services/auth.service';
import { MatSnackBar } from '@angular/material/snack-bar';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatIconModule } from '@angular/material/icon'; // Importar MatIconModule

@Component({
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatProgressSpinnerModule,
    MatIconModule, // Añadir MatIconModule
    RouterLink // Añadir RouterLink
  ],
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent {
  private fb = inject(FormBuilder);
  private auth = inject(AuthService);
  private snackBar = inject(MatSnackBar);
  private router = inject(Router);
  private route = inject(ActivatedRoute);

  loginForm = this.fb.nonNullable.group({
    username: ['', [Validators.required, Validators.minLength(3)]],
    password: ['', [Validators.required, Validators.minLength(6)]]
  });

  isLoading = false;
  hidePassword = true; // Para la visibilidad de la contraseña
  returnUrl = this.route.snapshot.queryParams['returnUrl'] || '/dashboard';

  ngOnInit() {
    // Mostrar mensaje si la sesión expiró al cargar el componente
    if (history.state?.sessionExpired) {
      this.snackBar.open(
        'Tu sesión ha expirado. Por favor ingresa nuevamente.',
        'Cerrar',
        { duration: 5000 }
      );
      // Limpiar el estado para que el mensaje no se muestre en futuras cargas
      history.replaceState({}, '', this.router.url.split('?')[0]);
    }
  }

  onSubmit() {
    if (this.loginForm.invalid) {
      this.loginForm.markAllAsTouched(); // Marcar todos los campos como tocados para mostrar errores
      return;
    }

    const { username, password } = this.loginForm.getRawValue();
    this.isLoading = true;

    this.auth.login({ username, password }).subscribe({
      next: () => {
        this.snackBar.open('Bienvenido', 'Cerrar', { duration: 3000 });
       // this.router.navigateByUrl(this.returnUrl);
      },
      error: (err) => {
        this.isLoading = false;
        const message = err.error?.message || 'Credenciales incorrectas. Intenta de nuevo.';
        this.snackBar.open(message, 'Cerrar', { duration: 5000 });
      },
      complete: () => {
        this.isLoading = false;
      }
    });
  }
}