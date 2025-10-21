import { DatePipe } from "@angular/common";

export interface Proyecto {
  id: number; // Changed to number | null for id, as it's auto-generated on creation
  idCliente: number;
  nombreCliente: string | null; // Added, can be null if not explicitly loaded in the response
  nombreProyecto: string;
  descripcion: string;
  direccionPropiedad: string; // Added
  estado: 'CANCELADO' | 'EN_PAUSA' | 'EN_PROGRESO' | 'FINALIZADO' | 'PENDIENTE';
  fechaInicio: string | Date; // Se espera en formato 'YYYY-MM-DD' o Date si se convierte
  fechaFinEstimada: string | Date; // Se espera en formato 'YYYY-MM-DD' o Date si se convierte
  fechaFinalizacionReal: string | Date | null; // Añadido, puede ser null
  idEmpleadoResponsable: number | null; // Añadido, puede ser null
  nombreEmpleadoResponsable: string | null; // Añadido, puede ser null
  montoContrato: number; // BigDecimal en Java se mapea a number en TypeScript
  montoRecibido: number | null; // Añadido, puede ser null
  fechaUltimoPagoRecibido: string | Date | null; // Añadido, puede ser null
  costoMaterialesConsolidado: number | null; // Añadido, puede ser null
  otrosGastosDirectosConsolidado: number | null; // Añadido, puede ser null
  costoManoDeObra: number | null;
  progresoPorcentaje: number | null; // Añadido
  notasProyecto: string | null; // Añadido
  fechaCreacion: string | Date | null; // Changed to 'string | null' as it's backend-managed on creation
}