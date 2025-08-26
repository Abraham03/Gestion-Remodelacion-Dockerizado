import { Role } from './role.model';

export interface User {
  id: number;
  username: string;
  enabled: boolean; // Nuevo campo
  accountNonExpired: boolean; // Nuevo campo
  accountNonLocked: boolean; // Nuevo campo
  credentialsNonExpired: boolean; // Nuevo campo
  roles: Role[]; // CRÍTICO: Cambiado de authorities: string[] a roles: Role[]
  token: string; // El access token actual
  refreshToken?: string; // Refresh token para renovar la sesión
  expirationDate?: string; // Fecha de expiración del token (útil para lógica de renovación)
  type?: string; // Tipo de token, usualmente "Bearer"
  authorities: string[]; // Si tu backend aún envía una lista plana de strings de autoridades, inclúyela aquí.
}

// Para la petición de creación/actualización, podemos enviar solo nombres de roles
export interface UserRequest {
  id?: number | null; // Opcional para creación, requerido para actualización
  username: string;
  password?: string; // Requerido para creación, opcional/null para actualización si no se cambia
  roleIds: number[]; // Enviamos IDs de roles al backend
  enabled: boolean; // Para habilitar/deshabilitar el usuario
}
