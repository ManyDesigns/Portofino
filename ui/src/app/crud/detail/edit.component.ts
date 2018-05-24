import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {PortofinoService} from "../../portofino.service";
import {Configuration, CrudComponent} from "../crud.component";
import {ClassAccessor, isEnabled, isUpdatable, Property} from "../../class-accessor";

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
    this.http.get(objectUrl, {params: {forEdit: "true"}}).subscribe(
      object => {
        this.object = object;
      }
    );
  }

  cancel() {
    this.close.emit();
  }

  save() {

  }

}
