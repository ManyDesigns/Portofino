import {ChangeDetectorRef, Component, OnInit} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {PortofinoService} from "../../portofino.service";
import {isInsertable, Property} from "../../class-accessor";
import {FormControl} from "@angular/forms";
import {BaseDetailComponent} from "../common.component";
import {MatSnackBar} from "@angular/material";
import {NotificationService} from "../../notifications/notification.service";

@Component({
  selector: 'portofino-crud-create',
  templateUrl: './create.component.html',
  styleUrls: ['./create.component.css']
})
export class CreateComponent extends BaseDetailComponent implements OnInit {

  constructor(
    protected http: HttpClient, protected portofino: PortofinoService,
    protected changeDetector: ChangeDetectorRef, protected notificationService: NotificationService) {
    super(http, portofino, changeDetector, notificationService);
  }

  isEditable(property: Property): boolean {
    return isInsertable(property);
  }

  isEditEnabled(): boolean {
    return true;
  }

  ngOnInit() {
    this.initClassAccessor();
    this.http.get(this.sourceUrl, {params: {newObject: "true"}}).subscribe(o => this.setupForm(o));
  }

  cancel() {
    this.close.emit();
  }

  doSave(object) {
    return this.http.post(this.sourceUrl, object);
  }

}
