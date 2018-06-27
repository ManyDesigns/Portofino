import {Component, Input, OnInit} from '@angular/core';
import {HttpClient, HttpParams} from "@angular/common/http";
import {PortofinoService} from "../../portofino.service";
import {isUpdatable, Property} from "../../class-accessor";
import {BaseDetailComponent} from "../common.component";

@Component({
  selector: 'portofino-crud-bulk-edit',
  templateUrl: './bulk-edit.component.html',
  styleUrls: ['./bulk-edit.component.css']
})
export class BulkEditComponent extends BaseDetailComponent implements OnInit {

  @Input()
  ids: string[];

  constructor(protected http: HttpClient, protected portofino: PortofinoService) {
    super(http, portofino);
  }

  protected isEditable(property: Property): boolean {
    return isUpdatable(property);
  }

  protected isEditEnabled(): boolean {
    return true;
  }

  ngOnInit() {
    this.initClassAccessor();
    this.setupForm({});
  }

  cancel() {
    this.close.emit();
  }

  isFormValid() {
    //This is a workaround because the form stays "invalid" even if all control validators are removed programmatically
    //after its creation.
    return this.form.valid || Object.keys(this.form.controls).every(k => {
      const control = this.form.controls[k];
      return control.valid || !control.enabled || control.pristine;
    });
  }

  save() {
    if(!this.isFormValid()) {
      return;
    }
    const objectUrl = `${this.portofino.apiPath + this.configuration.source}/:bulk`;
    let object = this.getObjectToSave();
    let params = new HttpParams();
    this.ids.forEach(id => params = params.append("ids", id));
    this.http.put(objectUrl, object,  { params: params }).subscribe(() => this.close.emit(object));
  }

}
