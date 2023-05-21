export interface ErrorDto {
    errorCode: string;
    message: string;
    timestamp: Date;
    errors: any[];
}

export interface ValidationErrorDto {
    fieldName: string;
    errorCode: string;
    rejectedValue: string;
    params: any[];
    message: string;
}
