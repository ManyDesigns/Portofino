import {Component, Input, OnInit} from '@angular/core';
import {isRequired, Property} from "../../class-accessor";
import {PortofinoService} from "../../portofino.service";
import {FormControl, FormGroup} from "@angular/forms";
import {AbstractControl} from "@angular/forms/src/model";

@Component({
  selector: 'portofino-crud-field',
  templateUrl: './field.component.html',
  styleUrls: ['./field.component.css']
})
export class FieldComponent implements OnInit {

  @Input()
  enabled: boolean;
  @Input()
  property: Property;
  @Input()
  form: FormGroup;

  control: AbstractControl;

  constructor(public portofino: PortofinoService) { }

  getOptionLabel(option) {
    if (option && option.l) {
      return option.l;
    }
    return option;
  }

  isRequired() {
    return isRequired(this.property);
  }

  ngOnInit() {
    this.control = this.form.get(this.property.name);
  }

}
