import { Role } from "../../../core/models/role.model";

export interface AuthResponse {
    token: string;
    refreshToken?: string;
    id: number;
    username: string;
    authorities: string[];
    expirationDate: string;
    type: string;
    roles: string[]; // CRÍTICO: Cambiado de authorities: string[] a roles: Role[]
   enabled: boolean; // Para habilitar/deshabilitar el usuario
    empresaId: number;
    plan: string,
    logoUrl?: string
  }