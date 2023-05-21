import { ProjectLiteDto } from './project-api.model';
export interface ProjectVersionDto {
    id: number,
    name: string,
    label?: string,
    description: string,
    owner?: string,
    mainProject: ProjectLiteDto
    projects: ProjectLiteDto[]
}

export interface ProjectVersionLiteDto {
    id?: number,
    name?: string,
    description?: string
}