import { Injectable } from '@angular/core';
import { environment } from '../../../../environments/environment';
import { HttpClient, HttpParams } from '@angular/common/http';
import { map, Observable } from 'rxjs';
import { Page } from '../../../core/models/page.model';
import { User, UserRequest } from '../../../core/models/user.model';
import { BaseService } from '../../../core/services/base.service';
import { ApiResponse } from '../../../core/models/ApiResponse';

@Injectable({
  providedIn: 'root',
})
export class UserService extends BaseService<User> {
  constructor(http: HttpClient) {
    super(http, `${environment.apiUrl}/users`);
  }

  getUsers(
    page: number,
    size: number,
    filter: string
  ): Observable<Page<User>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    if (filter) {
      params = params.set('filter', filter);
    }

    return this.http.get<ApiResponse<Page<User>>>(this.apiUrl, { params }).pipe(
      map(response => this.extractPageData(response))
    );
  }

  getUserById(id: number): Observable<User> {
    return this.http.get<User>(`${this.apiUrl}/${id}`);
  }

  createUser(user: UserRequest): Observable<User> {
    return this.http.post<User>(this.apiUrl, user);
  }

  updateUser(id: number, user: UserRequest): Observable<User> {
    return this.http.put<User>(`${this.apiUrl}/${id}`, user);
  }

  deleteUser(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }  

}
