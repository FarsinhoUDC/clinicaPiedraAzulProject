export interface LoginRequest {
  numeroDocumento: string;
  contrasena: string;
}

export interface LoginResponse {
  id: number;
  numeroDocumento: string;
  nombres: string;
  apellidos: string;
  correo: string;
  rol: string;
  activo: boolean;
  celular?: string;
  genero?: string;
}