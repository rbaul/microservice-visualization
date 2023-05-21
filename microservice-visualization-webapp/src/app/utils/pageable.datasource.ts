import { CollectionViewer, DataSource } from '@angular/cdk/collections';
import { BehaviorSubject, Observable, of } from 'rxjs';
import { catchError, finalize } from 'rxjs/operators';
import { Page } from './page';


export abstract class PageableDataSource<T> extends DataSource<T> {

  public contentSubject = new BehaviorSubject<T[]>([]);

  private loadingSubject = new BehaviorSubject<boolean>(false);

  private totalElementsSubject = new BehaviorSubject<number>(0);

  public loading$ = this.loadingSubject.asObservable();

  public totalElements$ = this.totalElementsSubject.asObservable();

  loadContent(pageSize = 10, pageIndex = 0, sort: string[] = [], filter = '') {
    this.loadingSubject.next(true);
    this.totalElementsSubject.next(0);

    this.getPageableContent(pageSize, pageIndex, sort, filter)
      .pipe(
        catchError(() => of(new Page<T>())),
        finalize(() => this.loadingSubject.next(false))
      )
      .subscribe(data => {
        this.totalElementsSubject.next(data.totalElements);
        this.contentSubject.next(data.content);
      });
  }

  getData(): T[] {
    return this.contentSubject.getValue();
  }

  abstract getPageableContent(pageSize: number, pageIndex: number, sort: string[], filter: string): Observable<Page<T>>;

  connect(collectionViewer: CollectionViewer | undefined = undefined): Observable<T[]> {
    return this.contentSubject.asObservable();
  }

  disconnect(collectionViewer: CollectionViewer | undefined = undefined): void {
    this.contentSubject.complete();
    this.loadingSubject.complete();
    this.totalElementsSubject.complete();
  }

}
