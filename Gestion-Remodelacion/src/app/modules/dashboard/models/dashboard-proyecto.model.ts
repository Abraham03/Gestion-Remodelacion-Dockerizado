// src/app/modules/dashboard/models/dashboard-proyecto.model.ts
export interface DashboardProyecto {
  id: number | null;
  nombre: string; // Mapeado de nombreProyecto
  descripcion: string;
  fechaInicio: string;
  fechaFinEstimada: string; // Mapeado de fechaFinEstimada
  estado: string; // Mapeado de estado original, luego a estadoDisplay
  progresoPorcentaje: number | null; // <-- CAMBIADO A number | null
  estadoDisplay: string; // Para mostrar el estado legible
  // Puedes añadir más propiedades si las necesitas en el dashboard
  // como nombreCliente, nombreEmpleadoResponsable, etc.
}