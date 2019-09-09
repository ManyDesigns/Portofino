import {ChangeDetectorRef, Component, EventEmitter, Input, OnDestroy, OnInit, Output} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {PortofinoService} from "../../portofino.service";
import {isUpdatable, Property} from "../../class-accessor";
import {BaseDetailComponent} from "../common.component";
import {Operation} from "../../page";
import {NotificationService} from "../../notifications/notification.service";
import {Button} from "../../buttons";
import {TranslateService} from "@ngx-translate/core";
import {Subject} from "rxjs";

@Component({
  selector: 'portofino-crud-detail',
  templateUrl: './detail.component.html',
  styleUrls: ['./detail.component.css']
})
export class DetailComponent extends BaseDetailComponent implements OnInit, OnDestroy {

  @Input()
  id: string;
  prettyName: string;
  editEnabled: boolean;
  deleteEnabled: boolean;

  loading = false;
  editMode = false;
  @Output()
  editModeChanges = new EventEmitter();
  @Input()
  editOrView: Subject<boolean>;

  operationsPath = '/:operations';

  constructor(
    http: HttpClient, portofino: PortofinoService, translate: TranslateService,
    changeDetector: ChangeDetectorRef, notificationService: NotificationService) {
    super(http, portofino, translate, changeDetector, notificationService);
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
    //Multiple inits are possible when openDetailInSamePage is true
    if(this.properties.length == 0) {
      this.initClassAccessor();
    }
    this.loading = true;
    const objectUrl = `${this.sourceUrl}/${this.id}`;
    this.loadObject(objectUrl, () => {
      this.http.get<Operation[]>(objectUrl + this.operationsPath).subscribe(ops => {
        this.editEnabled = ops.some(op => op.signature == "PUT" && op.available);
        this.deleteEnabled = ops.some(op => op.signature == "DELETE" && op.available);
        this.editOrView.subscribe(edit => {
          if(this.editEnabled && edit) {
            this.edit();
          } else {
            this.cancel();
          }
        });
      });
    });
  }

  ngOnDestroy(): void {
    super.ngOnDestroy();
    this.editOrView.complete();
  }

  protected loadObject(objectUrl: string, onSuccess: () => void) {
    this.http.get(objectUrl, {params: {forEdit: "true"}, observe: 'response'}).subscribe(resp => {
      this.prettyName = resp.headers.get('X-Portofino-Pretty-Name') || this.id;
      this.loading = false;
      this.object = resp.body;
      this.setupForm(this.object);
      onSuccess();
    }, () => {
      this.loading = false;
      this.translate.get("Not found").subscribe(t => this.prettyName = t);
    });
  }

  @Button({ text: 'Edit', icon: 'edit', color: 'primary', presentIf: DetailComponent.isEditButtonEnabled })
  edit() {
    this.editMode = true;
    this.editModeChanges.emit(true);
    this.setupForm(this.object);
  }

  @Button({ text: 'Delete', icon: 'delete', color: 'warn', presentIf: DetailComponent.isDeleteButtonEnabled })
  delete() {
    const objectUrl = `${this.sourceUrl}/${this.id}`;
    this.http.delete(objectUrl).subscribe(() => this.close.emit(this.object));
  }

  @Button({ text: 'Cancel', presentIf: DetailComponent.isEditMode })
  cancel() {
    this.editMode = false;
    this.editModeChanges.emit(false);
    this.setupForm(this.object);
  }

  @Button({ text: 'Back', icon: 'arrow_back', presentIf: DetailComponent.isViewMode })
  backToSearch() {
    this.close.emit();
  }

  static isViewMode(self: DetailComponent) {
    return !self.editMode;
  }

  static isEditMode(self: DetailComponent) {
    return self.editMode;
  }

  static isEditButtonEnabled(self: DetailComponent) {
    return !self.editMode && self.editEnabled && self.object;
  }

  static isDeleteButtonEnabled(self: DetailComponent) {
    return !self.editMode && self.deleteEnabled && self.object;
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
