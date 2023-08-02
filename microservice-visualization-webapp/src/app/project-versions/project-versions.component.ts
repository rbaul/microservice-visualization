import { ProjectVersionDto } from './../api/project-version-api.model';
import { ProjectVersionApiService } from './../api/project-version-api.service';
import { ActivatedRoute, Router } from '@angular/router';
import { ProjectApiService } from './../api/project-api.service';
import { Component, OnInit } from '@angular/core';
import { ProjectLiteDto } from '../api/project-api.model';

@Component({
  selector: 'app-project-versions',
  templateUrl: './project-versions.component.html',
  styleUrls: ['./project-versions.component.css']
})
export class ProjectVersionsComponent implements OnInit {

  data: ProjectVersionDto = new ProjectVersionDto();

  globalFilterFields: string[] = ['version'];

  constructor(
    private activatedRoute: ActivatedRoute,
    private router: Router,
    private projectApi: ProjectApiService,
    private projectVersionApi: ProjectVersionApiService
  ) { }

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

}
