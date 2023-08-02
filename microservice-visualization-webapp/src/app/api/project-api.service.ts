import { Page } from './../utils/page';
import { Observable } from 'rxjs';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { ProjectDto, ProjectLiteDto } from './project-api.model';


const API_URL = '/api/v1/projects';
const httpOptions = {
  headers: new HttpHeaders({
    'Content-Type': 'application/json'
  })
};

@Injectable({
  providedIn: 'root'
})
export class ProjectApiService {

  constructor(
    private http: HttpClient
  ) {
  }

  public get(id: number): Observable<ProjectDto> {
    return this.http.get<ProjectDto>(`${API_URL}/${id}`);
  }

  public delete(id: number): Observable<void> {
    return this.http.delete<void>(`${API_URL}/${id}`, httpOptions);
  }

  public getPageable(projectVersionId: number): Observable<Page<ProjectLiteDto>> {
    return this.http.get<Page<ProjectLiteDto>>(`${API_URL}/search`,
      { params: new HttpParams().set('projectVersionId', projectVersionId) });
  }

}
