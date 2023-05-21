import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiUtils } from './api-utils';
import { Page } from './page';

export abstract class PageableApiService<T> {

    protected constructor(
        protected http: HttpClient,
        public apiUrl: string
    ) {
    }

    search(pageSize: number, pageNumber: number,
        sort: string[], filter: string): Observable<Page<T>> {
        const httpParams = ApiUtils.getPageableHttpParams(pageSize, pageNumber, sort, filter);
        return this.http.get<Page<T>>(this.apiUrl + '/search', { params: httpParams });
    }
}
