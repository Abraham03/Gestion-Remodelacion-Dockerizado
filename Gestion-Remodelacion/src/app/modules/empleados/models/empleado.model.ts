export interface Empleado {
  id?: number;
  nombreCompleto: string;
  rolCargo: string;
  telefonoContacto?: string;
  // Allow fechaContratacion to be either a string (from backend) or Date (for form/datepicker)
  fechaContratacion?: string | Date;
  costoPorHora: number;
  modeloDePago: string;
  activo: boolean;
  notas?: string;
  fechaRegistro?: Date;
}