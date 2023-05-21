import { Component, OnInit } from '@angular/core';
import { ProjectVersionApiService } from '../api/project-version-api.service';
import { ProjectsDataSource } from './projects-datasource';
import { ProjectVersionDto } from '../api/project-version-api.model';
import { Router } from '@angular/router';

@Component({
  selector: 'app-projects',
  templateUrl: './projects.component.html',
  styleUrls: ['./projects.component.scss']
})
export class ProjectsComponent implements OnInit {

  public projects: ProjectVersionDto[] = [];

  dataSource: ProjectsDataSource;

  constructor(
    projectVersionApi: ProjectVersionApiService,
    private router: Router
  ) {
    this.dataSource = new ProjectsDataSource(projectVersionApi);
  }

  ngOnInit(): void {
    this.dataSource.loadContent();
    this.dataSource.contentSubject.subscribe((data) => {
      this.projects = data;
    });

  }

  selectProjectVersion(project: ProjectVersionDto) {
    this.router.navigate([`projects/${project.id}`]);
  }


}
