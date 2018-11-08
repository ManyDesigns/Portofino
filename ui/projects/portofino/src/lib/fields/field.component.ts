import {Component, Input, OnInit} from '@angular/core';
import {isMultiline, isRequired, isRichText, Property} from "../class-accessor";
import {PortofinoService} from "../portofino.service";
import {FormControl, FormGroup} from "@angular/forms";
import {AbstractControl} from "@angular/forms/src/model";

@Component({
  selector: 'portofino-field',
  templateUrl: './field.component.html',
  styleUrls: ['./field.component.css']
})
export class FieldComponent implements OnInit {

  @Input()
  enabled: boolean = true;
  @Input()
  property: Property;
  @Input()
  form: FormGroup;
  @Input()
  selectable: boolean;
  @Input()
  objectUrl: string;

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

  trackByOptionValue(index, option) {
    return option.v;
  }

  ngOnInit() {
    this.control = this.form.get(this.property.name);
    if(this.selectable && this.enabled) {
      this.selector = new FormControl();
      const validator = this.control.validator;
      this.control.clearValidators();
      this.control.updateValueAndValidity();
      this.selector.valueChanges.subscribe(v => {
        if(v) {
          this.control.setValidators(validator);
        } else {
          this.control.clearValidators();
        }
        this.property.editable = v;
        this.control.updateValueAndValidity({ emitEvent: false });
        this.control.markAsDirty();
      });
      this.control.valueChanges.subscribe(ch => {
        if((ch !== undefined && ch !== null) ||
           (this.selector.value !== undefined && this.selector.value !== null)) {
          this.selector.setValue(this.enabled);
        }
      });
    }
  }

  isMultiline() {
    return isMultiline(this.property);
  }

  isRichText() {
    return isRichText(this.property);
  }

}
