import { CommonModule } from '@angular/common';
import { Component, OnDestroy, OnInit, inject } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { TableModule } from 'primeng/table';
import { TagModule } from 'primeng/tag';
import { ToolbarModule } from 'primeng/toolbar';
import { ProjectLiteDto } from '../api/project-api.model';
import { ProjectApiService } from './../api/project-api.service';
import { ProjectVersionDto } from './../api/project-version-api.model';
import { ProjectVersionApiService } from './../api/project-version-api.service';

@Component({
  selector: 'app-project-versions',
  standalone: true,
  imports: [
    CommonModule,
    ToolbarModule,
    TableModule,
    InputTextModule,
    ButtonModule,
    TagModule
  ],
  templateUrl: './project-versions.component.html',
  styleUrls: ['./project-versions.component.css']
})
export class ProjectVersionsComponent implements OnInit {

  private activatedRoute = inject(ActivatedRoute);
  private router = inject(Router);
  private projectApi = inject(ProjectApiService);
  private projectVersionApi = inject(ProjectVersionApiService);

  data: ProjectVersionDto = new ProjectVersionDto();

  globalFilterFields: string[] = ['version'];

  ngOnInit() {
    this.activatedRoute.params.subscribe(value => {
      let projectId: number = value['id'];
      if (projectId) {
        this.projectVersionApi.get(projectId).subscribe(result => {
          this.data = result;
        });
        // this.projectApi.getPageable(projectId).subscribe(result => {
        //   this.data = result;
        // });
      }
    });
  }

  isDefaultVersion(project: ProjectLiteDto): boolean {
    return this.data.mainProject.id === project.id;
  }

  openVersion(project: ProjectLiteDto) {
    this.router.navigate([`projects/${project.id}`]);
  }

  showProjectDependencies(project: ProjectLiteDto) {
    this.router.navigate([`projects/${project.id}/dependencies`]);
  }

}
