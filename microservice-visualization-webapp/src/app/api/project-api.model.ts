import { ApplicationLiteDto } from "./application-api.model"
import { ProjectVersionLiteDto } from "./project-version-api.model"

export interface ApplicationGroupDto {
    name: string,
    description: string,
    applicationNames: string[]
}

export interface OwnerDto {
    name: string,
    description: string,
    applicationNames: string[]
}

export interface ProjectDto {
    id?: number,
    version?: string,
    applications: ApplicationLiteDto[],
    groups?: ApplicationGroupDto[],
    owners?: OwnerDto[],
    tags?: string[],
    // connections?: Map<String, String[]>,
    connections?: any,
    dependencies?: any,
    librariesCycle?: [string[]],
    projectVersion: ProjectVersionLiteDto
}

export interface ProjectLiteDto {
    id: number,
    version: string
}

export interface DependencyDto {
    packageName: string,
    artifactName: string,
    version: string,
    usageOf: string[]
}

export interface ProjectDependenciesDto extends ProjectLiteDto {
    dependencies: DependencyDto[],
    projectVersion: ProjectVersionLiteDto
}