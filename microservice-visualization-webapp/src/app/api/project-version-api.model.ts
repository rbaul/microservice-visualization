import { ProjectLiteDto } from './project-api.model';

export interface ProjectVersionLiteDto {
    id?: number,
    name?: string,
    description?: string
}

export class ProjectVersionDto {
    id!: number;
    name!: string;
    label!: string;
    description!: string;
    owner!: string;
    mainProject!: ProjectLiteDto;
    projects!: ProjectLiteDto[];
}
