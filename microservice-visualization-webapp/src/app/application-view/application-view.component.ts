import { Component, OnInit } from '@angular/core';
import { DynamicDialogRef, DynamicDialogConfig } from 'primeng/dynamicdialog';
import { ApplicationApiService } from '../api/application-api.service';

@Component({
  selector: 'app-application-view',
  templateUrl: './application-view.component.html',
  styleUrls: ['./application-view.component.scss']
})
export class ApplicationViewComponent implements OnInit {

  data: any;

  constructor(
    public ref: DynamicDialogRef,
    public config: DynamicDialogConfig,
    private applicationApi: ApplicationApiService
  ) { }

  ngOnInit() {
    this.applicationApi.get(this.config.data.applicationId).subscribe(result => {
      this.data = result;
    });
  }

}
