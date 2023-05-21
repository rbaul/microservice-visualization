import { Observable } from 'rxjs';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { PageableApiService } from '../utils/pageable-api-service';
import { ProjectVersionDto } from './project-version-api.model';
import { Page } from '../utils/page';

const API_URL = '/api/v1/project-versions';
const httpOptions = {
  headers: new HttpHeaders({
    'Content-Type': 'application/json'
  })
};

@Injectable({
  providedIn: 'root'
})
export class ProjectVersionApiService extends PageableApiService<ProjectVersionDto> {

  constructor(
    http: HttpClient
  ) {
    super(http, API_URL);
  }

  public get(id: number): Observable<ProjectVersionDto> {
    return this.http.get<ProjectVersionDto>(`${API_URL}/${id}`);
  }

  public delete(id: number): Observable<void> {
    return this.http.delete<void>(`${API_URL}/${id}`, httpOptions);
  }

  public getPageable(): Observable<Page<ProjectVersionDto>> {
    return this.http.get<Page<ProjectVersionDto>>(`${API_URL}/search`);
  }
}
