export interface MenuItem {
  label: string; // Nombre del elemento del menú
  icon: string;  // Ícono de Angular Material
  route: string; // Ruta a la que redirige
  permission?: string; // Permiso requerido para ver el elemento

}