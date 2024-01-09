import { CommonModule } from '@angular/common';
import { Component, OnDestroy, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { ButtonModule } from 'primeng/button';
import { DialogModule } from 'primeng/dialog';
import { DialogService, DynamicDialogRef } from 'primeng/dynamicdialog';
import { InputTextModule } from 'primeng/inputtext';
import { SelectButtonModule } from 'primeng/selectbutton';
import { SidebarModule } from 'primeng/sidebar';
import { Table, TableModule } from 'primeng/table';
import { TagModule } from 'primeng/tag';
import { ToolbarModule } from 'primeng/toolbar';
import { ApplicationLiteDto, ApplicationType } from '../api/application-api.model';
import { ProjectDto } from '../api/project-api.model';
import { ProjectApiService } from '../api/project-api.service';
import { ApplicationViewComponent } from '../application-view/application-view.component';
import { ProjectTopologyComponent, TopologyType } from '../project-topology/project-topology.component';

import { get } from 'lodash';
import { DropdownModule } from 'primeng/dropdown';

@Component({
  selector: 'app-project-view',
  standalone: true,
  imports: [
    CommonModule,
    TableModule,
    ToolbarModule,
    SelectButtonModule,
    FormsModule,
    SidebarModule,
    ProjectTopologyComponent,
    DialogModule,
    InputTextModule,
    ButtonModule,
    TagModule,
    DropdownModule
  ],
  templateUrl: './project-view.component.html',
  styleUrls: ['./project-view.component.scss'],
  providers: [DialogService]
})
export class ProjectViewComponent implements OnInit, OnDestroy {

  private activatedRoute = inject(ActivatedRoute);
  public dialogService = inject(DialogService);
  private projectApi = inject(ProjectApiService);

  public TopologyTypeEnum = TopologyType;

  data: ProjectDto = {
    id: 0,
    applications: [],
    projectVersion: {}
  };

  types: AppType[] = [
    {label: 'Library', value: ApplicationType.LIBRARY},
    {label: 'Microservice', value: ApplicationType.MICROSERVICE}  
  ];

  tags: string[] = ['java', 'Spring Boot', 'Spring Cloud', 'gradle'];

  globalFilterFields: string[] = [];

  toggleOptions: ToggleView[] = [
    { name: 'Table view', value: 1 },
    { name: 'Microservices view', value: 2 },
    { name: 'Dependencies view', value: 3 }
  ];
  toggleValue: number = 1;

  visible: boolean = false;

  ref: DynamicDialogRef | undefined;
  selectedApplication: any;
  sidebarVisible: boolean = false;

  ngOnInit(): void {
    // this.globalFilterFields = this.tags.map(tag => `tags.${tag}`);
    this.globalFilterFields.push('name');
    // this.globalFilterFields.push('type');
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

  isLibrary(appType: ApplicationType): boolean {
    return appType === ApplicationType.LIBRARY;
  }

  clear(table: Table) {
    table.clear();
  }

  customSort(event: any) {
    event.data.sort((data1: any, data2: any) => {
      let value1 = get(data1, event.field);
      let value2 = get(data2, event.field);
      let result = null;

      if (value1 == null && value2 != null) result = -1;
      else if (value1 != null && value2 == null) result = 1;
      else if (value1 == null && value2 == null) result = 0;
      else if (typeof value1 === 'string' && typeof value2 === 'string') {
        result = value1.localeCompare(value2, undefined, { numeric: true });
      }
      else result = value1 < value2 ? -1 : value1 > value2 ? 1 : 0;

      return event.order * result;
    });
  }

}

interface ToggleView {
  value: number,
  name: string
}

interface AppType {
  value: ApplicationType,
  label: string
}
