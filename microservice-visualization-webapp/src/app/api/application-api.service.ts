import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { httpJsonOptions } from '../utils/api-utils';
import { ApplicationDto } from './application-api.model';

const API_URL = '/api/v1/applications';

@Injectable({
  providedIn: 'root'
})
export class ApplicationApiService {
  http = inject(HttpClient);

  public get(id: number): Observable<ApplicationDto> {
    return this.http.get<ApplicationDto>(`${API_URL}/${id}`, httpJsonOptions);
  }
}
