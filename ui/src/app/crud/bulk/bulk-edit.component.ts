import {ChangeDetectorRef, Component, Input, OnInit} from '@angular/core';
import {HttpClient, HttpParams} from "@angular/common/http";
import {PortofinoService} from "../../portofino.service";
import {isBlob, isUpdatable, Property} from "../../class-accessor";
import {BaseDetailComponent} from "../common.component";
import {MatSnackBar} from "@angular/material";

@Component({
  selector: 'portofino-crud-bulk-edit',
  templateUrl: './bulk-edit.component.html',
  styleUrls: ['./bulk-edit.component.css']
})
export class BulkEditComponent extends BaseDetailComponent implements OnInit {

  @Input()
  ids: string[];

  constructor(
    protected http: HttpClient, protected portofino: PortofinoService,
    protected changeDetector: ChangeDetectorRef, protected snackBar: MatSnackBar) {
    super(http, portofino, changeDetector, snackBar);
  }

  protected isEditable(property: Property): boolean {
    return isUpdatable(property);
  }

  protected isEditEnabled(): boolean {
    return true;
  }

  protected filterProperty(property): boolean {
    return !!(super.filterProperty(property) || isBlob(property));
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
    let object = this.getObjectToSave();
    let params = new HttpParams();
    this.ids.forEach(id => params = params.append("id", id));
    this.http.put(this.sourceUrl, object,  { params: params }).subscribe(() => this.close.emit(object));
  }

  protected doSave(object): undefined {
    throw "Not used";
  }

}
