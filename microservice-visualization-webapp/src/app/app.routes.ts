import { Routes } from '@angular/router';
import { ProjectVersionsComponent } from './project-versions/project-versions.component';
import { ProjectViewComponent } from './project-view/project-view.component';
import { ProjectsComponent } from './projects/projects.component';

export const routes: Routes = [
  { path: 'projects', component: ProjectsComponent },
  { path: 'projects/:id/versions', component: ProjectVersionsComponent },
  { path: 'projects/:id', component: ProjectViewComponent },
  { path: '**', redirectTo: 'projects', pathMatch: 'full' }
];
