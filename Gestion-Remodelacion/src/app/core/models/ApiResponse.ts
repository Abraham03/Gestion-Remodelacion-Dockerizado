// src/app/core/models/api-response.model.ts
export interface ApiResponse<T> {
  status: number;
  message: string;
  data: T; // Contiene la Page<T> o T, o T[]
  timestamp?: string;
}