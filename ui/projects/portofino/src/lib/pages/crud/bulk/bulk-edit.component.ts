import {ChangeDetectorRef, Component, Input, OnInit, ViewChild} from '@angular/core';
import {HttpClient, HttpParams} from "@angular/common/http";
import {PortofinoService} from "../../../portofino.service";
import {isBlob, isDateProperty, isUpdatable, Property} from "../../../class-accessor";
import {BaseDetailComponent} from "../common.component";
import {FormComponent} from "../../../form";
import {NotificationService} from "../../../notifications/notification.services";
import {Button} from "../../../buttons";
import {TranslateService} from "@ngx-translate/core";

@Component({
  selector: 'portofino-crud-bulk-edit',
  templateUrl: '../../../../../assets/pages/crud/bulk/bulk-edit.component.html',
  styleUrls: ['../../../../../assets/pages/crud/bulk/bulk-edit.component.scss']
})
export class BulkEditComponent extends BaseDetailComponent implements OnInit {

  @Input()
  ids: string[];
  @ViewChild(FormComponent)
  formComponent: FormComponent;

  constructor(
    http: HttpClient, portofino: PortofinoService, translate: TranslateService,
    changeDetector: ChangeDetectorRef, notificationService: NotificationService) {
    super(http, portofino, translate, changeDetector, notificationService);
  }

  protected setupForm(object): void {
    super.setupForm(object);
    this.formDefinition.selectableFields = true;
  }

  isEditable(property: Property): boolean {
    return isUpdatable(property);
  }

  isEditEnabled(): boolean {
    return true;
  }

  protected filterProperty(property): boolean {
    return !!(super.filterProperty(property) || isBlob(property));
  }

  ngOnInit() {
    this.initClassAccessor();
    this.setupForm({});
  }

  @Button({ text: 'Cancel' })
  cancel() {
    this.close.emit();
  }

  isFormValid() {
    //TODO does this still apply?
    //This is a workaround because the form stays "invalid" even if all control validators are removed programmatically
    //after its creation.
    return this.form.valid || Object.keys(this.form.controls).every(k => {
      const control = this.form.controls[k];
      return control.valid || !control.enabled || control.pristine;
    });
  }

  protected getObjectToSave(): any {
    let object = {};
    this.properties.filter(p => p.editable).forEach(p => {
      const field = this.formComponent.allFields.find(item => {
        return item.property.name == p.name;
      });
      const selected = field.selector.value;
      if(!selected) {
        return;
      }
      let value = this.form.get(p.name).value;
      if(p.selectionProvider && value) {
        value = value.v;
      }
      if(value == null) {
        value = "";
      }
      if (isDateProperty(p) && value) {
        object[p.name] = value.valueOf();
      } else {
        object[p.name] = value;
      }
    });
    return object;
  }

  save() {
    if(!this.isFormValid()) {
      return;
    }
    let object = this.getObjectToSave();
    let params = new HttpParams();
    this.ids.forEach(id => params = params.append("id", id));
    this.http.put(this.sourceUrl, object,  { params: params }).subscribe(() => this.close.emit(object));
  }

  protected doSave(object): undefined {
    throw "Not used";
  }

}
