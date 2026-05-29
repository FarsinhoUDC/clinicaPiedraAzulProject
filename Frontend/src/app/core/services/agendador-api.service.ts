import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';

import { ApiResponse } from '../api/api-response.model';
import { environment } from '../../../environments/environment';

export interface AgendadorRequest {
  numeroDocumento: string;
  nombres: string;
  apellidos: string;
  correo?: string | null;
  celular?: string | null;
  genero?: string | null;
  fechaNacimiento?: string | null;
}

export interface AgendadorResponse {
  id: number;
  numeroDocumento: string;
  nombres: string;
  apellidos: string;
  correo?: string | null;
  celular?: string | null;
  genero?: string | null;
  fechaNacimiento?: string | null;
}

@Injectable({ providedIn: 'root' })
export class AgendadorApiService {
  constructor(private readonly http: HttpClient) {}

  create(data: AgendadorRequest): Observable<AgendadorResponse> {
    return this.http.post<ApiResponse<AgendadorResponse>>(`${environment.apiBaseUrl}/agendadores`, data).pipe(
      map((response) => {
        if (!response.data) throw new Error('No se recibió la data del agendador');
        return response.data;
      })
    );
  }
}
