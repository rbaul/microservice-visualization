export interface ApplicationDto {
    id: number,
    name: string,
    label: string,
    description: string,
    owner: string,
    tags: Map<String, String>,
    dependencies: string[],
    managementDependencies: string[]
}

export interface ApplicationLiteDto {
    id: number,
    name: string,
    label: string,
    description: string,
    owner: string,
    tags: any
}