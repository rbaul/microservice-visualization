import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { httpJsonOptions } from '../utils/api-utils';
import { Page } from './../utils/page';
import { ProjectDto, ProjectLiteDto } from './project-api.model';

const API_URL = '/api/v1/projects';

@Injectable({
  providedIn: 'root'
})
export class ProjectApiService {

  http = inject(HttpClient);

  public get(id: number): Observable<ProjectDto> {
    return this.http.get<ProjectDto>(`${API_URL}/${id}`, httpJsonOptions);
  }

  public delete(id: number): Observable<void> {
    return this.http.delete<void>(`${API_URL}/${id}`, httpJsonOptions);
  }

  public getPageable(projectVersionId: number): Observable<Page<ProjectLiteDto>> {
    return this.http.get<Page<ProjectLiteDto>>(`${API_URL}/search`,
      { params: new HttpParams().set('projectVersionId', projectVersionId) });
  }

}
