import {ChangeDetectorRef, Component, OnInit} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {PortofinoService} from "../../../portofino.service";
import {isInsertable, Property} from "../../../class-accessor";
import {BaseDetailComponent} from "../common.component";
import {NotificationService} from "../../../notifications/notification.services";
import {Button} from "../../../buttons";
import {TranslateService} from "@ngx-translate/core";

@Component({
  selector: 'portofino-crud-create',
  templateUrl: '../../../../../assets/pages/crud/detail/create.component.html',
  styleUrls: ['../../../../../assets/pages/crud/detail/create.component.scss']
})
export class CreateComponent extends BaseDetailComponent implements OnInit {

  constructor(
    http: HttpClient, portofino: PortofinoService, translate: TranslateService,
    changeDetector: ChangeDetectorRef, notificationService: NotificationService) {
    super(http, portofino, translate, changeDetector, notificationService);
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

  @Button({ text: 'Cancel' })
  cancel() {
    this.close.emit();
  }

  doSave(object) {
    return this.http.post(this.sourceUrl, object);
  }

}
