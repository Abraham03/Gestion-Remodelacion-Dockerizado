export interface HorasTrabajadas {

  id: number;
  idEmpleado: number;
  idProyecto: number;
  fecha: string | Date | null; 
  horas: number;
  costoPorHoraActual: number; // Calculado por el backend  
  montoTotal: number;
  actividadRealizada: string | null;

  cantidad: number;
  unidad: string;
  nombreEmpleado: string; 
  nombreProyecto: string; 
  fechaRegistro: string | Date | null; 

}

// (Lo que el formulario envía al backend)
export interface HorasTrabajadasRequest {
  id?: number; // Opcional, solo para actualizar
  idEmpleado: number;
  idProyecto: number;
  fecha: string | null; // Formato YYYY-MM-DD
  actividadRealizada: string | null;
  
  // --- DATOS "TONTOS" ---
  cantidad: number; // El valor del input (ej: 1)
  unidad: string; // "dias" o "horas"
}


