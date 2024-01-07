import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { DynamicDialogConfig, DynamicDialogRef } from 'primeng/dynamicdialog';
import { ApplicationApiService } from '../api/application-api.service';

@Component({
  selector: 'app-application-view',
  standalone: true,
  imports: [
    CommonModule
  ],
  templateUrl: './application-view.component.html',
  styleUrls: ['./application-view.component.scss']
})
export class ApplicationViewComponent implements OnInit {

  public ref = inject(DynamicDialogRef);
  public config = inject(DynamicDialogConfig);
  private applicationApi = inject(ApplicationApiService);

  data: any;

  ngOnInit() {
    this.applicationApi.get(this.config.data.applicationId).subscribe(result => {
      this.data = result;
    });
  }

}
