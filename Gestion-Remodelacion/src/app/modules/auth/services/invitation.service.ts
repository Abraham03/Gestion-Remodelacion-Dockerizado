import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { ApiResponse } from '../../../core/models/ApiResponse';
import { BaseService } from '../../../core/services/base.service';

export interface InvitationDetails {
  email: string;
}

@Injectable({
  providedIn: 'root'
})
export class InvitationService extends BaseService<InvitationDetails> {

  constructor(http: HttpClient) {
    super(http, `${environment.apiUrl}/invitations`);
  }

  validateToken(token: string): Observable<InvitationDetails> {
    const params = new HttpParams().set('token', token);
    
    // 4. Simplifica el método usando la lógica heredada
    return this.http.get<ApiResponse<InvitationDetails>>(`${this.apiUrl}/validate`, { params })
      .pipe(
        map(response => this.extractSingleData(response))
      );
  }
}