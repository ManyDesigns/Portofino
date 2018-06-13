import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {PortofinoService} from "../../portofino.service";
import {Configuration} from "../crud.component";
import {ClassAccessor, isEnabled, isUpdatable, Property} from "../../class-accessor";
import * as moment from "moment";
import {FormControl, FormGroup} from "@angular/forms";

@Component({
  selector: 'portofino-crud-detail',
  templateUrl: './detail.component.html',
  styleUrls: ['./detail.component.css']
})
export class DetailComponent implements OnInit {

  @Input()
  id: string;
  @Input()
  classAccessor: ClassAccessor;
  @Input()
  configuration: Configuration;
  @Output()
  close = new EventEmitter();

  form: FormGroup;
  properties: Property[] = [];
  object;
  editMode = false;

  constructor(private http: HttpClient, private portofino: PortofinoService) { }

  ngOnInit() {
    this.classAccessor.properties.forEach(property => {
      if(!isEnabled(property)) {
        return;
      }
      property = {...property};
      this.properties.push(property);
      property.editable = isUpdatable(property);
    });
    const objectUrl = `${this.portofino.apiPath + this.configuration.source}/${this.id}`;
    this.http.get(objectUrl, {params: {forEdit: "true"}}).subscribe(o => this.createForm(o));
  }

  protected createForm(object) {
    this.object = object;
    const formControls = {};
    this.properties.forEach(p => {
      let value;
      if(!object[p.name]) {
        //value is undefined
      } else if (this.portofino.isDate(p)) {
        value = moment(object[p.name].value);
      } else if(!p.editable && object[p.name].displayValue) {
        value = object[p.name].displayValue;
      } else {
        value = object[p.name].value;
      }
      formControls[p.name] = new FormControl({value: value, disabled: !this.editMode || !p.editable});
    });
    this.form = new FormGroup(formControls);
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
    let object = {};
    this.properties.filter(p => p.editable).forEach(p => {
      let value = this.form.get(p.name).value;
      if (this.portofino.isDate(p) && value) {
        object[p.name] = value.valueOf();
      } else {
        object[p.name] = value;
      }
    });
    this.http.put(objectUrl, object).subscribe(() => this.close.emit(object));
  }

}
