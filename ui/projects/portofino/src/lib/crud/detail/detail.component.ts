import {ChangeDetectorRef, Component, EventEmitter, Input, NgZone, OnInit, Output} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {PortofinoService} from "../../portofino.service";
import {isUpdatable, Property} from "../../class-accessor";
import {BaseDetailComponent} from "../common.component";
import {Operation} from "../../page";
import {NotificationService} from "../../notifications/notification.service";

@Component({
  selector: 'portofino-crud-detail',
  templateUrl: './detail.component.html',
  styleUrls: ['./detail.component.css']
})
export class DetailComponent extends BaseDetailComponent implements OnInit {

  @Input()
  id: string;
  prettyName: string;
  editEnabled: boolean;
  deleteEnabled: boolean;

  editMode = false;
  @Output()
  editModeChanges = new EventEmitter();

  operationsPath = '/:operations';

  constructor(
    protected http: HttpClient, protected portofino: PortofinoService,
    protected changeDetector: ChangeDetectorRef, protected notificationService: NotificationService) {
    super(http, portofino, changeDetector, notificationService);
  }

  isEditable(property: Property): boolean {
    return isUpdatable(property);
  }

  isEditEnabled(): boolean {
    return this.editMode;
  }

  protected setupForm(object): void {
    super.setupForm(object);
    this.formDefinition.baseUrl = this.sourceUrl + '/' + this.id;
  }

  ngOnInit() {
    this.initClassAccessor();
    const objectUrl = `${this.sourceUrl}/${this.id}`;
    this.http.get(objectUrl, {params: {forEdit: "true"}, observe: 'response'}).subscribe(resp => {
      this.prettyName = resp.headers.get('X-Portofino-Pretty-Name') || this.id;
      this.setupForm(resp.body);
    });
    this.http.get<Operation[]>(objectUrl + this.operationsPath).subscribe(ops => {
      this.editEnabled = ops.some(op => op.signature == "PUT" && op.available);
      this.deleteEnabled = ops.some(op => op.signature == "DELETE" && op.available);
    });
  }

  edit() {
    this.editMode = true;
    this.editModeChanges.emit(true);
    this.setupForm(this.object);
  }

  delete() {
    const objectUrl = `${this.sourceUrl}/${this.id}`;
    this.http.delete(objectUrl).subscribe(() => this.close.emit(this.object));
  }

  cancel() {
    if(this.editMode) {
      this.editMode = false;
      this.editModeChanges.emit(false);
      this.setupForm(this.object);
    } else {
      this.close.emit();
    }
  }

  save() {
    if(!this.editMode) {
      throw "Not in edit mode!";
    }
    return super.save();
  }

  protected doSave(object) {
    const objectUrl = `${this.sourceUrl}/${this.id}`;
    return this.http.put(objectUrl, object);
  }

}
