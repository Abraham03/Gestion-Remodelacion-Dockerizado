import { Role } from './role.model';

export interface User {
  id: number;
  username: string;
  email: string;
  enabled: boolean; 
  accountNonExpired: boolean;
  accountNonLocked: boolean; 
  credentialsNonExpired: boolean; 
  roles: Role[]; 
  token: string; // El access token actual
  refreshToken?: string; // Refresh token para renovar la sesión
  expirationDate?: string; // Fecha de expiración del token (útil para lógica de renovación)
  type?: string; // Tipo de token, usualmente "Bearer"
  authorities: string[];
  empresaId: number;
  plan: string;
  logoUrl?: string;
  nombreEmpresa?: string;
}

// Para la petición de creación/actualización, podemos enviar solo nombres de roles
export interface UserRequest {
  id?: number | null; 
  username: string;
  password?: string; 
  email: string;
  roleIds: number[]; 
  enabled: boolean; 
  empresaId?: number;
  empleadoId?: number | null;
}
