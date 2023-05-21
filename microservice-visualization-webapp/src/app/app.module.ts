import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { ProjectsComponent } from './projects/projects.component';
import { ProjectViewComponent } from './project-view/project-view.component';
import { PrimengModule } from './shared/primeng.module';
import { HttpClientModule } from '@angular/common/http';
import { ApplicationViewComponent } from './application-view/application-view.component';
import { ProjectTopologyComponent } from './project-topology/project-topology.component';

@NgModule({
  declarations: [
    AppComponent,
    ProjectsComponent,
    ProjectViewComponent,
    ApplicationViewComponent,
    ProjectTopologyComponent
  ],
  imports: [
    BrowserModule,
    BrowserAnimationsModule,
    FormsModule,
    HttpClientModule,
    AppRoutingModule,
    PrimengModule
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule { }
