export interface Cliente {
  id: number | null;
  nombreCliente: string; // Asegúrate de que este campo coincida con el nombre del cliente en tu backend
  telefonoContacto: string;
  direccion?: string;
  notas?: string;
  fechaRegistro?: string; // Opcional
}