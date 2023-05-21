import { Observable } from 'rxjs';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { ApplicationDto } from './application-api.model';

const API_URL = '/api/v1/applications';
const httpOptions = {
  headers: new HttpHeaders({
    'Content-Type': 'application/json'
  })
};

@Injectable({
  providedIn: 'root'
})
export class ApplicationApiService {

  constructor(
    private http: HttpClient
  ) { }

  public get(id: number): Observable<ApplicationDto> {
    return this.http.get<ApplicationDto>(`${API_URL}/${id}`);
  }
}
