// src/app/modules/empresas/models/empresa.model.ts

export interface Empresa {
  id: number | null;
  nombreEmpresa: string;
  activo: boolean;
  plan: 'BASICO' | 'NEGOCIOS' | 'PROFESIONAL';
  estadoSuscripcion: 'ACTIVA' | 'CANCELADA' | 'VENCIDA';
  fechaInicioSuscripcion: string | Date | null;
  fechaFinSuscripcion: string | Date | null;
  logoUrl: string | null;
  fechaCreacion?: string | Date;
  telefono?: string | null;
}

export interface EmpresaDropdown {
  id: number;
  nombreEmpresa: string;
}