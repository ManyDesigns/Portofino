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
  @Input()
  selectable: boolean;

  control: AbstractControl;
  selector: FormControl;

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
    if(this.selectable) {
      this.selector = new FormControl();
      const validator = this.control.validator;
      this.control.clearValidators();
      this.selector.valueChanges.subscribe(v => {
        if(v) {
          this.control.setValidators(validator);
        } else {
          this.control.clearValidators();
        }
        this.control.updateValueAndValidity({ emitEvent: false });
      });
      this.control.valueChanges.subscribe(ch => {
        this.selector.setValue(this.enabled);
      });
    }
  }

}
