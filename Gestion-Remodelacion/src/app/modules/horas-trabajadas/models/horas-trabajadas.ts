export interface HorasTrabajadas {

  id: number | null;
  idEmpleado: number;
  nombreEmpleado: string | null; // Para mostrar en el frontend, si el backend lo proporciona
  idProyecto: number;
  nombreProyecto: string | null; // Para mostrar en el frontend, si el backend lo proporciona
  fecha: string | Date; // Formato YYYY-MM-DD para backend, Date para frontend
  horas: number;
  actividadRealizada: string | null;
  fechaRegistro: string | Date | null; // Campo generado por el backend

}


