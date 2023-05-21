import { Observable } from 'rxjs';
import { PageableDataSource } from "../utils/pageable.datasource";
import { ProjectVersionDto } from '../api/project-version-api.model';
import { Page } from '../utils/page';
import { ProjectVersionApiService } from '../api/project-version-api.service';


/**
 * Data source for the Decisions view. This class should
 * encapsulate all logic for fetching and manipulating the displayed data
 * (including sorting, pagination, and filtering).
 */
export class ProjectsDataSource extends PageableDataSource<ProjectVersionDto> {
  constructor(
    private projectVersionApi: ProjectVersionApiService
  ) {
    super();
  }

  getPageableContent(pageSize: number, pageIndex: number, sort: string[], filter: string): Observable<Page<ProjectVersionDto>> {
    return this.projectVersionApi.search(pageSize, pageIndex, [], '');
  }

}
