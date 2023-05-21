import { Component, OnInit, OnDestroy } from '@angular/core';
import { DialogService, DynamicDialogRef } from 'primeng/dynamicdialog';
import { ProjectApiService } from '../api/project-api.service';
import { ActivatedRoute } from '@angular/router';
import { ProjectDto } from '../api/project-api.model';
import { ApplicationLiteDto } from '../api/application-api.model';
import { ApplicationViewComponent } from '../application-view/application-view.component';
import { Table } from 'primeng/table';

@Component({
  selector: 'app-project-view',
  templateUrl: './project-view.component.html',
  styleUrls: ['./project-view.component.scss'],
  providers: [DialogService]
})
export class ProjectViewComponent implements OnInit, OnDestroy {

  data: ProjectDto = {
    id: 0,
    applications: [],
    projectVersion: {}
  };

  tags: string[] = ['java', 'Spring Boot', 'Spring Cloud', 'gradle'];

  globalFilterFields: string[] = [];

  toggleOptions: ToggleView[] = [
    { icon: 'pi pi-table', value: false },
    { icon: 'pi pi-share-alt', value: true }
  ];
  toggle: ToggleView = { icon: 'pi pi-table', value: false };

  visible: boolean = false;

  ref: DynamicDialogRef | undefined;
  selectedApplication: any;
  sidebarVisible: boolean = false;

  constructor(
    private activatedRoute: ActivatedRoute,
    public dialogService: DialogService,
    private projectApi: ProjectApiService
  ) { }

  ngOnInit(): void {
    this.globalFilterFields = this.tags.map(tag => `tags.${tag}`);
    this.globalFilterFields.push('name');
    this.activatedRoute.params.subscribe(value => {
      let projectId: number = value['id'];
      if (projectId) {
        this.projectApi.get(projectId).subscribe(result => {
          this.data = result;
        });
      }
    });
  }

  ngOnDestroy() {
    if (this.ref) {
      this.ref.close();
    }
  }

  showApplication(application: ApplicationLiteDto) {
    this.ref = this.dialogService.open(ApplicationViewComponent, {
      header: `Application: ${application.name}`,
      width: '70%',
      contentStyle: { overflow: 'auto' },
      baseZIndex: 10000,
      maximizable: true,
      data: {
        applicationId: application.id
      }
    });
  }

  onRowSelect($event: any) {
    console.log(`Row selected ${JSON.stringify($event)}`)
  }

  onRowUnselect($event: any) {
    console.log(`Row unselected ${JSON.stringify($event)}`)
  }

  // customSort(event: any) {
  //   event.data.sort((data1: ApplicationLiteDto, data2: ApplicationLiteDto) => {
  //     let value1 = data1.tags[event.field];
  //     let value2 = data2.tags[event.field];
  //     let result = null;

  //     if (value1 == null && value2 != null) result = -1;
  //     else if (value1 != null && value2 == null) result = 1;
  //     else if (value1 == null && value2 == null) result = 0;
  //     else if (typeof value1 === 'string' && typeof value2 === 'string') result = value1.localeCompare(value2);
  //     else result = value1 < value2 ? -1 : value1 > value2 ? 1 : 0;

  //     return event.order * result;
  //   });
  // }

}

interface ToggleView {
  value: boolean,
  icon: string
}