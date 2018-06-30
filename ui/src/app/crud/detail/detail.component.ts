import {Component, Input, OnInit} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {PortofinoService} from "../../portofino.service";
import {isUpdatable, Property} from "../../class-accessor";
import {BaseDetailComponent} from "../common.component";
import {Operation} from "../crud.component";
import {AbstractControl, FormArray, FormControl, FormGroup} from "@angular/forms";

@Component({
  selector: 'portofino-crud-detail',
  templateUrl: './detail.component.html',
  styleUrls: ['./detail.component.css']
})
export class DetailComponent extends BaseDetailComponent implements OnInit {

  @Input()
  id: string;
  editEnabled: boolean;
  deleteEnabled: boolean;

  editMode = false;

  operationsPath = '/:operations';

  constructor(protected http: HttpClient, protected portofino: PortofinoService) {
    super(http, portofino);
  }

  protected isEditable(property: Property): boolean {
    return isUpdatable(property);
  }

  protected isEditEnabled(): boolean {
    return this.editMode;
  }

  ngOnInit() {
    this.initClassAccessor();
    const objectUrl = `${this.portofino.apiPath + this.configuration.source}/${this.id}`;
    this.http.get(objectUrl, {params: {forEdit: "true"}}).subscribe(o => this.setupForm(o));
    this.http.get<Operation[]>(objectUrl + this.operationsPath).subscribe(ops => {
      this.editEnabled = ops.some(op => op.signature == "PUT" && op.available);
      this.deleteEnabled = ops.some(op => op.signature == "DELETE" && op.available);
    });
  }

  edit() {
    this.editMode = true;
    this.setupForm(this.object);
  }

  delete() {
    const objectUrl = `${this.portofino.apiPath + this.configuration.source}/${this.id}`;
    this.http.delete(objectUrl).subscribe(() => this.close.emit(this.object));
  }

  cancel() {
    if(this.editMode) {
      this.editMode = false;
      this.setupForm(this.object);
    } else {
      this.close.emit();
    }
  }

  save() {
    if(this.form.invalid) {
      this.triggerValidationForAllFields(this.form);
      return;
    }
    const objectUrl = `${this.portofino.apiPath + this.configuration.source}/${this.id}`;
    let object = this.getObjectToSave();
    this.http.put(objectUrl, object).subscribe(() => this.close.emit(object));
  }

}
