import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {PortofinoService} from "../../portofino.service";
import {Configuration, CrudComponent} from "../crud.component";
import {ClassAccessor, isEnabled, isUpdatable, Property} from "../../class-accessor";
import * as moment from 'moment';

@Component({
  selector: 'portofino-crud-edit',
  templateUrl: './edit.component.html',
  styleUrls: ['./edit.component.css']
})
export class EditComponent implements OnInit {

  @Input()
  id: string;
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
      property.updatable = isUpdatable(property);
    });
    const objectUrl = `${this.portofino.apiPath + this.configuration.path}/${this.id}`;
    this.http.get(objectUrl, {params: {forEdit: "true"}}).subscribe(o => this.initObject(o));
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
      } else if(!p.updatable && object[p.name].displayValue) {
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
    const objectUrl = `${this.portofino.apiPath + this.configuration.path}/${this.id}`;
    let object = {...this.object};
    this.properties.forEach(p => {
      if (this.portofino.isDate(p) && object[p.name]) {
        object[p.name] = object[p.name].valueOf();
      } else if(!p.updatable && this.loadedObject[p.name]) {
        object[p.name] = this.loadedObject[p.name].value;
      }
    });
    this.http.put(objectUrl, object).subscribe(o => this.close.emit(object));
  }

}
