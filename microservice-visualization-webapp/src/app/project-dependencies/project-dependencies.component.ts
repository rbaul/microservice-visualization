import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { Table, TableModule } from 'primeng/table';
import { ToolbarModule } from 'primeng/toolbar';
import { ProjectApiService } from '../api/project-api.service';
import { TagModule } from 'primeng/tag';
import { ProjectDependenciesDto } from '../api/project-api.model';
import { SelectButtonChangeEvent, SelectButtonModule } from 'primeng/selectbutton';
import { ToggleView } from '../project-view/project-view.component';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-project-dependencies',
  standalone: true,
  imports: [
    CommonModule,
    ToolbarModule,
    TableModule,
    InputTextModule,
    ButtonModule,
    TagModule,
    SelectButtonModule,
    FormsModule
  ],
  templateUrl: './project-dependencies.component.html',
  styleUrl: './project-dependencies.component.scss'
})
export class ProjectDependenciesComponent {
  private activatedRoute = inject(ActivatedRoute);
  private router = inject(Router);
  private projectApiService = inject(ProjectApiService);

  private projectId!: number;

  toggleOptions: ToggleView[] = [
    { name: 'Direct', value: 1 },
    { name: 'Full', value: 2 },
    { name: 'Implicit', value: 3 }
  ];

  toggleSelect: number = 1;

  data: ProjectDependenciesDto = {
    id: 0,
    version: '0',
    dependencies: [],
    projectVersion: {
      name: ''
    }
  };

  globalFilterFields: string[] = ['packageName', 'artifactName'];

  ngOnInit() {
    this.activatedRoute.params.subscribe(value => {
      this.projectId = value['id'];
      if (this.projectId) {
        this.filterChange(1);
      }
    });
  }

  openProject(): void {
    this.router.navigate([`projects/${this.data.id}`]);
  }

  clear(table: Table) {
    table.clear();
  }

  filterChange(value: number): void {
    switch(value) {
      case 1: {
        this.projectApiService.getDirectDependencies(this.projectId).subscribe(result => {
          this.data = result;
        });
        break;
      }
      case 2: {
        this.projectApiService.getDependencies(this.projectId).subscribe(result => {
          this.data = result;
        });
        break;
      }
      case 3: {
        this.projectApiService.getImplicitDependencies(this.projectId).subscribe(result => {
          this.data = result;
        });
        break;
      }
      default: {
        
      }
    }
  }

  customSort(event: any) {
    event.data.sort((data1: any, data2: any) => {
      let value1 = data1[event.field];
      let value2 = data2[event.field];
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
