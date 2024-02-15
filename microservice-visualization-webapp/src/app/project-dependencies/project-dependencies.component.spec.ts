import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ProjectDependenciesComponent } from './project-dependencies.component';

describe('ProjectDependenciesComponent', () => {
  let component: ProjectDependenciesComponent;
  let fixture: ComponentFixture<ProjectDependenciesComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ProjectDependenciesComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(ProjectDependenciesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
