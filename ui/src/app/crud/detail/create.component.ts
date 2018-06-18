import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {PortofinoService} from "../../portofino.service";
import {Configuration, SelectionOption, SelectionProvider} from "../crud.component";
import {ClassAccessor, isEnabled, isInsertable, Property} from "../../class-accessor";
import * as moment from "moment";
import {FormControl, FormGroup} from "@angular/forms";
import {BaseDetailComponent} from "./common.component";

@Component({
  selector: 'portofino-crud-create',
  templateUrl: './create.component.html',
  styleUrls: ['./create.component.css']
})
export class CreateComponent extends BaseDetailComponent implements OnInit {

  constructor(protected http: HttpClient, protected portofino: PortofinoService) {
    super(http, portofino);
  }

  protected isEditable(property: Property): boolean {
    return isInsertable(property);
  }

  protected isEditEnabled(): boolean {
    return true;
  }

  ngOnInit() {
    this.initClassAccessor();
    const objectUrl = `${this.portofino.apiPath + this.configuration.source}`;
    this.http.get(objectUrl, {params: {newObject: "true"}}).subscribe(o => this.createForm(o));
  }

  cancel() {
    this.close.emit();
  }

  save() {
    const objectUrl = `${this.portofino.apiPath + this.configuration.source}`;
    let object = this.getObjectToSave();
    this.http.post(objectUrl, object).subscribe(() => this.close.emit(object));
  }

}
