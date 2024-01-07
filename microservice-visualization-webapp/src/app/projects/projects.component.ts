import { Component, OnInit, inject } from '@angular/core';
import { ProjectVersionApiService } from '../api/project-version-api.service';
import { ProjectsDataSource } from './projects-datasource';
import { ProjectVersionDto } from '../api/project-version-api.model';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { TableModule } from 'primeng/table';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';

@Component({
  selector: 'app-projects',
  standalone: true,
  imports: [
    CommonModule,
    TableModule,
    ButtonModule
  ],
  templateUrl: './projects.component.html',
  styleUrls: ['./projects.component.scss']
})
export class ProjectsComponent implements OnInit {

  projectVersionApi = inject(ProjectVersionApiService);
  router = inject(Router);

  public projects: ProjectVersionDto[] = [];

  dataSource: ProjectsDataSource;

  constructor() {
    this.dataSource = new ProjectsDataSource(this.projectVersionApi);
  }

  ngOnInit(): void {
    this.dataSource.loadContent();
    this.dataSource.contentSubject.subscribe((data) => {
      this.projects = data;
    });

  }

  selectProjectVersion(project: ProjectVersionDto) {
    this.router.navigate([`projects/${project.id}/versions`]);
  }

  openDefaultVersion(project: ProjectVersionDto) {
    this.router.navigate([`projects/${project.mainProject.id}`]);
  }

}
