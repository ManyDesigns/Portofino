import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {PortofinoService} from "../../portofino.service";
import {Configuration, SelectionOption, SelectionProvider} from "../crud.component";
import {ClassAccessor, isEnabled, isUpdatable, Property} from "../../class-accessor";
import * as moment from "moment";
import {FormControl, FormGroup} from "@angular/forms";

export abstract class BaseDetailComponent {

  @Input()
  classAccessor: ClassAccessor;
  @Input()
  selectionProviders: SelectionProvider[];
  @Input()
  configuration: Configuration;
  @Output()
  close = new EventEmitter();

  form: FormGroup;
  properties: Property[] = [];
  object;

  constructor(protected http: HttpClient, protected portofino: PortofinoService) { }

  protected initClassAccessor() {
    this.classAccessor.properties.forEach(property => {
      if (!isEnabled(property)) {
        return;
      }
      property = {...property};
      this.properties.push(property);
      property.editable = this.isEditable(property);
    });
  }

  protected abstract isEditable(property: Property): boolean;

  protected abstract isEditEnabled(): boolean;

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
      const formState = { value: value, disabled: !this.isEditEnabled() || !this.isEditable(p) };
      formControls[p.name] = new FormControl(formState);
    });
    this.form = new FormGroup(formControls);

    this.selectionProviders.forEach(sp => {
      sp.fieldNames.forEach((name, index) => {
        const property = this.properties.find(p => p.name == name);
        if(!property) {
          return;
        }
        const spUrl = `${this.portofino.apiPath + this.configuration.source}/:selectionProvider/${sp.name}/${index}`;
        property.selectionProvider = {
          name: sp.name,
          index: index,
          displayMode: sp.displayMode,
          url: spUrl,
          options: []
        };
        if(index == 0) {
          this.http.get<SelectionOption[]>(spUrl).subscribe(
            options => {
              property.selectionProvider.options = options;
              const selected = options.find(o => o.s);
              this.form.get(property.name).setValue(selected ? selected.v : null);
            });
        }
        if(index < sp.fieldNames.length - 1) {
          property.selectionProvider.nextProperty = sp.fieldNames[index + 1];
        }
      });
    });
  }

  protected getObjectToSave(): any {
    let object = {};
    this.properties.filter(p => p.editable).forEach(p => {
      let value = this.form.get(p.name).value;
      if (this.portofino.isDate(p) && value) {
        object[p.name] = value.valueOf();
      } else {
        object[p.name] = value;
      }
    });
    return object;
  }
}
