import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {PortofinoService} from "../../portofino.service";
import {Configuration, SelectionOption, SelectionProvider} from "../crud.component";
import {ClassAccessor, isEnabled, isUpdatable, Property} from "../../class-accessor";
import * as moment from "moment";
import {FormControl, FormGroup} from "@angular/forms";
import {BaseDetailComponent} from "./common.component";

@Component({
  selector: 'portofino-crud-detail',
  templateUrl: './detail.component.html',
  styleUrls: ['./detail.component.css']
})
export class DetailComponent extends BaseDetailComponent implements OnInit {

  @Input()
  id: string;

  editMode = false;

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
    this.http.get(objectUrl, {params: {forEdit: "true"}}).subscribe(o => this.createForm(o));
  }

  edit() {
    this.editMode = true;
    this.createForm(this.object);
  }

  delete() {
    const objectUrl = `${this.portofino.apiPath + this.configuration.source}/${this.id}`;
    this.http.delete(objectUrl).subscribe(() => this.close.emit(this.object));
  }

  cancel() {
    if(this.editMode) {
      this.editMode = false;
      this.createForm(this.object);
    } else {
      this.close.emit();
    }
  }

  save() {
    const objectUrl = `${this.portofino.apiPath + this.configuration.source}/${this.id}`;
    let object = this.getObjectToSave();
    this.http.put(objectUrl, object).subscribe(() => this.close.emit(object));
  }

}
