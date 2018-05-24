import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {ClassAccessor, isEnabled, isInsertable, isUpdatable, Property} from "../../class-accessor";
import {HttpClient} from "@angular/common/http";
import {PortofinoService} from "../../portofino.service";
import * as moment from "moment";
import {Configuration} from "../crud.component";

@Component({
  selector: 'portofino-crud-create',
  templateUrl: './create.component.html',
  styleUrls: ['./create.component.css']
})
export class CreateComponent implements OnInit {

  @Input()
  classAccessor: ClassAccessor;
  @Input()
  configuration: Configuration;
  @Output()
  close = new EventEmitter();

  properties: Property[] = [];
  loadedObject;
  object;

  constructor(private http: HttpClient, private portofino: PortofinoService) { }

  ngOnInit() {
    this.classAccessor.properties.forEach(property => {
      if(!isEnabled(property)) {
        return;
      }
      this.properties.push(property);
      property.editable = isInsertable(property);
    });
    const objectUrl = `${this.portofino.apiPath + this.configuration.path}`;
    this.http.get(objectUrl, {params: {newObject: "true"}}).subscribe(o => this.initObject(o));
  }

  protected initObject(object) {
    this.loadedObject = object;
    this.object = {};
    this.properties.forEach(p => {
      if(!object[p.name]) {
        return;
      }
      if (this.portofino.isDate(p)) {
        this.object[p.name] = moment(object[p.name].value);
      } else if(!p.editable && object[p.name].displayValue) {
        this.object[p.name] = object[p.name].displayValue;
      } else {
        this.object[p.name] = object[p.name].value;
      }
    });
  }

  cancel() {
    this.close.emit();
  }

  save() {
    const objectUrl = `${this.portofino.apiPath + this.configuration.path}`;
    let object = {...this.object};
    this.properties.forEach(p => {
      if (this.portofino.isDate(p) && object[p.name]) {
        object[p.name] = object[p.name].valueOf();
      } else if(!p.editable && this.loadedObject[p.name]) {
        object[p.name] = this.loadedObject[p.name].value;
      }
    });
    this.http.post(objectUrl, object).subscribe(o => this.close.emit(object));
  }

}
