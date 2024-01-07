import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { httpJsonOptions } from '../utils/api-utils';
import { Page } from '../utils/page';
import { PageableApiService } from '../utils/pageable-api-service';
import { ProjectVersionDto } from './project-version-api.model';

const API_URL = '/api/v1/project-versions';

@Injectable({
  providedIn: 'root'
})
export class ProjectVersionApiService extends PageableApiService<ProjectVersionDto> {

  constructor(
  ) {
    super(API_URL);
  }

  public get(id: number): Observable<ProjectVersionDto> {
    return this.http.get<ProjectVersionDto>(`${API_URL}/${id}`, httpJsonOptions);
  }

  public delete(id: number): Observable<void> {
    return this.http.delete<void>(`${API_URL}/${id}`, httpJsonOptions);
  }

  public getPageable(): Observable<Page<ProjectVersionDto>> {
    return this.http.get<Page<ProjectVersionDto>>(`${API_URL}/search`, httpJsonOptions);
  }

}
