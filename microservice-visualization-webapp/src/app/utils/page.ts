export class Page<T> {
    content: T[] = [];
    empty: boolean | undefined;
    first: boolean | undefined;
    last: boolean | undefined;
    number: number | undefined;
    numberOfElements: number | undefined;
    pageable: any | undefined;
    size: number | undefined;
    sort: any | undefined;
    totalElements: number = 0;
    totalPages: number = 0;
}
