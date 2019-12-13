import { Host, HostBinding, Input, OnInit, Optional, SkipSelf, Directive } from '@angular/core';
import {isRequired, Property} from "../class-accessor";
import {
  AbstractControl,
  AbstractFormGroupDirective,
  ControlContainer,
  FormControl,
  FormGroup, FormGroupDirective
} from "@angular/forms";

@Directive()
export abstract class FieldComponent implements OnInit {

  @Input()
  @HostBinding('class.enabled')
  enabled: boolean = true;
  @Input()
  property: Property;
  @Input()
  form: FormGroup;
  @Input()
  selectable: boolean = false;

  control: AbstractControl;
  @Input()
  selector: FormControl;

  constructor(@Optional() @Host() @SkipSelf() protected controlContainer: ControlContainer) {}

  get required() {
    return isRequired(this.property);
  }

  ngOnInit() {
    if(!this.form) {
      if(this.controlContainer instanceof AbstractFormGroupDirective ||
         this.controlContainer instanceof FormGroupDirective) {
        this.form = this.controlContainer.control;
      } else {
        throw "A form must be provided to field " + this.property.name
      }
    }
    this.control = this.form.get(this.property.name);
    if(!this.control) {
      throw "No control found named " + this.property.name
    }
    if(!this.selector && this.selectable && this.enabled) {
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

}
