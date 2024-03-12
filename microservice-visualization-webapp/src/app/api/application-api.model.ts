export interface DependencyManagement {
    groupId: string,
    artifactId: string,
    version: string,
    dependencies: string[]
}

export interface ApplicationDto {
    id: number,
    name: string,
    label: string,
    description: string,
    location: string,
    owner: string,
    tags: Map<String, String>,
    dependencies: string[],
    dependencyManagements: DependencyManagement[]
}

export interface ApplicationLiteDto {
    id: number,
    name: string,
    label: string,
    description: string,
    type: ApplicationType,
    location: string,
    owners: string[],
    tags: any
}

export enum ApplicationType {
    MICROSERVICE = 'MICROSERVICE',
    LIBRARY = 'LIBRARY',
    BOM = 'BOM'
}
