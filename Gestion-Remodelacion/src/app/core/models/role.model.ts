import { Permission } from "./permission.model";

export interface Role {
  id: number;
  name: string; // Ej: 'ROLE_ADMIN', 'ROLE_USER'
  description: string;
  permissions: Permission[]; // Lista de permisos asociados a este rol
}

// Para la petición de creación/actualización de un rol al backend
export interface RoleRequest {
  id?: number | null; // Opcional para creación, requerido para actualización
  name: string;
  description: string;
  permissions: number[]; // Enviamos IDs de permisos al backend
}

export interface RoleDropdownResponse {
  id: number;
  name: string;
}